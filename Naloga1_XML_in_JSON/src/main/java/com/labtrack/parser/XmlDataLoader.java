package com.labtrack.parser;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

public class XmlDataLoader {
    private static Document parse(String fileName) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setIgnoringElementContentWhitespace(true);

        DocumentBuilder builder = factory.newDocumentBuilder();

        File file = new File(fileName);
        return builder.parse(file);
    }

    public static Document loadData(String fileName)  {
        Document document = null;
        try {
            document = parse(fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return document;
    }
}
