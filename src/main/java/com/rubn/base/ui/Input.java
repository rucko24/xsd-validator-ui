package com.rubn.base.ui;

import com.infraleap.animatecss.Animated;
import com.rubn.base.ui.list.FileListItem;
import com.rubn.base.ui.list.ListCustom;
import com.rubn.base.ui.utility.ConfirmDialogBuilder;
import com.rubn.xsdvalidator.service.ValidationXsdSchemaService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ScrollIntoViewOption;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.UIDetachedException;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.shared.Tooltip;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.server.streams.InMemoryUploadHandler;
import com.vaadin.flow.server.streams.UploadHandler;
import com.vaadin.flow.server.streams.UploadMetadata;
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
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.rubn.base.ui.Constants.BORDER_BOTTOM_COLOR;
import static com.rubn.base.ui.Constants.CONTEXT_MENU_ITEM_NO_CHECKMARK;
import static com.rubn.base.ui.Constants.SCROLLBAR_CUSTOM_STYLE;
import static com.rubn.base.ui.Constants.VAR_CUSTOM_BOX_SHADOW;
import static com.rubn.base.ui.Constants.WINDOW_COPY_TO_CLIPBOARD;

@Slf4j
public class Input extends Layout implements BeforeEnterObserver {

    private final Map<String, InputStream> mapPrefixFileNameAndContent = new ConcurrentHashMap<>();

    private final VerticalLayout verticalLayoutArea;
    private final Button attachment;
    private final Button validateButton;
    private final ListCustom list;
    private final Uploader uploader;
    private final Button buttonCleanFileList;
    /**
     * Service
     */
    private final ValidationXsdSchemaService validationXsdSchemaService;
    /**
     * Mutable fields
     */
    private Span span;

    public Input(final ValidationXsdSchemaService validationXsdSchemaService) {
        this.validationXsdSchemaService = validationXsdSchemaService;

        addClassNames(Background.CONTRAST_5, Border.ALL, BorderColor.CONTRAST_5, BorderRadius.LARGE,
                Margin.Horizontal.AUTO, Margin.Top.LARGE, MaxWidth.SCREEN_MEDIUM, Padding.SMALL);
        setBoxSizing(BoxSizing.BORDER);
        setFlexDirection(FlexDirection.COLUMN);
        setGap(Gap.SMALL);
        getStyle().setBoxShadow(VAR_CUSTOM_BOX_SHADOW);
        setMaxHeight("400px");

        // Text area
        verticalLayoutArea = new VerticalLayout();
        verticalLayoutArea.getStyle().setCursor("pointer");
        verticalLayoutArea.setHeight("350px");
        verticalLayoutArea.addClassNames(Padding.NONE, Width.FULL);
        verticalLayoutArea.getStyle().setOverflow(Style.Overflow.AUTO);
        verticalLayoutArea.getElement().executeJs(SCROLLBAR_CUSTOM_STYLE);
        final ContextMenu contextMenu = this.buildContextMenu(verticalLayoutArea);
        contextMenu.addItem(this.createRowItemWithVaadinIcon("Clean errors!", VaadinIcon.TRASH), event -> {
            verticalLayoutArea.removeAll();
            verticalLayoutArea.getElement().executeJs(SCROLLBAR_CUSTOM_STYLE);
        }).addClassName(CONTEXT_MENU_ITEM_NO_CHECKMARK);

        //Build error span
        this.span = this.buildErrorSpan();

        // Actions
        attachment = new Button(VaadinIcon.UPLOAD.create());
        attachment.addClassNames(Background.TRANSPARENT, Border.ALL, BorderColor.CONTRAST_20, BorderRadius.FULL,
                Margin.Vertical.NONE, Padding.NONE, TextColor.SECONDARY);
        attachment.addThemeVariants(ButtonVariant.LUMO_SMALL);
        attachment.setAriaLabel("Attachment");
        attachment.setTooltipText("Attachment");

        this.list = new ListCustom();
        list.setFlexWrap(FlexWrap.WRAP);
        list.setDisplay(Display.FLEX);
        list.setGap(Gap.SMALL);
        list.removeBackgroundColor();
        final Button buttonClearAll = new  Button(new Icon("lumo","cross"));
        buttonClearAll.setTooltipText("Clear files!");
        buttonClearAll.getStyle().setCursor("pointer");
        buttonClearAll.addClassNames(Margin.Left.AUTO, "close-button-hover");
        buttonClearAll.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY_INLINE);
        buttonClearAll.addClickListener(event -> {
           list.removeAll();
            mapPrefixFileNameAndContent.clear();
            list.add(buttonClearAll);
        });
        list.add(buttonClearAll);

