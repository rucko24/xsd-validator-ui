package com.rubn.xsdvalidator.view;

import com.github.underscore.U;
import com.rubn.xsdvalidator.util.ConfirmDialogBuilder;
import com.rubn.xsdvalidator.util.SvgFactory;
import com.rubn.xsdvalidator.util.XsdValidatorConstants;
import com.rubn.xsdvalidator.util.XsdValidatorFileUtils;
import com.vaadin.flow.component.UIDetachedException;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.clipboard.Clipboard;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.shared.Tooltip;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.theme.lumo.LumoUtility;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import static com.rubn.xsdvalidator.util.XsdValidatorConstants.CURSOR_POINTER;
import static com.rubn.xsdvalidator.util.XsdValidatorConstants.LIGHT;
import static com.rubn.xsdvalidator.util.XsdValidatorConstants.VS_DARK;
import static com.rubn.xsdvalidator.util.XsdValidatorConstants.XML;
import static com.rubn.xsdvalidator.util.XsdValidatorConstants.XSD;
import static com.rubn.xsdvalidator.view.list.FileListItem.SIZE;

/**
 * @author rubn
 */
@Slf4j
public class SimpleCodeEditorDialog extends Dialog {

    private final ProgressBar progressBar = new ProgressBar();

    @Getter
    private final SimpleCodeEditor simpleCodeEditor = new SimpleCodeEditor();

    private final String fileName;
    private final Map<String, byte[]> mapPrefixFileNameAndContent;
    private final SearchDialog searchPopover;
    private final Span sizeSpan;

    public SimpleCodeEditorDialog(String fileName, Map<String, byte[]> mapPrefixFileNameAndContent,
                                  SearchDialog searchPopover, Span sizeSpan) {
        this.fileName = fileName;
        this.mapPrefixFileNameAndContent = mapPrefixFileNameAndContent;
        this.searchPopover = searchPopover;
        this.sizeSpan = sizeSpan;

        this.buildContentDialog();
    }

    public void buildContentDialog() {
        super.setSizeFull();
        super.setCloseOnEsc(true);
        super.addClassName("simple-code-editor-dialog");
        final Button closeButton = this.buildCloseButton();
        final Icon iconBackLeft = this.buildBackIconLeft();
        super.getHeader().add(iconBackLeft);
        //Header with title
        final Span spanFileNameTitle = new Span(fileName);
        spanFileNameTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.FontWeight.BOLD);
        final SvgIcon copyButtonIcon = SvgFactory.createCopyButtonFromSvg();
        copyButtonIcon.setSize("30px");
        final Button copyButton = new Button(copyButtonIcon);
        copyButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        copyButton.addClickListener(event -> {
            Clipboard.onClick(copyButton).writeText(fileName);
            Notification.show("Copied " + fileName, 2500, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_PRIMARY);
            copyButton.setIcon(VaadinIcon.CHECK.create());
            Mono.just(copyButton)
                    .delayElement(Duration.ofMillis(1500))
                    .subscribe(btn -> {
                        btn.getUI().ifPresent(ui -> ui.access(() -> {
                            btn.setIcon(copyButtonIcon);
                        }));
                    });
        });
        super.addClosedListener(event -> copyButton.setIcon(copyButtonIcon));
        copyButton.setTooltipText("Copy filename!");
        this.progressBar.setWidth("10%");
        this.progressBar.setVisible(true);
        this.progressBar.setIndeterminate(true);

        Icon iconTheme = this.buildIconTheme();
        SvgIcon iconWordWrap = this.buildIconWordWrap();
        SvgIcon iconFormatTrigger = this.buildIconFormatTrigger();

        super.getHeader().add(spanFileNameTitle, copyButton, iconTheme, iconWordWrap,
                iconFormatTrigger,
                this.progressBar, closeButton);

        simpleCodeEditor.addValueChangeListener(event -> {
            String newContentStr = simpleCodeEditor.getContent();
            if (newContentStr != null) {
                byte[] newBytes = newContentStr.getBytes(StandardCharsets.UTF_8);
                this.mapPrefixFileNameAndContent.put(fileName, newBytes);
                simpleCodeEditor.setContent(newContentStr);
                this.sizeSpan.setText(SIZE + XsdValidatorFileUtils.formatSize(newBytes.length));
                this.searchPopover.updateItems(this.getXsdXmlFiles());
                //log.info("Content: {}", simpleCodeEditor.getContent());
            }
        });
        super.add(simpleCodeEditor);
        //Footer with update code ?
        final Button button = new Button("Update file", (event) -> {
//            log.info("Content: {}", simpleCodeEditor.getContent());
            String newContentStr = simpleCodeEditor.getContent();
            if (newContentStr != null) {
                byte[] newBytes = newContentStr.getBytes(StandardCharsets.UTF_8);
                this.mapPrefixFileNameAndContent.put(fileName, newBytes);
                simpleCodeEditor.setContent(newContentStr);
                this.sizeSpan.setText(SIZE + XsdValidatorFileUtils.formatSize(newBytes.length));
                ConfirmDialogBuilder.showInformation("Updated!");
                this.searchPopover.updateItems(this.getXsdXmlFiles());
            }
        });
        button.getStyle().setBoxShadow(XsdValidatorConstants.VAR_CUSTOM_BOX_SHADOW);
        button.addThemeVariants(ButtonVariant.LUMO_SMALL);

