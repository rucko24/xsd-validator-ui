package com.rubn.xsdvalidator.service;

import com.rubn.xsdvalidator.providers.FailureErrorLineValidationXsdSchemaProvider;
import com.rubn.xsdvalidator.providers.FailureValidationXsdSchemaProvider;
import com.rubn.xsdvalidator.providers.SuccessValidationXsdSchemaProvider;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Map;

@ExtendWith(MockitoExtension.class)
class ValidationXsdSchemaServiceTest {

    @InjectMocks
    private ValidationXsdSchemaService validationXsdSchemaService;

    @ParameterizedTest
    @ArgumentsSource(SuccessValidationXsdSchemaProvider.class)
    @DisplayName("Valida xml agains xsd schema")
    void case1(byte[] inputXml, byte[] inputXsd, final Map<String, byte[]> mapPrefixFileNameAndContent) {

        Flux<String> listString = validationXsdSchemaService.validateXmlInputWithXsdSchema(inputXml, inputXsd, mapPrefixFileNameAndContent)
                .flatMapIterable(this.validationXsdSchemaService::detectWords);

        StepVerifier.create(listString)
                .verifyComplete();

    }

    @Disabled
    @ParameterizedTest
    @ArgumentsSource(FailureValidationXsdSchemaProvider.class)
    @DisplayName("Failed Valida xml agains xsd schema")
    void case2(byte[] inputXml, byte[] inputXsd, final Map<String, byte[]> mapPrefixFileNameAndContent) {

        Flux<String> listString = validationXsdSchemaService.validateXmlInputWithXsdSchema(inputXml, inputXsd, mapPrefixFileNameAndContent)
                .flatMapSequential(Flux::fromIterable);

        StepVerifier.create(listString.log())
                .expectNext("ERROR: [Linea] [8], Columna 58: cvc-pattern-valid: Value 'captainexample.org' is not facet-valid with respect to pattern '[^@]+@[^\\.]+\\..+' for type 'EmailType'.")
                .expectNext("ERROR: [Linea] [8], Columna 58: cvc-type.3.1.3: The value 'captainexample.org' of element 'm:CustomerEmail' is not valid.")
                .expectNext("ERROR: [Linea] [21], Columna 44: cvc-minExclusive-valid: Value '-10.00' is not facet-valid with respect to minExclusive '0.0' for type 'PriceType'.")
                .expectNext("ERROR: [Linea] [21], Columna 44: cvc-type.3.1.3: The value '-10.00' of element 'prod:Price' is not valid.")
                .verifyComplete();

    }

    @ParameterizedTest(name = "case {0}")
    @ArgumentsSource(FailureErrorLineValidationXsdSchemaProvider.class)
    @DisplayName("Failed Valida xml agains xsd schema")
    void case3(String name, byte[] inputXml, byte[] inputXsd, final Map<String, byte[]> mapPrefixFileNameAndContent) {

        Flux<String> listString = validationXsdSchemaService.validateXmlInputWithXsdSchema(inputXml, inputXsd, mapPrefixFileNameAndContent)
                .flatMapSequential(Flux::fromIterable);

        StepVerifier.create(listString.log())
                .expectErrorMatches(error -> error.getMessage().contains("The prefix \"prod\" for element \"prod:Sku\" is not bound."))
                .verify();

    }

    @ParameterizedTest
    @ArgumentsSource(FailureErrorLineValidationXsdSchemaProvider.class)
    @DisplayName("Error resolving imported schema: datatypes.xsd")
    void case4(String name, byte[] inputXml, byte[] inputXsd, final Map<String, byte[]> mapPrefixFileNameAndContent) {

        Flux<String> listString = validationXsdSchemaService.validateXmlInputWithXsdSchema(inputXml, inputXsd, Map.of())
                .flatMapSequential(Flux::fromIterable);

        StepVerifier.create(listString.log())
                .expectErrorMatches(error -> error.getMessage().contains("Error resolving imported schema: datatypes.xsd"))
                .verify();

    }

}