
package com.labtrack.parser;

import com.labtrack.model.Analysis;
import com.labtrack.model.Sample;
import com.labtrack.model.TestType;
import com.labtrack.model.enums.*;

import org.w3c.dom.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

public class XmlDeserializer {

    private static final DateTimeFormatter ISO_DATE =
            DateTimeFormatter.ISO_LOCAL_DATE;

    private static final DateTimeFormatter LEGACY_DATE =
            DateTimeFormatter.ofPattern("dd-MM-uuuu")
                    .withResolverStyle(ResolverStyle.STRICT);


    // **************** COMMON HELPERS ****************

    private static <T> List<T> parseElements(
            Document document,
            String tagName,
            Function<Element, T> mapper
    ) {
        NodeList nodes = document.getDocumentElement()
                .getElementsByTagName(tagName);

        List<T> results = new ArrayList<>();

        for (int i = 0; i < nodes.getLength(); i++) {
            Element element = (Element) nodes.item(i);
            results.add(mapper.apply(element));
        }

        return results;
    }


    // Required XML attribute, e.g. id="SMP-..."
    private static String getRequiredAttribute(
            Element element,
            String attributeName
    ) {
        String value = element.getAttribute(attributeName).trim();

        if (value.isEmpty()) {
            throw new XmlDeserializationException(
                    "Missing attribute '" + attributeName +
                            "' in <" + element.getTagName() + ">"
            );
        }

        return value;
    }


    // Reads a direct child element's text.
    // Returns null if missing or empty.
    private static String getOptionalText(
            Element element,
            String tagName
    ) {
        NodeList children = element.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);

            if (node.getNodeType() == Node.ELEMENT_NODE
                    && node.getNodeName().equals(tagName)) {

                String value = node.getTextContent().trim();
                return value.isEmpty() ? null : value;
            }
        }

        return null;
    }


    // Required XML text field
    private static String getRequiredText(
            Element element,
            String tagName
    ) {
        String value = getOptionalText(element, tagName);

        if (value == null) {
            throw new XmlDeserializationException(
                    "Missing or empty <" + tagName +
                            "> in <" + element.getTagName() +
                            " id='" + element.getAttribute("id") + "'>"
            );
        }

        return value;
    }


    // Converts text to a Java enum
    private static <E extends Enum<E>> E parseEnum(
            String value,
            Class<E> enumType
    ) {
        try {
            return Enum.valueOf(
                    enumType,
                    value.trim().toUpperCase(Locale.ROOT)
            );
        } catch (IllegalArgumentException e) {
            throw new XmlDeserializationException(
                    "Invalid " + enumType.getSimpleName() +
                            " value: " + value,
                    e
            );
        }
    }


    // Converts a date string to LocalDate
    private static LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return LocalDate.parse(value, ISO_DATE);
        } catch (DateTimeParseException ignored) {
            // Try legacy date format
        }

        try {
            return LocalDate.parse(value, LEGACY_DATE);
        } catch (DateTimeParseException e) {
            throw new XmlDeserializationException(
                    "Invalid date: " + value,
                    e
            );
        }
    }


    // **************** SAMPLE ****************

    private static Sample toSample(Element element) {

        String id = getRequiredAttribute(element, "id");

        SampleType sampleType = parseEnum(
                getRequiredText(element, "sampleType"),
                SampleType.class
        );

        LocalDate collectionDate = parseDate(
                getRequiredText(element, "collectionDate")
        );

        // Supports both correct spelling and the older XML typo
        String receivedText = getOptionalText(
                element, "receivedDate"
        );

        if (receivedText == null) {
            receivedText = getOptionalText(
                    element, "recivedDate"
            );
        }

        LocalDate receivedDate = parseDate(receivedText);

        SampleStatus status = parseEnum(
                getRequiredText(element, "status"),
                SampleStatus.class
        );

        return new Sample(
                id,
                sampleType,
                collectionDate,
                receivedDate,
                status
        );
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


    // **************** TEST TYPE ****************

    private static TestType toTestType(Element element) {

        String id = getRequiredAttribute(element, "id");

        String name = getRequiredText(element, "name");

        TestCategory category = parseEnum(
                getRequiredText(element, "category"),
                TestCategory.class
        );

        String description = getRequiredText(
                element, "description"
        );

        SampleType requiredSampleType = parseEnum(
                getRequiredText(element, "requiredSampleType"),
                SampleType.class
        );

        return new TestType(
                id,
                name,
                category,
                description,
                requiredSampleType
        );
    }


    private static List<TestType> deserializeTests(
            Document document
    ) {
        return parseElements(
                document,
                "test",
                XmlDeserializer::toTestType
        );
    }


    // **************** ANALYSIS ****************

    private static Analysis toAnalysis(Element element) {

        String id = getRequiredAttribute(element, "id");

        String sampleId = getRequiredText(
                element, "sampleId"
        );

        String testId = getRequiredText(
                element, "testId"
        );

        LocalDate startDate = parseDate(
                getOptionalText(element, "startDate")
        );

        LocalDate completionDate = parseDate(
                getOptionalText(element, "completionDate")
        );

        AnalysisStatus status = parseEnum(
                getRequiredText(element, "status"),
                AnalysisStatus.class
        );

        QcResult qcResult = parseEnum(
                getRequiredText(element, "qcResult"),
                QcResult.class
        );

        LocalDate reviewDate = parseDate(
                getOptionalText(element, "reviewDate")
        );

        return new Analysis(
                id,
                sampleId,
                testId,
                startDate,
                completionDate,
                status,
                qcResult,
                reviewDate
        );
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


    // **************** ENTRY POINT ****************

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
