package com.labtrack.service;

import com.labtrack.model.Analysis;
import com.labtrack.model.AnalysisView;
import com.labtrack.model.Sample;
import com.labtrack.model.TestType;
import com.labtrack.parser.DataNormalizer;
import com.labtrack.parser.XmlDataLoader;
import com.labtrack.parser.XmlDeserializer;
import com.labtrack.service.validation.ValidationService;

import lombok.Getter;
import org.w3c.dom.Document;

import java.util.List;

@Getter
public class LaboratoryService {

    private List<Sample> samples = List.of();
    private List<TestType> testTypes = List.of();
    private List<Analysis> analyses = List.of();

    private List<AnalysisView> analysisViews = List.of();

    private final XmlDataLoader loader;
    private final ValidationService validator;

    private static void loadData(String fileName) {
        Document document = DataNormalizer.normalizeAll(XmlDataLoader.loadData(fileName));

        XmlDeserializer.deserialize(document);

    }

    public LaboratoryService(XmlDataLoader loader, ValidationService validator) {
        this.loader = loader;
        this.validator = validator;
    }
}