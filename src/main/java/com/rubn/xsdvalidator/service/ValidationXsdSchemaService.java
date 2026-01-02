package com.rubn.xsdvalidator.service;

import com.rubn.xsdvalidator.XmlValidationErrorHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.rubn.base.ui.Constants.JAVA_IO_USER_HOME_DIR_OS;
import static com.rubn.base.ui.Constants.OUTPUT_DIR_UI_XSD_VALIDATOR;

@Slf4j
@Service
public class ValidationXsdSchemaService {

    public Flux<String> validateXmlInputWithXsdSchema(final String xmlFileName, final String fileNameXsdSchema) throws IOException, SAXException {
        return this.loadSchema(fileNameXsdSchema)
                .map(Schema::newValidator)
                .flatMap(validator -> {
                    final XmlValidationErrorHandler xmlValidationErrorHandler = new XmlValidationErrorHandler();
                    validator.setErrorHandler(xmlValidationErrorHandler);
                    return Mono.zip(Mono.just(validator), Mono.just(xmlValidationErrorHandler));
                })
                .flatMap(tuple -> {
                    final Validator validator = tuple.getT1();
                    final XmlValidationErrorHandler xmlValidationErrorHandler = tuple.getT2();
                    try {
                        validator.validate(this.buildStreamSource(xmlFileName));
                        return Mono.just(xmlValidationErrorHandler.getExceptions());
                    } catch (Exception e) {
                        return Mono.error(e);
                    }
                }).flatMapMany(Flux::fromIterable);
    }

    private Mono<Schema> loadSchema(final String fileNameSchema) throws SAXException {
        final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        final Path path = Path.of(JAVA_IO_USER_HOME_DIR_OS.concat(OUTPUT_DIR_UI_XSD_VALIDATOR).concat(fileNameSchema));
        try (var inputStream = new BufferedInputStream(Files.newInputStream(path))) {
            return Mono.just(schemaFactory.newSchema(new StreamSource(inputStream)));
        } catch (IOException ex) {
            return Mono.error(ex);
        }
    }

    private StreamSource buildStreamSource(String xmlInput) throws IOException {
        InputStream inputStream = Files.newInputStream(Path.of(JAVA_IO_USER_HOME_DIR_OS.concat(OUTPUT_DIR_UI_XSD_VALIDATOR).concat(xmlInput)));
        return new StreamSource(new BufferedInputStream(inputStream));
    }

}
