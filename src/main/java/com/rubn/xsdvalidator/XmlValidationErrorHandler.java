package com.rubn.xsdvalidator;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;
import java.util.ArrayList;
import java.util.List;

public class XmlValidationErrorHandler implements ErrorHandler {

    private final List<String> exceptions = new ArrayList<>();

    @Override
    public void warning(SAXParseException exception) {
        // Puedes decidir ignorar los warnings o guardarlos
        exceptions.add("WARNING: " + formatMessage(exception));
    }

    @Override
    public void error(SAXParseException exception) {
        // Errores de validación (ej: tipo de dato incorrecto, falta campo obligatorio)
        exceptions.add("ERROR: " + formatMessage(exception));
    }

    @Override
    public void fatalError(SAXParseException exception) {
        // Errores graves (ej: XML mal formado que impide seguir leyendo)
        exceptions.add("FATAL: " + formatMessage(exception));
    }

    public List<String> getExceptions() {
        return exceptions;
    }

    public boolean isValid() {
        return exceptions.isEmpty();
    }

    private String formatMessage(SAXParseException e) {
        return String.format("Línea %d, Columna %d: %s", 
            e.getLineNumber(), e.getColumnNumber(), e.getMessage());
    }
}