        this.uploader = new Uploader(attachment);
        this.uploader.setUploadHandler(this.buildUploadHandler());

        this.buttonCleanFileList = new Button(VaadinIcon.TRASH.create());
        this.buttonCleanFileList.addClickListener(event -> {
            uploader.clearFileList();
        });

        validateButton = new Button("Validate", VaadinIcon.CHECK.create());
        validateButton.setIconAfterText(true);
        validateButton.addClassNames(BorderRadius.LARGE, Margin.NONE, Padding.SMALL);
        validateButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        validateButton.setAriaLabel("Validate");
        validateButton.setTooltipText("validate");
        validateButton.addClickListener(event -> {
            if (this.tenemosLaParejaCompleta()) {
                validate();
            }
        });

        Layout actions = new Layout(this.uploader, validateButton);
        actions.setJustifyContent(JustifyContent.BETWEEN);
        actions.setAlignItems(AlignItems.CENTER);

        add(list, verticalLayoutArea, actions);
    }

    private Span buildErrorSpan() {
        final Span span = new Span();
        span.getStyle().setCursor("pointer");
        Tooltip.forComponent(span).setText("Copy text");
        span.addClassNames(LumoUtility.FontSize.SMALL, TextColor.SECONDARY);
        span.getStyle().setBorderBottom(BORDER_BOTTOM_COLOR);
        return span;
    }

    private void validate() {
        String[] sortedNamesByExtension = mapPrefixFileNameAndContent.keySet()
                .stream()
                .sorted((a, b) -> a.endsWith(".xml") ? -1 : 0)
                .toArray(String[]::new);
        String xmlFileName = sortedNamesByExtension[0];
        String xsdSchemaFileName = sortedNamesByExtension[1];

        InputStream inputXml = mapPrefixFileNameAndContent.get(xmlFileName);
        InputStream inputXsdSchema = mapPrefixFileNameAndContent.get(xsdSchemaFileName);

        this.validationXsdSchemaService.validateXmlInputWithXsdSchema(inputXml, inputXsdSchema)
                .doOnError(onError -> {
                    this.executeUI(() -> {
                        log.error("Error validating: {}", xmlFileName, onError);
                        ConfirmDialogBuilder.showWarning("Validation error " + onError.getLocalizedMessage());
                        this.buildCustomSpan(onError.getLocalizedMessage());
                        this.resetInputStream();
                    });
                })
                .delayElements(Duration.ofMillis(50), Schedulers.boundedElastic())
                .subscribe(listConstaintErrors -> {
                    this.executeUI(() -> {
                        if (!listConstaintErrors.isEmpty()) {
                            log.info(listConstaintErrors);
                            this.buildCustomSpan(listConstaintErrors);
                            this.resetInputStream();
                        } else {
                            ConfirmDialogBuilder.showInformation("Validation successfully");
                            mapPrefixFileNameAndContent.clear();
                        }
                    });
                });
    }

    /**
     * Nos permite reusar el inputStream cargado en memoria
     */
    private void resetInputStream() {
        mapPrefixFileNameAndContent.forEach((k, v) -> {
            try {
                v.reset();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void buildCustomSpan(String listConstaintErrors) {
        if(listConstaintErrors.equals("ERROR:")
                ||  listConstaintErrors.equals("WARNING:") || listConstaintErrors.equals("FATAL:")) {
            span = this.buildErrorSpan();
        }
        span.setText(span.getText().concat(listConstaintErrors).concat(" "));
        Animated.animate(span, Animated.Animation.FADE_IN);
//        final SvgIcon icon = new SvgIcon(DownloadHandler.forClassResource(getClass(),
//                "/META-INF/resources/svg-images/copy-alt.svg" + "copy-alt.svg"));
//        icon.setSize("25px");
        span.addClickListener(event -> {
            UI.getCurrent().getPage().executeJs(WINDOW_COPY_TO_CLIPBOARD, span.getText());
            Notification.show("Error copied!", 2000, Notification.Position.BOTTOM_CENTER);
        });
        verticalLayoutArea.add(span);
        verticalLayoutArea.scrollIntoView(ScrollIntoViewOption.Behavior.SMOOTH);
    }

    private void executeUI(Command command) {
        super.getUI().ifPresent(ui -> {
            try {
                ui.access(command);
            } catch (UIDetachedException ex) {}
        });
    }

    private InMemoryUploadHandler buildUploadHandler() {
        return UploadHandler.inMemory((metadata, data) -> {
                    //without buffered, prevent a SAXException
                    try {
                        String incomingFileName = metadata.fileName();
                        this.validateToAggregate(incomingFileName);
                        try (final InputStream inputStream = new ByteArrayInputStream(data)) {
                            this.processFile(metadata, inputStream);
                        } catch (IOException error) {
                            log.error(error.getMessage());
                            ConfirmDialogBuilder.showWarning("File transfer failed: " + error.getMessage());
                        }
                    } catch (IllegalArgumentException e) {
                        ConfirmDialogBuilder.showWarning(e.getMessage());
                    }
                })
                .whenStart(() -> {
                    log.info("Upload started");
                    this.uploader.clearFileList();
                })
                .whenComplete((transferContext, aBoolean) -> log.info("Upload complete"));
    }

    private void processFile(final UploadMetadata uploadMetadata, InputStream inputStream) {
        final String fileName = uploadMetadata.fileName();
        long contentLength = uploadMetadata.contentLength();
        String content = "Size ⋅ " + contentLength + "KB";
        try {
            mapPrefixFileNameAndContent.put(fileName, inputStream);
        } catch (Exception ex) {
            log.error("Error al obtener inputstreams desde el vaadinRequest {}", ex.getMessage());
            ConfirmDialogBuilder.showWarning("Validation error " + ex.getMessage());
            return;
        }
        final FileListItem fileListItem = new FileListItem(fileName, content);
        list.add(fileListItem);

        ContextMenu contextMenu = this.buildContextMenu(fileListItem);
        contextMenu.addItem(this.createRowItemWithVaadinIcon("Delete", VaadinIcon.TRASH), event -> {
            event.getSource().getUI().ifPresent(ui -> {
                ConfirmDialogBuilder.showConfirmInformation("Do you want to delete: " + fileName, ui)
                        .addConfirmListener(confirm -> {
                            list.remove(fileListItem);
                            mapPrefixFileNameAndContent.remove(fileName, inputStream);
                            this.uploader.clearFileList();
                        });
            });
        }).addClassName(CONTEXT_MENU_ITEM_NO_CHECKMARK);
    }

    public ContextMenu buildContextMenu(Component target) {
        final ContextMenu contextMenu = new ContextMenu();
        contextMenu.setTarget(target);
        return contextMenu;
    }

    private HorizontalLayout createRowItemWithVaadinIcon(final String titleForSpan, VaadinIcon icon) {
        final HorizontalLayout row = new HorizontalLayout();
        final com.vaadin.flow.component.html.Span span = new com.vaadin.flow.component.html.Span(titleForSpan);
        var iconCustomSize = icon.create();
        iconCustomSize.setSize("20px");
        row.add(iconCustomSize, span);
        row.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        return row;
    }

    private void validateToAggregate(String incomingName) {
        boolean isXml = incomingName.toLowerCase().endsWith(".xml");
        boolean isXsd = incomingName.toLowerCase().endsWith(".xsd");
        // Revisamos el mapa actual
        long xmlCount = mapPrefixFileNameAndContent.keySet()
                .stream()
                .filter(k -> k.toLowerCase().endsWith(".xml")).count();
        long xsdCount = mapPrefixFileNameAndContent.keySet()
                .stream()
                .filter(k -> k.toLowerCase().endsWith(".xsd")).count();
        if (isXml && xmlCount >= 1) {
            throw new IllegalArgumentException("An XML file already exists.");
        }
        if (isXsd && xsdCount >= 1) {
            throw new IllegalArgumentException("An XSD file already exists.");
        }
    }

    private boolean tenemosLaParejaCompleta() {
        long xmlCount = mapPrefixFileNameAndContent.keySet().stream()
                .filter(k -> k.toLowerCase().endsWith(".xml")).count();
        long xsdCount = mapPrefixFileNameAndContent.keySet().stream()
                .filter(k -> k.toLowerCase().endsWith(".xsd")).count();
        return xmlCount == 1 && xsdCount == 1;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        verticalLayoutArea.getElement().executeJs(SCROLLBAR_CUSTOM_STYLE);
    }

}
