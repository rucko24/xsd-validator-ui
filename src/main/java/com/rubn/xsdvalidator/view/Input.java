package com.rubn.xsdvalidator.view;

import com.rubn.xsdvalidator.records.DecompressedFile;
import com.rubn.xsdvalidator.records.ErrorEventRecord;
import com.rubn.xsdvalidator.service.DecompressionService;
import com.rubn.xsdvalidator.service.ValidationXsdSchemaService;
import com.rubn.xsdvalidator.util.ConfirmDialogBuilder;
import com.rubn.xsdvalidator.util.Layout;
import com.rubn.xsdvalidator.util.SvgFactory;
import com.rubn.xsdvalidator.util.XsdValidatorConstants;
import com.rubn.xsdvalidator.util.XsdValidatorFileUtils;
import com.rubn.xsdvalidator.view.list.CustomList;
import com.rubn.xsdvalidator.view.list.FileListItem;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.UIDetachedException;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.clipboard.Clipboard;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.AbstractIcon;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.shared.Tooltip;
import com.vaadin.flow.internal.JacksonCodec;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.DownloadResponse;
import com.vaadin.flow.theme.lumo.LumoUtility;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.vaadin.firitin.components.upload.UploadFileHandler;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import tools.jackson.databind.JsonNode;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.rubn.xsdvalidator.util.XsdValidatorConstants.CONTEXT_MENU_ITEM_NO_CHECKMARK;
import static com.rubn.xsdvalidator.util.XsdValidatorConstants.CURSOR_POINTER;
import static com.rubn.xsdvalidator.util.XsdValidatorConstants.DELETE_ITEM;
import static com.rubn.xsdvalidator.util.XsdValidatorConstants.JS_COMMAND;
import static com.rubn.xsdvalidator.util.XsdValidatorConstants.MENU_ITEM_NO_CHECKMARK;
import static com.rubn.xsdvalidator.util.XsdValidatorConstants.RETURN_TEXT_ERROR;
import static com.rubn.xsdvalidator.util.XsdValidatorConstants.SCROLLBAR_CUSTOM_STYLE;
import static com.rubn.xsdvalidator.util.XsdValidatorConstants.WINDOW_COPY_TO_CLIPBOARD;
import static com.rubn.xsdvalidator.util.XsdValidatorConstants.XML;
import static com.rubn.xsdvalidator.util.XsdValidatorConstants.XSD;

/**
 * @author rubn
 */
@Slf4j
public class Input extends Layout implements BeforeEnterObserver {

    public final ProgressBar progressBarFileListItem;
    public static final String ICON_SIZE_IN_PX = "15px";
    private final Map<String, byte[]> mapPrefixFileNameAndContent = new ConcurrentHashMap<>();
    private final AtomicInteger counterSpanId = new AtomicInteger(0);
    private final List<String> allErrorsList = new CopyOnWriteArrayList<>();
    private final VerticalLayout verticalLayoutArea;
    private final Button attachment;
    private final Button validateButton;
    private final CustomList customList;
    private final Anchor anchorDownloadErrors;
    /**
     * Service
     */
    private final ValidationXsdSchemaService validationXsdSchemaService;
    private final DecompressionService decompressionService;
    private final UploadFileHandler uploadFileHandler;
    /**
     * Mutable fields
     */
    private Span spanWordError;
    private String selectedXmlFile;
    private String selectedMainXsd;
    @Getter
    private SearchDialog searchDialog;
    private Disposable disposableStreaming;

