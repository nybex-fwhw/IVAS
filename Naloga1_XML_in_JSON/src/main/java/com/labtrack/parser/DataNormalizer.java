package com.labtrack.parser;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class DataNormalizer {
    private static String normalize(String string) {
        return string.toUpperCase().trim();
    }

    private static void normalizeNode(Node node) {
        if(node == null) return;
        if (node.getNodeType() == Node.TEXT_NODE) {
            node.setNodeValue(normalize(node.getNodeValue()));
        }

        for (Node child = node.getFirstChild(); child != null; ) {
            Node next = child.getNextSibling();
            normalizeNode(child);
            child = next;
        }
    }

    public static Document normalizeAll(Document document) {
        normalizeNode(document);
        return document;
    }
}
