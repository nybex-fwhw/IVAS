package com.labtrack.parser;

import com.labtrack.model.Analysis;
import com.labtrack.model.Sample;
import com.labtrack.model.TestType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class XmlDeserializer {

    private static <T> List<T> parseElements(
            Document document,
            String tagName,
            Function<Element, T> mapper
    ) {
        NodeList nodes =
                document.getElementsByTagName(tagName);

        List<T> results = new ArrayList<>();

        for (int i = 0; i < nodes.getLength(); i++) {
            Element element = (Element) nodes.item(i);
            results.add(mapper.apply(element));
        }

        return results;
    }

    // ************************SAMPLE***************************

    private static Sample toSample(Element element) {
        /*TODO*/
        return new Sample();
    }

    private static List<Sample> deserializeSamples(
            Document document
    ) {
        return parseElements(
                document,
                "sample",
                XmlDeserializer::toSample
        );
    }

    // ************************TEST_TYPE***************************

    private static TestType toTestType(Element element) {
        /*TODO*/
        return new TestType();
    }

    private static List<TestType> deserializeTests(
            Document document
    ) {
        return parseElements(
                document,
                "test_type",
                XmlDeserializer::toTestType
        );
    }

    // ************************ANALAYSIS***************************

    private static Analysis toAnalysis(Element element) {
        /*TODO*/
        return new Analysis();
    }

    private static List<Analysis> deserializeAnalyses(
            Document document
    ) {
        return parseElements(
                document,
                "analysis",
                XmlDeserializer::toAnalysis
        );
    }

    public static DeserializationResult deserialize(
            Document document
    ) {
        if (document == null ||
                document.getDocumentElement() == null) {
            throw new XmlDeserializationException(
                    "XML document is empty."
            );
        }

        String root = document.getDocumentElement()
                .getTagName();

        return switch (root) {

            case "samples" -> new DeserializationResult.Samples(
                    deserializeSamples(document)
            );

            case "tests" -> new DeserializationResult.Tests(
                    deserializeTests(document)
            );

            case "analyses" -> new DeserializationResult.Analyses(
                    deserializeAnalyses(document)
            );

            default -> throw new XmlDeserializationException(
                    "Unsupported XML root: " + root
            );
        };
    }
}