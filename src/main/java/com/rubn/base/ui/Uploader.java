package com.rubn.base.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.theme.lumo.LumoUtility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;

import static com.rubn.base.ui.Constants.JAVA_IO_USER_HOME_DIR_OS;
import static com.rubn.base.ui.Constants.OUTPUT_DIR_UI_XSD_VALIDATOR;

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
        super.setMaxFiles(1);
        super.setWidthFull();
        super.addClassNames("validator-upload", LumoUtility.Padding.XSMALL, LumoUtility.Margin.Right.MEDIUM, LumoUtility.Margin.Left.NONE);

        super.setAcceptedFileTypes(MediaType.APPLICATION_XML_VALUE, ".xml", ".xsd");
        super.setUploadButton(this.uploadComponent);
        super.setDropLabel(new Span("Drop files here (.xml, xsd)"));
        super.setDropLabelIcon(new Span());

    }

    public CustomFileUploadHandler buildUploadHandler() {
        return new CustomFileUploadHandler(JAVA_IO_USER_HOME_DIR_OS.concat(OUTPUT_DIR_UI_XSD_VALIDATOR), this);
    }

}
