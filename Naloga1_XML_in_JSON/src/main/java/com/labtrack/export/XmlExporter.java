
package com.labtrack.export;

import com.labtrack.model.AnalysisView;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public final class XmlExporter {

    private XmlExporter() {}

    public static void export(
            List<AnalysisView> analyses,
            Path outputPath
    ) throws Exception {

        Objects.requireNonNull(analyses);
        Objects.requireNonNull(outputPath);

        DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();

        Document document =
                factory.newDocumentBuilder().newDocument();

        Element root = document.createElement("analyses");
        document.appendChild(root);

        for (AnalysisView analysis : analyses) {

            Element element = document.createElement("analysis");
            element.setAttribute("id", analysis.getAnalysisId());
            root.appendChild(element);

            addElement(document, element, "sampleId",
                    analysis.getSampleId());

            addElement(document, element, "sampleType",
                    analysis.getSampleType().name());

            addElement(document, element, "testId",
                    analysis.getTestId());

            addElement(document, element, "testName",
                    analysis.getTestName());

            addElement(document, element, "category",
                    analysis.getCategory().name());

            addElement(document, element, "startDate",
                    dateText(analysis.getStartDate()));

            addElement(document, element, "completionDate",
                    dateText(analysis.getCompletionDate()));

            addElement(document, element, "reviewDate",
                    dateText(analysis.getReviewDate()));

            addElement(document, element, "status",
                    analysis.getStatus().name());

            addElement(document, element, "qcResult",
                    analysis.getQcResult().name());
        }

        TransformerFactory transformerFactory =
                TransformerFactory.newInstance();

        transformerFactory.setFeature(
                XMLConstants.FEATURE_SECURE_PROCESSING, true
        );

        transformerFactory.setAttribute(
                XMLConstants.ACCESS_EXTERNAL_DTD, ""
        );
        transformerFactory.setAttribute(
                XMLConstants.ACCESS_EXTERNAL_STYLESHEET, ""
        );

        Transformer transformer =
                transformerFactory.newTransformer();

        transformer.setOutputProperty(
                OutputKeys.INDENT, "yes"
        );

        transformer.setOutputProperty(
                OutputKeys.ENCODING, "UTF-8"
        );

        transformer.setOutputProperty(
                "{http://xml.apache.org/xslt}indent-amount", "4"
        );

        Path parent = outputPath.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        transformer.transform(
                new DOMSource(document),
                new StreamResult(outputPath.toFile())
        );
    }

    private static void addElement(
            Document document,
            Element parent,
            String name,
            String value
    ) {
        Element child = document.createElement(name);
        child.setTextContent(value == null ? "" : value);
        parent.appendChild(child);
    }

    private static String dateText(LocalDate date) {
        return date == null ? null : date.toString();
    }
}
