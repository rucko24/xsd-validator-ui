package com.rubn.xsdvalidator.view;

import com.rubn.xsdvalidator.service.ValidationXsdSchemaService;
import com.rubn.xsdvalidator.util.ConfirmDialogBuilder;
import com.rubn.xsdvalidator.util.Layout;
import com.rubn.xsdvalidator.util.XsdValidatorConstants;
import com.rubn.xsdvalidator.view.list.FileListItem;
import com.rubn.xsdvalidator.view.list.ListCustom;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ScrollIntoViewOption;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.UIDetachedException;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
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
import org.apache.commons.lang3.StringUtils;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.rubn.xsdvalidator.util.XsdValidatorConstants.BORDER_BOTTOM_COLOR;
import static com.rubn.xsdvalidator.util.XsdValidatorConstants.CONTEXT_MENU_ITEM_NO_CHECKMARK;
import static com.rubn.xsdvalidator.util.XsdValidatorConstants.MENU_ITEM_NO_CHECKMARK;
import static com.rubn.xsdvalidator.util.XsdValidatorConstants.SCROLLBAR_CUSTOM_STYLE;
import static com.rubn.xsdvalidator.util.XsdValidatorConstants.VAR_CUSTOM_BOX_SHADOW;
import static com.rubn.xsdvalidator.util.XsdValidatorConstants.WINDOW_COPY_TO_CLIPBOARD;

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
    private Span errorSpan;

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
        verticalLayoutArea.getStyle().setCursor(XsdValidatorConstants.CURSOS_POINTER);
        verticalLayoutArea.setHeight("350px");
        verticalLayoutArea.addClassNames(Padding.NONE, Width.FULL);
        verticalLayoutArea.getStyle().setOverflow(Style.Overflow.AUTO);
        verticalLayoutArea.getElement().executeJs(SCROLLBAR_CUSTOM_STYLE);
        final ContextMenu contextMenu = this.buildContextMenu(verticalLayoutArea);
        contextMenu.addItem(this.createRowItemWithVaadinIcon("Clean errors!", VaadinIcon.TRASH), event -> {
            verticalLayoutArea.removeAll();
            verticalLayoutArea.getElement().executeJs(SCROLLBAR_CUSTOM_STYLE);
        }).addClassName(CONTEXT_MENU_ITEM_NO_CHECKMARK);

