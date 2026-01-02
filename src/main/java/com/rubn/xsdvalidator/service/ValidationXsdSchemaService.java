package com.rubn.xsdvalidator.service;

import com.rubn.xsdvalidator.XmlValidationErrorHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Slf4j
@Service
public class ValidationXsdSchemaService {

    public Flux<String> validateXmlInputWithXsdSchema(final InputStream inputXml, final InputStream inputXsdSchema) {
        return this.loadSchema(inputXsdSchema)
                .map(Schema::newValidator)
                .flatMap(this::buildXmlValidatorErrorHandler)
                .flatMap(tuple -> this.buildValidator(tuple, inputXml))
                .flatMapMany(Flux::fromIterable);
    }

    private Mono<Schema> loadSchema(final InputStream inputXsdSchema) {
        final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        try {
            return Mono.just(schemaFactory.newSchema(this.buildStreamSource(inputXsdSchema)));
        } catch (Exception ex) {
            return Mono.error(ex);
        }
    }

    private Mono<Tuple2<Validator, XmlValidationErrorHandler>> buildXmlValidatorErrorHandler(Validator validator) {
        final XmlValidationErrorHandler xmlValidationErrorHandler = new XmlValidationErrorHandler();
        validator.setErrorHandler(xmlValidationErrorHandler);
        return Mono.zip(Mono.just(validator), Mono.just(xmlValidationErrorHandler));
    }

    private Mono<List<String>> buildValidator(Tuple2<Validator, XmlValidationErrorHandler> tuple, InputStream inputXml) {
        final Validator validator = tuple.getT1();
        final XmlValidationErrorHandler xmlValidationErrorHandler = tuple.getT2();
        try {
            validator.validate(this.buildStreamSource(inputXml));
            return Mono.just(xmlValidationErrorHandler.getExceptions());
        } catch (Exception e) {
            return Mono.error(e);
        }
    }

    /**
     *
     * Desde una ruta la validacion es mejor
     *
     * @param inputXml
     * @return StreamSource
     * @throws IOException
     */
    private StreamSource buildStreamSource(InputStream inputXml) {
        return new StreamSource(new BufferedInputStream(inputXml));
    }

}
