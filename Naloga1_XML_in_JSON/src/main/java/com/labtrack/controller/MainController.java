package com.labtrack.controller;

import com.labtrack.model.Analysis;
import com.labtrack.model.Sample;
import com.labtrack.model.TestType;
import com.labtrack.parser.DeserializationResult;
import com.labtrack.parser.XmlDataLoader;
import com.labtrack.parser.XmlDeserializer;
import com.labtrack.service.LaboratoryService;
import com.labtrack.service.validation.ValidationResult;
import com.labtrack.service.validation.ValidationService;
import org.w3c.dom.Document;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainController {
    public static void loadData() {
        XmlDataLoader loader = new XmlDataLoader();
        ValidationService validator = new ValidationService();

        LaboratoryService service = new LaboratoryService(loader, validator);

        Document documentSamples = XmlDataLoader.loadData("/home/nybex/Documents/FERI/5-semester/TIIDS/project/IVAS/Naloga1_XML_in_JSON/data/samples.xml");
        Document documentTestTypes = XmlDataLoader.loadData("/home/nybex/Documents/FERI/5-semester/TIIDS/project/IVAS/Naloga1_XML_in_JSON/data/test_types.xml");
        Document documentAnalysis = XmlDataLoader.loadData("/home/nybex/Documents/FERI/5-semester/TIIDS/project/IVAS/Naloga1_XML_in_JSON/data/analysis.xml");

        DeserializationResult resultSamples = XmlDeserializer.deserialize(documentSamples);
        DeserializationResult resultTestTypes = XmlDeserializer.deserialize(documentTestTypes);
        DeserializationResult resultAnalysis = XmlDeserializer.deserialize(documentAnalysis);

        if (!(resultSamples instanceof DeserializationResult.Samples s)) {
            throw new IllegalStateException("Expected samples XML.");
        }

        if (!(resultTestTypes instanceof DeserializationResult.Tests t)) {
            throw new IllegalStateException("Expected tests XML.");
        }

        if (!(resultAnalysis instanceof DeserializationResult.Analyses a)) {
            throw new IllegalStateException("Expected analyses XML.");
        }

        List<Sample> samples = s.values();
        List<TestType> testTypes = t.values();
        List<Analysis> analyses = a.values();

        ValidationResult result = validator.validate(
                samples,
                testTypes,
                analyses
        );

        if (result.isValid()) {
            System.out.println("Validation successful!");
        } else {
            System.err.println("Validation failed:");

            result.errors().forEach(System.err::println);
        }

    }
}