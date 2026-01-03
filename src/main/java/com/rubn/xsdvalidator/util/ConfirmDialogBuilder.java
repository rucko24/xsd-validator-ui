package com.rubn.xsdvalidator.util;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import lombok.Getter;
import lombok.Value;
import lombok.extern.log4j.Log4j2;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

import static com.rubn.xsdvalidator.util.XsdValidatorConstants.INFORMATION;
import static com.rubn.xsdvalidator.util.XsdValidatorConstants.OK;
import static com.rubn.xsdvalidator.util.XsdValidatorConstants.WARNING;

/**
 * ConfirmDialogBuilder
 *
 * @author rubn
 */
@Log4j2
@Value
public class ConfirmDialogBuilder {

    public static void showWarning(String text, final Component component) {
        ConfirmDialogBuilder.builder()
                .withHeaderIconAndHeaderText(VaadinIcon.WARNING, WARNING)
                .withText(text)
                .withComponent(component)
                .build();
    }

    public static void showWarning(String text) {
        ConfirmDialogBuilder.builder()
                .withHeaderIconAndHeaderText(VaadinIcon.WARNING, WARNING)
                .withText(text)
                .withComponent(null)
                .build();
    }

    public static void showWarningUI(String text, final UI ui) {
        final var confirmDialog = ConfirmDialogBuilder.builder()
                .withHeaderIconAndHeaderText(VaadinIcon.WARNING, WARNING)
                .withText(text)
                .withComponent(null)
                .build();
        ui.addToModalComponent(confirmDialog);
    }

    public static void showInformationUI(String text, final UI ui) {
        final var confirmDialog = ConfirmDialogBuilder.builder()
                .withHeaderIconAndHeaderText(VaadinIcon.INFO, INFORMATION)
                .withText(text)
                .withComponent(null)
                .build();
        ui.addToModalComponent(confirmDialog);
    }

    public static void showInformation(final String text) {
        ConfirmDialogBuilder.builder()
                .withHeaderIconAndHeaderText(VaadinIcon.INFO, INFORMATION)
                .withText(text)
                .withComponent(null)
                .build();
    }

    public static ConfirmDialog showConfirmInformation(final String text, UI ui) {
        var confirmDialog = ConfirmDialogBuilder.builder()
                .withHeaderIconAndHeaderText(VaadinIcon.WARNING, WARNING)
                .withText(text)
                .withComponent(null)
                .build();
        confirmDialog.setCancelable(true);
        confirmDialog.setConfirmText("Delete");
        confirmDialog.setConfirmButtonTheme("error primary");
        ui.addToModalComponent(confirmDialog);
        return confirmDialog;
    }

    private static WithIconAndHeaderText builder() {
        return new InnerBuilder();
    }

    /**
     * 1
     */
    public interface WithIconAndHeaderText {
        Text withHeaderIconAndHeaderText(final VaadinIcon icon, final String textHeader);
    }

    /**
     * 2
     */
    public interface Text {
        ComponentStage withText(final String text);
    }

    /**
     * 3
     */
    public interface ComponentStage {
        Build withComponent(final Component component);
    }

    /**
     * 4
     */
    public interface Build extends IBuilder<ConfirmDialog> {

    }

    public interface IBuilder<T> {
        @NonNull T build();
    }

    /**
     * The magic builder
     */
    @Getter
    public static class InnerBuilder implements Text, WithIconAndHeaderText, ComponentStage, Build {

        private VaadinIcon icon;
        private String text;
        private String headerText;
        private final ConfirmDialog confirmDialog = new ConfirmDialog();
        private Component component;

        @Override
        public ComponentStage withText(String text) {
            Objects.requireNonNull(text, "Text must not be null");
            this.text = text;
            return this;
        }

        @Override
        public Text withHeaderIconAndHeaderText(VaadinIcon icon, String headerText) {
            Objects.requireNonNull(icon, "Icon must not be null");
            Objects.requireNonNull(headerText, "Header text must not be null");
            this.icon = icon;
            this.headerText = headerText;
            return this;
        }

        @Override
        public Build withComponent(Component component) {
            this.component = component;
            return this;
        }

        @Override
        @NonNull
        public ConfirmDialog build() {
            final HorizontalLayout headerRow = new HorizontalLayout(icon.create(), new H3(headerText));
            headerRow.setAlignItems(Alignment.CENTER);
            confirmDialog.setHeader(headerRow);
            confirmDialog.setConfirmText(OK);

            if (icon == VaadinIcon.WARNING) {
                confirmDialog.setConfirmButtonTheme("error primary");
            }
            confirmDialog.setText(text);

            if (Objects.nonNull(component)) {
                confirmDialog.setText(component);
            }

            confirmDialog.open();
            return confirmDialog;
        }

    }
}