    public Input(final ValidationXsdSchemaService validationXsdSchemaService,
                 final DecompressionService decompressionService,
                 final ProgressBar progressBarFileListItem) {
        this.validationXsdSchemaService = validationXsdSchemaService;
        this.decompressionService = decompressionService;
        this.progressBarFileListItem = progressBarFileListItem;
        addClassName("input-layout");
        // Text area
        verticalLayoutArea = new VerticalLayout();
        verticalLayoutArea.addClassNames("vertical-area");
        verticalLayoutArea.getElement().executeJs(SCROLLBAR_CUSTOM_STYLE);

        // attachment upload button
        attachment = new Button(VaadinIcon.UPLOAD.create());
        attachment.addClassName("attachment");
        attachment.addThemeVariants(ButtonVariant.LUMO_SMALL);
        attachment.setAriaLabel("Attachment");
        attachment.setTooltipText("Attachment");

        this.progressBarFileListItem.setVisible(false);
        this.progressBarFileListItem.setIndeterminate(true);
        this.progressBarFileListItem.addClassName("progressbar-file-list-items");

        this.customList = new CustomList();
        this.anchorDownloadErrors = new Anchor();
        this.anchorDownloadErrors.setEnabled(false);
        MenuBar menuBar = this.buildMenuBarOptions();

        final Div divHeader = new Div(customList, menuBar);
        divHeader.addClassName("div-files-wrapper");

        validateButton = new Button("Validate", VaadinIcon.CHECK.create());
        validateButton.addClassName("validate-button");
        validateButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        validateButton.setAriaLabel("Validate");
        validateButton.setTooltipText("validate");
        validateButton.setDisableOnClick(true);
        validateButton.addClickListener(event -> {
            if (event.isFromClient()) {
                if (Objects.isNull(this.selectedXmlFile) || this.selectedXmlFile.isBlank()) {
                    showMessageFailedToStartValidation();
                    return;
                }
                if (Objects.isNull(this.selectedMainXsd) || this.selectedMainXsd.isBlank()) {
                    showMessageFailedToStartValidation();
                    return;
                }
                this.validateXmlInputWithXsdSchema();
            }
        });

        uploadFileHandler = this.buildUploadFileHandler(attachment);
        Layout actions = new Layout(uploadFileHandler, validateButton);
        actions.addClassName("actions");

        this.searchDialog = this.buildDialog(List.of());

        add(divHeader, verticalLayoutArea, actions, searchDialog);
    }

    public SearchDialog buildDialog(List<String> xsdXmlFiles) {
        return new SearchDialog(xsdXmlFiles, this.selectedMainXsd, this.selectedXmlFile,
                this::synchronizeListFromPopover, mapPrefixFileNameAndContent);
    }

    private List<String> getXsdXmlFiles() {
        return mapPrefixFileNameAndContent.keySet()
                .stream()
                .filter(name -> name.toLowerCase().endsWith(XSD) || name.toLowerCase().endsWith(XML))
                .sorted()
                .toList();
    }

    private void synchronizeListFromPopover(Set<String> selectedFiles) {
        // Obtenemos todos los items actuales de la lista visual
        List<FileListItem> allItems = customList.getChildren()
                .filter(c -> c instanceof FileListItem)
                .map(c -> (FileListItem) c)
                .toList();

        for (FileListItem item : allItems) {
            String fileName = item.getFileName();
            boolean shouldBeSelected = selectedFiles.contains(fileName);

            if (shouldBeSelected) {
                // CASO A: Está seleccionado en el Popover
                if (!item.isChecked()) {
                    item.setSelected(true); // Lo marcamos visualmente

                    // Efectos visuales (Mover arriba y Scroll)
                    customList.addComponentAsFirst(item);
                    item.getElement().executeJs("this.scrollIntoView({block: 'center', behavior: 'smooth'});");

                    // Actualizamos variables de estado locales si es necesario
                    if (fileName.toLowerCase().endsWith(XML)) {
                        this.selectedXmlFile = fileName;
                    } else {
                        this.selectedMainXsd = fileName;
                    }
                }
            } else {
                // CASO B: NO está en el Popover (fue deseleccionado) -> Lo desmarcamos
                if (item.isChecked()) {
                    item.setSelected(false);
                    // Limpiamos variables de estado locales
                    if (fileName.equals(this.selectedXmlFile)) this.selectedXmlFile = null;
                    if (fileName.equals(this.selectedMainXsd)) this.selectedMainXsd = null;
                }
            }
        }
    }

    private void showMessageFailedToStartValidation() {
        ConfirmDialogBuilder.showWarning("Failed to start validation, check uploaded files...");
        validateButton.setEnabled(true);
    }

    private MenuBar buildMenuBarOptions() {
        final Button buttonOptions = new Button(VaadinIcon.ELLIPSIS_V.create());
        buttonOptions.getStyle().setCursor(CURSOR_POINTER);
        buttonOptions.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);

        final MenuBar menuBarGridOptions = new MenuBar();
        menuBarGridOptions.addClassName("fixed-menu");
        var tooltip = Tooltip.forComponent(buttonOptions);
        tooltip.setPosition(Tooltip.TooltipPosition.TOP);
        tooltip.setText("Options");
        menuBarGridOptions.addThemeVariants(MenuBarVariant.LUMO_TERTIARY_INLINE, MenuBarVariant.LUMO_CONTRAST);

