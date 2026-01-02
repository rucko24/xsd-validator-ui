package com.rubn.xsdvalidator.configuration;

import com.rubn.xsdvalidator.records.AppVersionRecord;
import org.apache.maven.model.v4.MavenStaxReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.xml.stream.XMLStreamException;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author rubn
 */
@Configuration
public class AppVersionPropertiesConfiguration {

    @Bean
    public MavenStaxReader mavenXpp3Reader() {
        return new MavenStaxReader();
    }

    @Bean
    public AppVersionRecord appVersion(final MavenStaxReader mavenStaxReader) throws IOException, XMLStreamException {
        String appVersion = mavenStaxReader.read(new FileReader("pom.xml")).getVersion();
        return new AppVersionRecord(appVersion);
    }


}

