package com.rubn.xsdvalidator.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.theme.lumo.LumoUtility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;

@Slf4j
public class Uploader extends Upload {

    private final Component uploadComponent;

    public Uploader(final Component uploadComponent) {
        this.uploadComponent = uploadComponent;
        this.uploadInitialConfig();// File list
    }

    /**
     * Configure the uploader
     */
    private void uploadInitialConfig() {
        super.setDropAllowed(true);
        super.setMaxFiles(2);
        super.setWidthFull();
        super.addClassNames("upload-xml-xsd", LumoUtility.Padding.XSMALL, LumoUtility.Margin.Right.MEDIUM, LumoUtility.Margin.Left.NONE);

        super.setAcceptedFileTypes(MediaType.APPLICATION_XML_VALUE, ".xml", ".xsd");
        super.setUploadButton(this.uploadComponent);
        super.setDropLabel(new Span("Drop files here xml and xsd"));
        super.setDropLabelIcon(new Span());
        super.addFileRejectedListener(event -> {
            String errorMessage = "Incorrect file type, only xml and xsd";
            Notification notification = Notification.show(errorMessage, 2000,
                    Notification.Position.MIDDLE);
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        });
    }

    @Override
    public void clearFileList() {
        getElement().executeJs("this.files = [];" + "return this.files;");
    }

}
