package com.rubn.xsdvalidator.configuration;

import com.rubn.xsdvalidator.records.AppVersionRecord;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author rubn
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppVersionConfiguration {

    private String appVersion;

    @Bean
    public AppVersionRecord appVersion() {
        return new AppVersionRecord(appVersion);
    }


}

