package com.rubn.xsdvalidator.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ValidationXsdSchemaServiceTest {

    @InjectMocks
    private ValidationXsdSchemaService validationXsdSchemaService;

    @Test
    void validateXmlInputWithXsdSchema() throws IOException, SAXException {

        List<String> listString = validationXsdSchemaService.validateXmlInputWithXsdSchema(".xml", "schema-xml.xsd");

        assertThat(listString).isEmpty();

    }
}