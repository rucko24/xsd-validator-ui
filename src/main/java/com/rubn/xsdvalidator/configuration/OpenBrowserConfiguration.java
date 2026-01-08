package com.rubn.xsdvalidator.configuration;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;

@Slf4j
@Order(Ordered.LOWEST_PRECEDENCE)
@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "app")
public class OpenBrowserConfiguration {

    private boolean browserAutoOpenBrowser;

    @Autowired
    private Environment environment;

    @EventListener(ApplicationReadyEvent.class)
    public void openBrowser() {
        if (browserAutoOpenBrowser) {
            return;
        }
        String port = environment.getProperty("server.port");
        String url = "http://localhost:" + port;
        try {
            new ProcessBuilder()
                    .command("xdg-open", url)
                    .inheritIO()
                    .redirectInput(ProcessBuilder.Redirect.PIPE)
                    .start();
        } catch (Exception e) {
            log.error("Error: {}", e.getMessage());
        }
    }

}