        final MenuItem itemEllipsis = menuBarGridOptions.addItem("");
        itemEllipsis.add(buttonOptions);

        this.anchorDownloadErrors.setClassName("anchor-downloader");
        this.anchorDownloadErrors.setHref(DownloadHandler.fromInputStream((event) -> {
            try {
                String errors = this.textProcessing();
//                log.info("Errors menu item: {}", errors);
                byte[] byteArray = errors.getBytes(StandardCharsets.UTF_8);
                String fileNameError = "validation-errors-" + System.currentTimeMillis() + ".txt";
                event.getResponse().setHeader("Content-Disposition", "attachment; filename=\"" + fileNameError + "\"");
                return new DownloadResponse(
                        new ByteArrayInputStream(byteArray),
                        fileNameError,
                        MediaType.TEXT_PLAIN_VALUE,
                        byteArray.length
                );
            } catch (Exception e) {
                log.error("Error downloading errors: ", e);
                ConfirmDialogBuilder.showWarning("Error downloading logs");
                return DownloadResponse.error(500);
            }
        }));

        final HorizontalLayout rowDownload = this.buildRowItemWithIcon("Download errors", VaadinIcon.DOWNLOAD.create(), "18px");
        this.anchorDownloadErrors.addComponentAsFirst(rowDownload);
        itemEllipsis.getSubMenu().addItem(this.anchorDownloadErrors).addClassNames(MENU_ITEM_NO_CHECKMARK);

