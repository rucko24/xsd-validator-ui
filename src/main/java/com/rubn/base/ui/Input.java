package com.rubn.base.ui;

import com.rubn.base.ui.list.FileListItem;
import com.rubn.base.ui.list.List;
import com.rubn.base.ui.utility.ConfirmDialogBuilder;
import com.rubn.base.ui.utility.InputTheme;
import com.rubn.xsdvalidator.service.ValidationXsdSchemaService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.shared.Tooltip;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.internal.JacksonUtils;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.server.streams.TransferContext;
import com.vaadin.flow.theme.lumo.LumoUtility;
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static com.rubn.base.ui.Constants.BORDER_BOTTOM_COLOR;
import static com.rubn.base.ui.Constants.JAVA_IO_USER_HOME_DIR_OS;
import static com.rubn.base.ui.Constants.OUTPUT_DIR_XSD_VALIDATOR_UI;
import static com.rubn.base.ui.Constants.SCROLLBAR_CUSTOM_STYLE;
import static com.rubn.base.ui.Constants.WINDOW_COPY_TO_CLIPBOARD;

@Slf4j
public class Input extends Layout implements BeforeEnterObserver {

    private static final Map<String, String> MAP_PREFIX_FILE_NAME_AND_CONTENT = new HashMap<>();

    private final VerticalLayout verticalLayoutArea;
    private final Button attachment;
    private final Button validateButton;
    private final List list;
    private final Uploader uploader;
    private final ValidationXsdSchemaService validationXsdSchemaService;
    private final Button buttonCleanFileList;

    public Input(final ValidationXsdSchemaService validationXsdSchemaService) {
        this.validationXsdSchemaService = validationXsdSchemaService;

        addClassNames(Background.CONTRAST_5, Border.ALL, BorderColor.CONTRAST_5, BorderRadius.LARGE,
                Margin.Horizontal.AUTO, Margin.Top.LARGE, MaxWidth.SCREEN_MEDIUM, Padding.SMALL);
        setBoxSizing(Layout.BoxSizing.BORDER);
        setFlexDirection(Layout.FlexDirection.COLUMN);
        setGap(Gap.SMALL);
        setMaxHeight("400px");

        // Text area
        verticalLayoutArea = new VerticalLayout();
        verticalLayoutArea.setHeight("350px");
        verticalLayoutArea.addClassNames(Padding.NONE, Width.FULL);
        verticalLayoutArea.addThemeName(InputTheme.TRANSPARENT);
        verticalLayoutArea.getStyle().setOverflow(Style.Overflow.AUTO);
        verticalLayoutArea.getElement().executeJs(SCROLLBAR_CUSTOM_STYLE);

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

        this.buttonCleanFileList = new Button(VaadinIcon.TRASH.create());
        this.buttonCleanFileList.addClickListener(event -> {
            uploader.clearFileList();
            uploader.getElement().setPropertyJson("files", JacksonUtils.createArrayNode());
        });

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
                        .sorted((a, b) -> a.endsWith(".xml") ? -1 : 0)
                        .toArray(String[]::new);
                String xmlFileName = nombresOrdenados[0];
                String xsdSchemaFileName = nombresOrdenados[1];
                this.validationXsdSchemaService.validateXmlInputWithXsdSchema(xmlFileName, xsdSchemaFileName)
                        .doOnError(onError -> {
                            this.executeUI(() -> {
                                log.error("Error validating: {}", xmlFileName, onError);
                            });
                        })
                        .delayElements(Duration.ofMillis(700))
                        .subscribe(listConstaintErrors -> {
                            this.executeUI(() -> {
                                if (!listConstaintErrors.isEmpty()) {
                                    log.info("Error: " + listConstaintErrors);
                                    this.buildCustomSpan(listConstaintErrors);
                                } else {
                                    ConfirmDialogBuilder.showInformation("Validation successfully");
                                }
                            });
                        });
            } catch (Exception e) {
                this.executeUI(() -> {
                    log.error(e.getLocalizedMessage());
                    ConfirmDialogBuilder.showWarning("Validation error " + e.getLocalizedMessage());
                    this.buildCustomSpan(e.getLocalizedMessage());
                });
            }
        });

        Layout actions = new Layout(this.uploader, validateButton);
        actions.setJustifyContent(JustifyContent.BETWEEN);
        actions.setAlignItems(AlignItems.CENTER);

        add(list, verticalLayoutArea, actions);
    }

    private void buildCustomSpan(String listConstaintErrors) {
        final Span span = new Span(listConstaintErrors);
        span.getStyle().setCursor("pointer");
        Tooltip.forComponent(span).setText("copy text");
        span.addClassNames(LumoUtility.FontSize.SMALL, TextColor.SECONDARY);
        span.getStyle().setBorderBottom(BORDER_BOTTOM_COLOR);
//        final SvgIcon icon = new SvgIcon(DownloadHandler.forClassResource(getClass(),
//                "/META-INF/resources/svg-images/copy-alt.svg" + "copy-alt.svg"));
//        icon.setSize("25px");
        span.addClickListener(event -> {
            UI.getCurrent().getPage().executeJs(WINDOW_COPY_TO_CLIPBOARD, span.getText());
            Notification.show("Error copied!", 2000, Notification.Position.BOTTOM_CENTER);
        });
        verticalLayoutArea.add(span);
    }

    private void executeUI(Command command) {
        super.getUI().ifPresent(ui -> {
            ui.access(command);
        });
    }

    private boolean listConstaintErrors(java.util.List<String> listString) {
        return !listString.isEmpty();
    }

    private CustomFileUploadHandler buildUploadHandler() {
        return this.uploader.buildUploadHandler()
                .whenStart(() -> {
                    getElement().setPropertyJson("files", JacksonUtils.createArrayNode());
                    log.info("Upload started");
                })
                .whenComplete((transferContext, success) -> {
                    this.uploader.clearFileList();
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
                            Files.deleteIfExists(Path.of(JAVA_IO_USER_HOME_DIR_OS.concat(OUTPUT_DIR_XSD_VALIDATOR_UI).concat(fileName)));
                        } catch (IOException e) {
                            log.error(e.getMessage());
                            ConfirmDialogBuilder.showWarningUI("File could not be deleted " + fileName, transferContext.getUI());
                        }
                    });
        });

    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        verticalLayoutArea.getElement().executeJs(SCROLLBAR_CUSTOM_STYLE);
    }
}
