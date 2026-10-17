package com.labtrack.parser;

import org.w3c.dom.Document;

import java.util.HashMap;
import java.util.Map;

public class XmlDeserializer {
    public static <V> Map<String, V> deserialize(Document document) {
        Map<String, Object> map = new HashMap<>();
        switch(document.getDocumentElement().getTagName()) {
            case "samples":

                break;
            case "tests":
                break;
            case "analyses":
                break;
            default:
                /*TODO*/
                break;
        }
    }
}
