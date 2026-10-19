
package com.labtrack.service;

import com.labtrack.model.Analysis;
import com.labtrack.model.AnalysisView;
import com.labtrack.model.Sample;
import com.labtrack.model.TestType;

import com.labtrack.parser.DeserializationResult;
import com.labtrack.parser.XmlDataLoader;
import com.labtrack.parser.XmlDeserializer;

import com.labtrack.service.filter.AnalysisFilter;
import com.labtrack.service.validation.ValidationResult;
import com.labtrack.service.validation.ValidationService;

import lombok.Getter;
import org.w3c.dom.Document;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class LaboratoryService {

    private final ValidationService validator;

    private List<Sample> samples = List.of();
    private List<TestType> testTypes = List.of();
    private List<Analysis> analyses = List.of();
    private List<AnalysisView> analysisViews = List.of();

    public LaboratoryService(ValidationService validator) {
        this.validator = validator;
    }

    private final FilterService filterService = new FilterService();

    public List<AnalysisView> filterAnalyses(AnalysisFilter filter) {
        return filterService.filter(this.analysisViews, filter);
    }

    public ValidationResult loadData(Path dataDirectory) {
        try {
            // 1. Read XML documents
            Document samplesDoc = XmlDataLoader.loadData(
                    dataDirectory.resolve("samples.xml").toString()
            );

            Document testsDoc = XmlDataLoader.loadData(
                    dataDirectory.resolve("test_types.xml").toString()
            );

            Document analysesDoc = XmlDataLoader.loadData(
                    dataDirectory.resolve("analysis.xml").toString()
            );

            // 2. Deserialize XML into Java models
            DeserializationResult samplesResult =
                    XmlDeserializer.deserialize(samplesDoc);

            DeserializationResult testsResult =
                    XmlDeserializer.deserialize(testsDoc);

            DeserializationResult analysesResult =
                    XmlDeserializer.deserialize(analysesDoc);

            // 3. Check the expected document types
            if (!(samplesResult instanceof DeserializationResult.Samples s)) {
                throw new IllegalStateException("Expected samples XML.");
            }

            if (!(testsResult instanceof DeserializationResult.Tests t)) {
                throw new IllegalStateException("Expected tests XML.");
            }

            if (!(analysesResult instanceof DeserializationResult.Analyses a)) {
                throw new IllegalStateException("Expected analyses XML.");
            }

            List<Sample> loadedSamples = s.values();
            List<TestType> loadedTests = t.values();
            List<Analysis> loadedAnalyses = a.values();

            // 4. Validate the models
            ValidationResult result = validator.validate(
                    loadedSamples,
                    loadedTests,
                    loadedAnalyses
            );

            if (!result.isValid()) {
                return result;
            }

            // 5. Prepare joined data
            List<AnalysisView> loadedViews = buildAnalysisViews(
                    loadedSamples,
                    loadedTests,
                    loadedAnalyses
            );

            // 6. Commit only after all steps succeed
            this.samples = List.copyOf(loadedSamples);
            this.testTypes = List.copyOf(loadedTests);
            this.analyses = List.copyOf(loadedAnalyses);
            this.analysisViews = List.copyOf(loadedViews);

            return result;

        } catch (Exception e) {
            return new ValidationResult(
                    List.of("Failed to load data: " + e.getMessage())
            );
        }
    }

    private List<AnalysisView> buildAnalysisViews(
            List<Sample> samples,
            List<TestType> testTypes,
            List<Analysis> analyses
    ) {
        Map<String, Sample> samplesById = new HashMap<>();
        Map<String, TestType> testsById = new HashMap<>();

        for (Sample sample : samples) {
            samplesById.put(sample.getSampleId(), sample);
        }

        for (TestType test : testTypes) {
            testsById.put(test.getTestId(), test);
        }

        List<AnalysisView> views = new ArrayList<>();

        for (Analysis analysis : analyses) {
            Sample sample = samplesById.get(analysis.getSampleId());
            TestType test = testsById.get(analysis.getTestId());

            if (sample == null || test == null) {
                throw new IllegalStateException(
                        "Missing reference for analysis: "
                                + analysis.getAnalysisId()
                );
            }

            views.add(new AnalysisView(analysis, sample, test));
        }

        return views;
    }
}
