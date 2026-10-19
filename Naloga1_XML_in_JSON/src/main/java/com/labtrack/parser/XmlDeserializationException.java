package com.labtrack.parser;

public class XmlDeserializationException
        extends RuntimeException {

    public XmlDeserializationException(String message) {
        super(message);
    }

    public XmlDeserializationException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}