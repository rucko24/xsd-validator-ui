package com.rubn.xsdvalidator.service;

import com.rubn.xsdvalidator.XmlValidationErrorHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

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
import java.util.List;

import static com.rubn.base.ui.Constants.JAVA_IO_USER_HOME_DIR_OS;
import static com.rubn.base.ui.Constants.OUTPUT_DIR_UI_XSD_VALIDATOR;

@Slf4j
@Service
public class ValidationXsdSchemaService {

    public List<String> validateXmlInputWithXsdSchema(final String xmlFileName, final String fileNameXsdSchema) throws IOException, SAXException {
        final Validator validator = this.loadSchema(fileNameXsdSchema).newValidator();
        final XmlValidationErrorHandler xmlValidationErrorHandler = new XmlValidationErrorHandler();
        validator.setErrorHandler(xmlValidationErrorHandler);
        validator.validate(this.buildStreamSource(xmlFileName));
        return xmlValidationErrorHandler.getExceptions();
    }

    private Schema loadSchema(final String fileNameSchema) throws IOException, SAXException {
        final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        final Path path = Path.of(JAVA_IO_USER_HOME_DIR_OS.concat(OUTPUT_DIR_UI_XSD_VALIDATOR).concat(fileNameSchema));
        try (var inputStream = new BufferedInputStream(Files.newInputStream(path))) {
            return schemaFactory.newSchema(new StreamSource(inputStream));
        } catch (IOException ex) {
            throw ex;
        }
    }

    private StreamSource buildStreamSource(String xmlInput) throws IOException {
        InputStream inputStream = Files.newInputStream(Path.of(JAVA_IO_USER_HOME_DIR_OS.concat(OUTPUT_DIR_UI_XSD_VALIDATOR).concat(xmlInput)));
        return new StreamSource(new BufferedInputStream(inputStream));
    }

}
