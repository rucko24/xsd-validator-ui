package com.rubn.xsdvalidator.service;

import com.rubn.xsdvalidator.XmlValidationErrorHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;
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
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static com.rubn.base.ui.Constants.JAVA_IO_USER_HOME_DIR_OS;
import static com.rubn.base.ui.Constants.OUTPUT_DIR_XSD_VALIDATOR_UI;

@Slf4j
@Service
public class ValidationXsdSchemaService {

    public Flux<String> validateXmlInputWithXsdSchema(final String xmlFileName, final String fileNameXsdSchema) throws IOException, SAXException {
        return this.loadSchema(fileNameXsdSchema)
                .map(Schema::newValidator)
                .flatMap(this::buildXmlValidatorErrorHandler)
                .flatMap(tuple -> this.buildValidator(tuple, xmlFileName))
                .flatMapMany(Flux::fromIterable);
    }

    private Mono<Schema> loadSchema(final String fileNameSchema) throws SAXException {
        final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        final Path path = Path.of(JAVA_IO_USER_HOME_DIR_OS.concat(OUTPUT_DIR_XSD_VALIDATOR_UI).concat(fileNameSchema));
        try (var inputStream = new BufferedInputStream(Files.newInputStream(path))) {
            return Mono.just(schemaFactory.newSchema(new StreamSource(inputStream)));
        } catch (IOException ex) {
            return Mono.error(ex);
        }
    }

    private Mono<Tuple2<Validator, XmlValidationErrorHandler>> buildXmlValidatorErrorHandler(Validator validator) {
        final XmlValidationErrorHandler xmlValidationErrorHandler = new XmlValidationErrorHandler();
        validator.setErrorHandler(xmlValidationErrorHandler);
        return Mono.zip(Mono.just(validator), Mono.just(xmlValidationErrorHandler));
    }

    private Mono<List<String>> buildValidator(Tuple2<Validator, XmlValidationErrorHandler> tuple, String xmlFileName) {
        final Validator validator = tuple.getT1();
        final XmlValidationErrorHandler xmlValidationErrorHandler = tuple.getT2();
        try {
            validator.validate(this.buildStreamSource(xmlFileName));
            return Mono.just(xmlValidationErrorHandler.getExceptions());
        } catch (Exception e) {
            return Mono.error(e);
        }
    }

    /**
     *
     * Desde una ruta la validacion es mejor
     *
     * @param xmlFileName
     * @return
     * @throws IOException
     */
    private StreamSource buildStreamSource(String xmlFileName) throws IOException {
        String inputStream = Files.readAllLines(Path.of(JAVA_IO_USER_HOME_DIR_OS.concat(OUTPUT_DIR_XSD_VALIDATOR_UI).concat(xmlFileName)))
                .stream()
                .collect(Collectors.joining("\n"));
        return new StreamSource(new StringReader(inputStream));
    }

}
