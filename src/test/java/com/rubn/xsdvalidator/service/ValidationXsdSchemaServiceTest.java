package com.rubn.xsdvalidator.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.xml.sax.SAXException;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.IOException;

@ExtendWith(MockitoExtension.class)
class ValidationXsdSchemaServiceTest {

    @InjectMocks
    private ValidationXsdSchemaService validationXsdSchemaService;

    @Test
    void buildValidatorXmlInputWithXsdSchema() throws IOException, SAXException {

        Flux<String> listString = validationXsdSchemaService.validateXmlInputWithXsdSchema(".xml", "schema-xml.xsd")
                .doOnNext(System.out::println);

        StepVerifier.create(listString)
                .expectNext("ERROR: Línea 45, Columna 63: cvc-pattern-valid: El valor 'ES79 2100 0000 0000 0000 0000' no es de faceta válida con respecto al patrón '[A-Z]{2,2}[0-9]{2,2}[a-zA-Z0-9]{1,30}' para el tipo 'IBAN2007Identifier'.")
                .expectNext("ERROR: Línea 45, Columna 63: cvc-type.3.1.3: El valor 'ES79 2100 0000 0000 0000 0000' del elemento 'IBAN' no es válido.")
                .expectNext("ERROR: Línea 63, Columna 30: cvc-complex-type.2.4.a: Se ha encontrado contenido no válido a partir del elemento '{\"urn:iso:std:iso:20022:tech:xsd:\":BIC}'. Se esperaba uno de '{\"urn:iso:std:iso:20022:tech:xsd:\":BICFI, \"urn:iso:std:iso:20022:tech:xsd:\":ClrSysMmbId, \"urn:iso:std:iso:20022:tech:xsd:\":LEI, \"urn:iso:std:iso:20022:tech:xsd:\":Nm, \"urn:iso:std:iso:20022:tech:xsd:\":PstlAdr, \"urn:iso:std:iso:20022:tech:xsd:\":Othr}'.")
                .verifyComplete();

    }
}