        super.getFooter().add(button);

    }

    public void showXmlCode() {
        super.open();
        this.progressBar.setVisible(true);
        Mono.fromSupplier(() -> new String(this.mapPrefixFileNameAndContent.get(fileName)))
                .subscribeOn(Schedulers.boundedElastic())
                .delaySubscription(Duration.ofMillis(700))
                .doOnTerminate(() -> this.access(() -> this.progressBar.setVisible(false)))
                .subscribe(content -> {
                    this.access(() -> {
                        this.simpleCodeEditor.setContent(content);
                    });
                });
    }

    public void searchLineOnEditor(String line, String fileName) {
        super.open();
        this.progressBar.setVisible(true);
        Mono.fromSupplier(() -> new String(this.mapPrefixFileNameAndContent.get(fileName)))
                .subscribeOn(Schedulers.boundedElastic())
                .delaySubscription(Duration.ofMillis(700))
                .doOnTerminate(() -> this.access(() -> this.progressBar.setVisible(false)))
                .subscribe(content -> {
                    this.access(() -> {
                        this.simpleCodeEditor.setContent(content);
                        this.simpleCodeEditor.scrollToLine(Integer.parseInt(line));
                    });
                });
    }

    private @NonNull Button buildCloseButton() {
        final Button closeButton = new Button(VaadinIcon.CLOSE.create());
        closeButton.setTooltipText("Close");
        closeButton.getStyle().setCursor(CURSOR_POINTER);
        closeButton.addClassName(LumoUtility.Margin.Left.AUTO);
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        closeButton.addClickListener(e -> super.close());
        return closeButton;
    }

    private Icon buildIconTheme() {
        final Icon iconTheme = VaadinIcon.ADJUST.create();
        Tooltip.forComponent(iconTheme)
                .withText("dark - ligth");
        iconTheme.getStyle().setCursor(CURSOR_POINTER);
        iconTheme.addClassName(LumoUtility.FontSize.SMALL);
        iconTheme.addClickListener(event -> {
            String theme = simpleCodeEditor.getTheme().equals(VS_DARK) ? LIGHT : VS_DARK;
            simpleCodeEditor.setTheme(theme);
        });
        return iconTheme;
    }

    private @NonNull Icon buildBackIconLeft() {
        final Icon iconClose = VaadinIcon.ARROW_LEFT.create();
        Tooltip.forComponent(iconClose).setText("Back");
        iconClose.getStyle().setCursor(CURSOR_POINTER);
        iconClose.addClassName(LumoUtility.TextColor.TERTIARY);
        iconClose.addClickListener(event -> super.close());
        return iconClose;
    }

    private SvgIcon buildIconWordWrap() {
        final SvgIcon iconTheme = SvgFactory.createIconFromSvg("word-wrap.svg", "25px", null);
        Tooltip.forComponent(iconTheme)
                .withText("word wrap");
        iconTheme.getStyle().setCursor(CURSOR_POINTER);
        iconTheme.addClickListener(event -> simpleCodeEditor.setWordWrap(!simpleCodeEditor.getWordWrap()));
        return iconTheme;
    }

    private SvgIcon buildIconFormatTrigger() {
        final SvgIcon iconTheme = SvgFactory.createIconFromSvg("format-code.svg", "25px", null);
        Tooltip.forComponent(iconTheme)
                .withText("Format");
        iconTheme.getStyle().setCursor(CURSOR_POINTER);
        iconTheme.addClickListener(event -> {
            String sfileName = new String(this.mapPrefixFileNameAndContent.get(fileName));
            String formatedXml = U.formatXml(sfileName);
            this.simpleCodeEditor.setContent(formatedXml);
        });
        return iconTheme;
    }

    private List<String> getXsdXmlFiles() {
        return mapPrefixFileNameAndContent.keySet()
                .stream()
                .filter(name -> name.toLowerCase().endsWith(XSD) || name.toLowerCase().endsWith(XML))
                .sorted()
                .toList();
    }

    private void access(Command command) {
        super.getUI().ifPresent(ui -> {
            try {
                ui.access(command);
            } catch (UIDetachedException ex) {
            }
        });
    }

}
