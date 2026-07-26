package com.rubn;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.ColorScheme;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.AppShellSettings;
import com.vaadin.flow.theme.lumo.Lumo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Push
@SpringBootApplication
@StyleSheet(Lumo.STYLESHEET)
@StyleSheet(Lumo.UTILITY_STYLESHEET)
@StyleSheet("styles.css")
@ColorScheme(ColorScheme.Value.LIGHT)
public class Application implements AppShellConfigurator {

    static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void configurePage(AppShellSettings settings) {
        settings.addLink("shortcut icon", "icons/icon.png");
        settings.addFavIcon("icon", "icons/icon.png", "192x192");
    }

}