//        final SvgIcon iconCopyAll = SvgFactory.createCopyButtonFromSvg();
//        iconCopyAll.setSize("30px");
//        iconCopyAll.getStyle().setCursor(XsdValidatorConstants.CURSOS_POINTER);
//        iconCopyAll.addClassNames(Margin.Left.AUTO, "icon-hover-effect");
//        iconCopyAll.addClickListener(event -> {
//            UI.getCurrent().getPage().executeJs(WINDOW_COPY_TO_CLIPBOARD, errorSpan.getText());
//            Notification.show("Error copied!", 2000, Notification.Position.MIDDLE)
//                    .addThemeVariants(NotificationVariant.LUMO_PRIMARY);
//        });

        //Build error span
        this.errorSpan = this.buildErrorSpan();

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

        final Button buttonClearAll = new Button(VaadinIcon.ELLIPSIS_V.create());
        buttonClearAll.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);

        final MenuBar menuBarGridOptions = new MenuBar();
        Tooltip.forComponent(menuBarGridOptions).setText("Options");
        menuBarGridOptions.setThemeName("tertiary-inline contrast");
        menuBarGridOptions.addThemeVariants(MenuBarVariant.LUMO_SMALL);
        menuBarGridOptions.addClassName(Margin.Left.AUTO);

        final MenuItem itemEllipsis = menuBarGridOptions.addItem("");
        itemEllipsis.add(buttonClearAll);
        list.add(menuBarGridOptions);

        itemEllipsis.getSubMenu().addItem(this.createRowItemWithVaadinIcon("Clean errors!", VaadinIcon.TRASH), event -> {
            verticalLayoutArea.removeAll();
            verticalLayoutArea.getElement().executeJs(SCROLLBAR_CUSTOM_STYLE);
        }).addClassName(MENU_ITEM_NO_CHECKMARK);

        itemEllipsis.getSubMenu().addItem(this.createRowItemWithVaadinIcon("Clear files!", VaadinIcon.TRASH), event -> {
            list.removeAll();
            mapPrefixFileNameAndContent.clear();
            list.add(menuBarGridOptions);
        }).addClassName(MENU_ITEM_NO_CHECKMARK);

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
            if (!this.checkUploadedFiles()) {
                ConfirmDialogBuilder.showWarning("No se ah podido iniciar la validacion, revisa los ficheros subidos");
                return;
            }
            this.validateXmlInputWithXsdSchema();
        });

        Layout actions = new Layout(this.uploader, validateButton);
        actions.setJustifyContent(JustifyContent.BETWEEN);
        actions.setAlignItems(AlignItems.CENTER);

        add(list, verticalLayoutArea, actions);
    }

    private InMemoryUploadHandler buildUploadHandler() {
        return UploadHandler.inMemory((metadata, data) -> {
                    //without buffered, prevent a SAXException
                    try (final InputStream inputStream = new ByteArrayInputStream(data)) {

                        this.processFile(metadata, inputStream);

                    } catch (IOException error) {
                        log.error(error.getMessage());
                        ConfirmDialogBuilder.showWarning("File transfer failed: " + error.getMessage());
                    }
                })
                .whenStart(() -> {
                    log.info("Upload started");
                    this.uploader.clearFileList();
                })
                .whenComplete((transferContext, aBoolean) -> log.info("Upload complete"));
    }

    private void validateXmlInputWithXsdSchema() {
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
                .subscribe(word -> {
                    this.executeUI(() -> {
                        if (!word.isEmpty()) {
                            log.info(word);
                            this.buildCustomSpan(word);
                            this.resetInputStream();
                        } else {
                            ConfirmDialogBuilder.showInformation("Validation successfully");
                            mapPrefixFileNameAndContent.clear();
                        }
                    });
                });
    }

    private void buildCustomSpan(String word) {
        //Obliga a crear otro span mas abajo en el VerticalLayout
        if (word.equals("ERROR:") || word.equals("WARNING:") || word.equals("FATAL:")) {
            this.errorSpan = this.buildErrorSpan();
        }
        String jsCommand = "this.insertAdjacentHTML('beforeend', '<span class=\"error-word\">' + $0 + '</span> ')";
        this.errorSpan.getElement().executeJs(jsCommand, errorSpan.getText().concat(word).concat(StringUtils.SPACE));
    }

    private Span buildErrorSpan() {
        final Span span = new Span();
        span.getStyle().setCursor(XsdValidatorConstants.CURSOS_POINTER);
        Tooltip.forComponent(span).setText("Copy text");
        span.addClassNames(LumoUtility.FontSize.SMALL, TextColor.SECONDARY);
        span.getStyle().setBorderBottom(BORDER_BOTTOM_COLOR);
        span.addClickListener(event -> {
            UI.getCurrent().getPage().executeJs(WINDOW_COPY_TO_CLIPBOARD, span.getText());
            Notification.show("Error copied!", 2000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_PRIMARY);
        });
        verticalLayoutArea.add(span);
        verticalLayoutArea.scrollIntoView(ScrollIntoViewOption.Behavior.SMOOTH);
        return span;
    }

    /**
     * Nos permite reusar el inputStream cargado en memoria
     */
    private void resetInputStream() {
        mapPrefixFileNameAndContent.forEach((k, v) -> {
            try {
                v.reset();
            } catch (IOException e) {
                log.error("resetInputStream {}", e.getMessage());
            }
        });
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
        row.setSpacing(false);
        row.addClassNames(LumoUtility.Gap.SMALL);
        final com.vaadin.flow.component.html.Span span = new com.vaadin.flow.component.html.Span(titleForSpan);
        var iconCustomSize = icon.create();
        iconCustomSize.setSize("20px");
        row.add(iconCustomSize, span);
        row.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.START);
        return row;
    }

    /**
     * Para dos ficheros, xsd o xml bien, pero para varias "xsd" no
     * <p>
     * A veces una xsd puede importar otras más
     *
     * @return boolean
     *
     */
    private boolean checkUploadedFiles() {
        long xmlCount = mapPrefixFileNameAndContent.keySet().stream()
                .filter(k -> k.toLowerCase().endsWith(".xml")).count();
        long xsdCount = mapPrefixFileNameAndContent.keySet().stream()
                .filter(k -> k.toLowerCase().endsWith(".xsd")).count();
        return xmlCount >= 1 && xsdCount >= 1;
    }

    private void executeUI(Command command) {
        super.getUI().ifPresent(ui -> {
            try {
                ui.access(command);
            } catch (UIDetachedException ex) {
            }
        });
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        verticalLayoutArea.getElement().executeJs(SCROLLBAR_CUSTOM_STYLE);
    }

}
