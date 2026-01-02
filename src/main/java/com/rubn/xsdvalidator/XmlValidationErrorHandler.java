package com.rubn.xsdvalidator;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class XmlValidationErrorHandler implements ErrorHandler {

    private final List<String> exceptions = new CopyOnWriteArrayList<>();

    @Override
    public void warning(SAXParseException exception) {
        exceptions.add("WARNING: " + formatMessage(exception));
    }

    @Override
    public void error(SAXParseException exception) {
        exceptions.add("ERROR: " + formatMessage(exception));
    }

    @Override
    public void fatalError(SAXParseException exception) {
        exceptions.add("FATAL: " + formatMessage(exception));
    }

    public List<String> getExceptions() {
        return exceptions;
    }

    public boolean isValid() {
        return exceptions.isEmpty();
    }

    private String formatMessage(SAXParseException e) {
        return String.format("Línea %d, Columna %d: %s", e.getLineNumber(), e.getColumnNumber(), e.getMessage());
    }
}