        SvgIcon svgIcon = SvgFactory.createCopyButtonFromSvg();
        svgIcon.getStyle().setMarginLeft("-5px");
        svgIcon.getStyle().setMarginBottom("-6px");
        var row = this.buildRowItemWithIcon("Copy all text", svgIcon, "25px");
        row.getStyle().setGap("0.2em");
        row.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.BASELINE);
        MenuItem itemDelete = itemEllipsis.getSubMenu().addItem(row, event -> {
            if (!this.allErrorsList.isEmpty()) {
                String errors = this.textProcessing();
                UI.getCurrent().getPage().executeJs(WINDOW_COPY_TO_CLIPBOARD, errors);
                Notification.show("Copied!", 2000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_PRIMARY);
            }
        });
        itemDelete.addClassNames(MENU_ITEM_NO_CHECKMARK);
        itemDelete.getStyle().setPaddingBottom("10px");

        itemEllipsis.getSubMenu().addSeparator();

        itemEllipsis.getSubMenu().addItem(this.buildRowItemWithIcon("Clear files and text",
                VaadinIcon.TRASH.create(), "18px"), event -> {
            this.clearAllData();
        }).addClassNames(MENU_ITEM_NO_CHECKMARK, DELETE_ITEM);

        itemEllipsis.getSubMenu()
                .getItems()
                .forEach(item -> item.getStyle().setCursor(CURSOR_POINTER));

        return menuBarGridOptions;
    }

    public void clearAllData() {
        verticalLayoutArea.removeAll();
        verticalLayoutArea.getElement().executeJs(SCROLLBAR_CUSTOM_STYLE);
        customList.removeAll();
        mapPrefixFileNameAndContent.clear();
        allErrorsList.clear();
        this.counterSpanId.set(0);
        this.anchorDownloadErrors.setEnabled(false);
        this.selectedMainXsd = StringUtils.EMPTY;
        this.selectedXmlFile = StringUtils.EMPTY;
        this.searchDialog.updateItems(this.getXsdXmlFiles());
    }

    /**
     * Thanks to {@link <a href="https://github.com/mstahv">mstahv<a>} for helping to fix the error when uploading a large
     * file and several error notifications were displayed.
     *
     * @param uploadButtonAttachment
     * @return UploadFileHandler
     */
    private UploadFileHandler buildUploadFileHandler(final Button uploadButtonAttachment) {
        return new UploadFileHandler((InputStream inputStream, UploadFileHandler.FileDetails metadata) -> {
            this.accessUI(() -> this.progressBarFileListItem.setVisible(true));
            if (XsdValidatorFileUtils.isNotSupportedExtension(metadata.fileName())) {
                // Another improvement place for UploadFileHandler here, would be great to
                // have reference for component/ui in handler...
                log.warn("File ignored during upload: {}", metadata.fileName());
                accessUI(() -> {
                    ConfirmDialogBuilder.showWarning("File not supported! " + metadata.fileName());
                    //uploadFileHandler.clearFiles(); // manually clear files after error as UFH doesn't seem to do it
                    this.progressBarFileListItem.setVisible(false);
                });
                // This sends 500 to browser and stop reading bytes
                //throw new IllegalArgumentException("File not supported!");
                return () -> {};
            }
            String fileName = metadata.fileName();
            byte[] bytes;
            List<DecompressedFile> decompressedFiles;
            if (this.decompressionService.isCompressedFile(fileName)) {
                decompressedFiles = this.decompressionService.decompressFile(fileName, inputStream,
                        (onError -> accessUI(() -> this.onError(onError))));
                bytes = null;
            } else {
                bytes = inputStream.readAllBytes();
                decompressedFiles = null;
            }
            return () -> {
                if (decompressedFiles != null) {
                    decompressedFiles.forEach(decompressedFile -> {
                        this.processFile(metadata, decompressedFile.content(), true, decompressedFile);
                    });
                } else {
                    this.processFile(metadata, bytes, false, new DecompressedFile(null, null, 0L));
                }
                this.progressBarFileListItem.setVisible(false);
                this.searchDialog.updateItems(this.getXsdXmlFiles());
            };
        })
                .withClearAutomatically(true)
                .withAddedClassName("upload-xml-xsd", LumoUtility.Padding.XSMALL, LumoUtility.Width.FULL,
                        LumoUtility.Margin.Right.MEDIUM, LumoUtility.Margin.Left.NONE)
                .withDragAndDrop(true)
                .withDropLabelIcon(new Span())
                .withDropLabel(new Span("Drop files here, only support: " + XsdValidatorConstants.SUPPORT_FILES))
                .withUploadButton(uploadButtonAttachment)
                .withAllowMultiple(true)
                .chooseFolders();
    }

    private void validateXmlInputWithXsdSchema() {
        byte[] inputXml = this.mapPrefixFileNameAndContent.get(this.selectedXmlFile);
        byte[] inputXsdSchema = this.mapPrefixFileNameAndContent.get(this.selectedMainXsd);

        this.disposableStreaming = this.validationXsdSchemaService.validateXmlInputWithXsdSchema(inputXml, inputXsdSchema, mapPrefixFileNameAndContent)
                .flatMapIterable(this.validationXsdSchemaService::detectWords)
                .switchIfEmpty(Mono.defer(() -> {
                    this.accessUI(() -> ConfirmDialogBuilder.showInformation("Validation successful!!!"));
                    return Mono.empty();
                }))
                .doOnError(onError -> this.onError(onError.getLocalizedMessage()))
                .delayElements(Duration.ofMillis(50), Schedulers.boundedElastic())
                .doOnTerminate(() -> {
                    log.info("Terminated!");
                    this.accessUI(() -> validateButton.setEnabled(true));
                })
                .subscribe(word -> {
                    super.getUI().ifPresent(ui -> {
                        ui.access(() -> {
                            if (!word.isEmpty()) {
                                this.buildErrorSpanAndUpdate(word);
                            }
                        });
                    });
                });
    }

    private void enableDownloadErrorsInOptions() {
        this.anchorDownloadErrors.setEnabled(!allErrorsList.isEmpty());
    }

    private void onError(String onError) {
        this.accessUI(() -> {
            this.allErrorsList.add(StringUtils.LF);
            this.allErrorsList.add(onError);
            this.spanWordError = this.buildErrorSpan();
            verticalLayoutArea.add(this.spanWordError);
            this.spanWordError.getElement().executeJs(JS_COMMAND, onError);
            this.enableDownloadErrorsInOptions();
        });
    }

    @EventListener
    public void listener(ErrorEventRecord errorEventRecord) {
        this.accessUI(() -> {
            this.allErrorsList.add(StringUtils.LF);
            this.allErrorsList.add(errorEventRecord.error());
            this.spanWordError = this.buildErrorSpan();
            verticalLayoutArea.add(this.spanWordError);
            this.spanWordError.getElement().executeJs(JS_COMMAND, errorEventRecord.error());
            this.enableDownloadErrorsInOptions();
        });
    }

    private void buildErrorSpanAndUpdate(String word) {
        //Obliga a crear otro span mas abajo en el VerticalLayout
        if (word.equals("ERROR:") || word.equals("WARNING:") || word.equals("FATAL:")) {
            this.spanWordError = this.buildErrorSpan();
            verticalLayoutArea.add(this.spanWordError);
            this.allErrorsList.add(StringUtils.LF);
        }
        this.allErrorsList.add(word);
        this.spanWordError.getElement().executeJs(JS_COMMAND, word);
        this.enableDownloadErrorsInOptions();
    }

    private Span buildErrorSpan() {
        final Span span = new Span();
        span.setId(String.valueOf(this.counterSpanId.incrementAndGet()));
        Tooltip.forComponent(span).setText("Copy text");
        span.addClassName("parent-span");
        span.addClickListener(event -> {
            span.getId().ifPresent(id -> {
                span.getElement().executeJs(RETURN_TEXT_ERROR
                ).then(String.class, errorText -> {
                    Clipboard.onClick(span).writeText(errorText);
                    UI.getCurrent().getPage().executeJs(WINDOW_COPY_TO_CLIPBOARD, errorText);
                    Notification.show("Error #" + id + " copied!", 2000, Notification.Position.MIDDLE)
                            .addThemeVariants(NotificationVariant.LUMO_PRIMARY);
                });
            });
        });
        ContextMenu contextMenuSpan = this.buildContextMenu(span);
        contextMenuSpan.addItem(this.buildRowItemWithIcon("Search error", VaadinIcon.SEARCH_PLUS.create(),
                ICON_SIZE_IN_PX), event -> {
            span.getElement().executeJs(RETURN_TEXT_ERROR)
                    .toCompletableFuture()
                    .whenCompleteAsync((JsonNode jsonNode, Throwable throwable) -> {
                        if (Objects.isNull(throwable)) {
                            this.accessUI(() -> {
                                final String errorText = JacksonCodec.decodeAs(jsonNode, String.class);
                                final String lineError = this.extractLineNumber(errorText);
                                this.customList.getChildren()
                                        .filter(component -> component instanceof FileListItem)
                                        .map(component -> (FileListItem) component)
                                        .filter(fileListItem -> fileListItem.getFileName().equals(this.selectedXmlFile))
                                        .findFirst()
                                        .ifPresent(fileListItem -> {
                                            fileListItem.searchForTextLineInTheEditor(lineError, this.selectedXmlFile);
                                            log.info("Editor is opened!");
                                        });
                            });
                        } else {
                            this.accessUI(() -> {
                                ConfirmDialogBuilder.showWarning("Error al intentar editar fichero: " + this.selectedXmlFile);
                            });
                        }
                    }, Executors.newCachedThreadPool());
        }).addClassName(CONTEXT_MENU_ITEM_NO_CHECKMARK);

        contextMenuSpan.addItem(this.buildRowItemWithIcon("Clear errors", VaadinIcon.TRASH.create(), ICON_SIZE_IN_PX), event -> {
            verticalLayoutArea.removeAll();
            this.counterSpanId.set(0);
            verticalLayoutArea.getElement().executeJs(SCROLLBAR_CUSTOM_STYLE);
        }).addClassNames(CONTEXT_MENU_ITEM_NO_CHECKMARK, DELETE_ITEM);

        return span;
    }

    public String extractLineNumber(String errorText) {
        Pattern pattern = Pattern.compile("\\[Linea]\\s*\\[(\\d+)]");
        Matcher matcher = pattern.matcher(errorText);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "0";
    }

    private void processFile(final UploadFileHandler.FileDetails metadata, byte[] readedBytesFromFile, boolean isCompressed,
                             DecompressedFile decompressedFile) {

        final String fileName = isCompressed ? decompressedFile.fileName() : metadata.fileName();
        long contentLength = isCompressed ? decompressedFile.content().length : metadata.contentLenght();
        mapPrefixFileNameAndContent.put(fileName, readedBytesFromFile);

        final FileListItem fileListItem = this.buildFileListItem(fileName, contentLength);
        final ContextMenu contextMenu = fileListItem.buildContextMenuItem(fileListItem);
        // Delete option
        contextMenu.getItems().get(2)
                .addClickListener(event -> this.showConfirmDialog(event, fileName, fileListItem));

        // lumo cross button delete option
        fileListItem.getButtonClose().addClickListener(event -> this.showConfirmDialog(event, fileName, fileListItem));
        customList.add(fileListItem);

    }

    private void showConfirmDialog(ClickEvent<?> event, String fileName, FileListItem fileListItem) {
        event.getSource().getUI().ifPresent(ui -> {
            ConfirmDialogBuilder.showConfirmInformation("Do you want to delete: " + fileName, ui)
                    .addConfirmListener(confirm -> {
                        this.deleteFileListItem(fileListItem, fileName);
                    });
        });
    }

    private void deleteFileListItem(FileListItem fileListItem, String fileName) {
        customList.remove(fileListItem);
        mapPrefixFileNameAndContent.remove(fileName);
        log.info("remove file {}", fileName);
        // Si borramos el que estaba seleccionado, limpiar la variable
        if (fileName.equals(selectedMainXsd)) {
            selectedMainXsd = StringUtils.EMPTY;
        }
        if (fileName.equals(this.selectedXmlFile)) {
            selectedXmlFile = StringUtils.EMPTY;
        }
        this.searchDialog.updateItems(this.getXsdXmlFiles());
    }

    private FileListItem buildFileListItem(String fileName, long contentLength) {
        BiConsumer<FileListItem, Boolean> onItemSelected;
        String lowerName = fileName.toLowerCase();
        if (lowerName.endsWith(XML)) {
            onItemSelected = createSelectionListener(XML,
                    value -> this.selectedXmlFile = value, // Setter
                    () -> this.selectedXmlFile // Getter
            );
        } else {
            onItemSelected = createSelectionListener(XSD,
                    value -> this.selectedMainXsd = value, // Setter
                    () -> this.selectedMainXsd // Getter
            );
        }

        return new FileListItem(fileName, contentLength, onItemSelected, mapPrefixFileNameAndContent, this.searchDialog);
    }

    private BiConsumer<FileListItem, Boolean> createSelectionListener(String extension, Consumer<String> stateSetter,
                                                                      Supplier<String> stateGetter) {

        return (item, isChecked) -> {
            if (isChecked) { // XML
                stateSetter.accept(item.getFileName());
                this.clearOtherSelections(item, extension);
                //Cuando el XML es checkiado
                //usaro para leer la linea de error y abrir el MonacoEditor
            } else { // XSD
                String currentSelection = stateGetter.get();
                if (item.getFileName().equals(currentSelection)) {
                    stateSetter.accept(StringUtils.EMPTY);
                }
            }
            if (this.searchDialog != null) {
                this.searchDialog.updateSelectionFromOutside(this.selectedMainXsd, this.selectedXmlFile);
            }
        };
    }

    /**
     * Recorre la lista y desmarca los items que no sean el actual
     * Y que coincidan con la extensión (para no desmarcar XMLs si selecciono XSDs)
     */
    private void clearOtherSelections(FileListItem currentItem, String extensionFilter) {
        customList.getChildren().forEach(component -> {
            if (component instanceof FileListItem item) {
                if (item != currentItem && item.isChecked() && item.getFileName().toLowerCase().endsWith(extensionFilter)) {
                    item.setSelected(false);
                }
            }
        });
    }

    public ContextMenu buildContextMenu(Component target) {
        final ContextMenu contextMenu = new ContextMenu();
        contextMenu.setTarget(target);
        return contextMenu;
    }

    private HorizontalLayout buildRowItemWithIcon(final String titleForSpan, AbstractIcon<?> icon, String iconSizeInPx) {
        final com.vaadin.flow.component.html.Span span = new com.vaadin.flow.component.html.Span(titleForSpan);
        final HorizontalLayout row = new HorizontalLayout(icon, span);
        row.setId("row-with-icon");
        row.setSpacing(false);
        icon.setSize(iconSizeInPx);
        row.addClassNames(LumoUtility.Gap.SMALL, LumoUtility.FontSize.SMALL);
        return row;
    }

    private String textProcessing() {
        return String.join(" ", this.allErrorsList
                        .stream()
                        .map(item -> item.equals(StringUtils.LF) ? item.concat(StringUtils.LF) : item)
                        .toList())
                .trim();
    }

    private void accessUI(Command command) {
        super.getUI().ifPresent(ui -> {
            try {
                if (isAttached()) {
                    ui.access(command);
                }
            } catch (UIDetachedException ex) {
            }
        });
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        if (this.disposableStreaming != null) {
            this.disposableStreaming.dispose();
        }
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        verticalLayoutArea.getElement().executeJs(SCROLLBAR_CUSTOM_STYLE);
    }

}
