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

import java.io.IOException;
import java.util.List;

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
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                this.executeProcess(List.of("cmd", "/c", "start", url));
            } else if (os.contains("mac")) {
                this.executeProcess(List.of("open", url));
            } else {
                this.executeProcess(List.of("xdg-open", url));
            }
            log.info("Navegador abierto en: " + url);
        } catch (Exception e) {
            log.error("Error: {}", e.getMessage());
        }
    }

    private void executeProcess(List<String> commands) throws IOException {
        new ProcessBuilder()
                .command(commands)
                .inheritIO()
                .redirectInput(ProcessBuilder.Redirect.PIPE)
                .start();
    }

}
