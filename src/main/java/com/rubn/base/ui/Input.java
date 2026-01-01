package com.rubn.base.ui;

import com.rubn.base.ui.list.FileListItem;
import com.rubn.base.ui.list.List;
import com.rubn.base.ui.utility.ConfirmDialogBuilder;
import com.rubn.base.ui.utility.InputTheme;
import com.rubn.xsdvalidator.enums.EnableValidateButtonEnum;
import com.rubn.xsdvalidator.events.EnableValidateButtonEventRecord;
import com.rubn.xsdvalidator.service.ValidationXsdSchemaService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextAreaVariant;
import com.vaadin.flow.internal.JacksonUtils;
import com.vaadin.flow.server.streams.TransferContext;
import com.vaadin.flow.theme.lumo.LumoUtility.Background;
import com.vaadin.flow.theme.lumo.LumoUtility.Border;
import com.vaadin.flow.theme.lumo.LumoUtility.BorderColor;
import com.vaadin.flow.theme.lumo.LumoUtility.BorderRadius;
import com.vaadin.flow.theme.lumo.LumoUtility.Margin;
import com.vaadin.flow.theme.lumo.LumoUtility.MaxWidth;
import com.vaadin.flow.theme.lumo.LumoUtility.Padding;
import com.vaadin.flow.theme.lumo.LumoUtility.TextColor;
import com.vaadin.flow.theme.lumo.LumoUtility.Width;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static com.rubn.base.ui.Constants.JAVA_IO_USER_HOME_DIR_OS;
import static com.rubn.base.ui.Constants.OUTPUT_DIR_UI_XSD_VALIDATOR;

@Slf4j
public class Input extends Layout {

    private static final Map<String, String> MAP_PREFIX_FILE_NAME_AND_CONTENT = new HashMap<>();

    private final TextArea textArea;
    private final Button attachment;
    private final Button validateButton;
    private final List list;
    private final Uploader uploader;
    private final ValidationXsdSchemaService validationXsdSchemaService;

    public Input(final ValidationXsdSchemaService validationXsdSchemaService, ApplicationEventPublisher applicationEventPublisher) {
        this.validationXsdSchemaService = validationXsdSchemaService;

        addClassNames(Background.CONTRAST_5, Border.ALL, BorderColor.CONTRAST_5, BorderRadius.LARGE,
                Margin.Horizontal.AUTO, Margin.Top.LARGE, MaxWidth.SCREEN_MEDIUM, Padding.SMALL);
        setBoxSizing(BoxSizing.BORDER);
        setFlexDirection(FlexDirection.COLUMN);
        setGap(Gap.SMALL);

        // Text area
        textArea = new TextArea();
        textArea.setPlaceholder("...");
        textArea.addClassNames(Padding.NONE, Width.FULL);
        textArea.addThemeName(InputTheme.TRANSPARENT);
        textArea.addThemeVariants(TextAreaVariant.LUMO_SMALL);

        // Actions
        attachment = new Button(VaadinIcon.UPLOAD.create());
        attachment.addClassNames(Background.TRANSPARENT, Border.ALL, BorderColor.CONTRAST_20, BorderRadius.FULL,
                Margin.Vertical.NONE, Padding.NONE, TextColor.SECONDARY);
        attachment.addThemeVariants(ButtonVariant.LUMO_SMALL);
        attachment.setAriaLabel("Attachment");
        attachment.setTooltipText("Attachment");

        this.list = new List();
        list.setFlexWrap(FlexWrap.WRAP);
        list.setDisplay(Display.FLEX);
        list.setGap(Gap.SMALL);
        list.removeBackgroundColor();

        this.uploader = new Uploader(attachment);
        this.uploader.setUploadHandler(this.buildUploadHandler());

        validateButton = new Button("Validate", VaadinIcon.CHECK.create());
        validateButton.setDisableOnClick(true);
        validateButton.setEnabled(false);
        validateButton.setIconAfterText(true);
        validateButton.addClassNames(BorderRadius.LARGE, Margin.NONE, Padding.SMALL);
        validateButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        validateButton.setAriaLabel("Validate");
        validateButton.setTooltipText("validate");
        validateButton.addClickListener(event -> {
            try {
                String[] nombresOrdenados = MAP_PREFIX_FILE_NAME_AND_CONTENT.keySet()
                        .stream()
                        .sorted((a, b) -> a.endsWith(".xml") ? -1 : 1)
                        .toArray(String[]::new);

                String xmlFileName = nombresOrdenados[0];
                String xsdSchemaFileName = nombresOrdenados[1];

                java.util.List<String> listString = this.validationXsdSchemaService.validateXmlInputWithXsdSchema(xmlFileName, xsdSchemaFileName);
                if (!listString.isEmpty()) {
                    log.info("Errors " + listString);
                } else {
                    ConfirmDialogBuilder.showInformation("Validation successfully");
                }
            } catch (Exception e) {
                log.error(e.getLocalizedMessage());
                ConfirmDialogBuilder.showWarning("Validation error " + e.getLocalizedMessage());
                textArea.setValue(e.getLocalizedMessage());
            }
        });

        Layout actions = new Layout(this.uploader, validateButton);
        actions.setJustifyContent(JustifyContent.BETWEEN);
        actions.setAlignItems(AlignItems.CENTER);

        add(list, textArea, actions);
    }

    private CustomFileUploadHandler buildUploadHandler() {
        return this.uploader.buildUploadHandler()
                .whenStart(() -> {
                    getElement().setPropertyJson("files", JacksonUtils.createArrayNode());
                    log.info("Upload started");
                })
                .whenComplete((transferContext, success) -> {
                    getElement().setPropertyJson("files", JacksonUtils.createArrayNode());
                    final UI ui = transferContext.getUI();
                    if (success) {
                        log.info("Upload completed successfully");
                        this.processFile(transferContext);
                    } else {
                        log.info("Upload failed");
                        ConfirmDialogBuilder.showWarningUI(transferContext.exception().getMessage(), ui);
                    }
                });
    }

    private void processFile(final TransferContext transferContext) {
        final String fileName = transferContext.fileName();
        long contentLength = transferContext.contentLength();
        String content = "Code ⋅ " + contentLength + "KB";
        MAP_PREFIX_FILE_NAME_AND_CONTENT.put(fileName, content);
        final FileListItem fileListItem = new FileListItem(fileName, content);
        list.add(fileListItem);

        validateButton.setEnabled(MAP_PREFIX_FILE_NAME_AND_CONTENT.size() == 2);

        final ContextMenu contextMenu = new ContextMenu();
        contextMenu.setTarget(fileListItem);
        contextMenu.addItem("Delete", event -> {
            ConfirmDialogBuilder.showConfirmInformation("Do you want to delete: " + fileName, transferContext.getUI())
                    .addConfirmListener(confirm -> {
                        list.remove(fileListItem);
                        try {
                            Files.deleteIfExists(Path.of(JAVA_IO_USER_HOME_DIR_OS.concat(OUTPUT_DIR_UI_XSD_VALIDATOR).concat(fileName)));
                        } catch (IOException e) {
                            log.error(e.getMessage());
                            ConfirmDialogBuilder.showWarningUI("File could not be deleted " + fileName, transferContext.getUI());
                        }
                    });
        });

    }

    @EventListener
    private void executeValidation(EnableValidateButtonEventRecord enableValidateButtonEventRecord) {
        boolean value = enableValidateButtonEventRecord.enableValidateButtonEnum() == EnableValidateButtonEnum.ENABLED;
        attachment.setEnabled(value);
    }
}
