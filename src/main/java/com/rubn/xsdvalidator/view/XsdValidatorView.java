package com.rubn.xsdvalidator.view;

import com.rubn.xsdvalidator.service.DecompressionService;
import com.rubn.xsdvalidator.service.ValidationXsdSchemaService;
import com.rubn.xsdvalidator.util.XsdValidatorConstants;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.Shortcuts;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.shared.Tooltip;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import com.vaadin.flow.theme.lumo.LumoUtility;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;

import static com.rubn.xsdvalidator.util.XsdValidatorConstants.CURSOR_POINTER;

@Log4j2
@UIScope
@SpringComponent
@Route("")
@RouteAlias("xsd-validator")
@PageTitle("xsd-validator")
@Menu(order = 1, icon = "vaadin:clipboard-check", title = "XSD Validator")
class XsdValidatorView extends Main {

    private final ProgressBar progressBar = new ProgressBar();

    public XsdValidatorView(final ValidationXsdSchemaService validationXsdSchemaService,
                            final DecompressionService decompressionService) {
        setSizeFull();
        getStyle().setOverflow(Style.Overflow.VISIBLE);

        final Input input = new Input(validationXsdSchemaService, decompressionService, progressBar);
        add(new ViewToolbar(StringUtils.EMPTY, this.buildSearchButton(input), this.createInfoIcon()));
        add(input, progressBar);

    }

    private Span createInfoIcon() {
        final Span span = new Span();
        span.getStyle().setCursor(CURSOR_POINTER);
        Tooltip.forComponent(span).setText("Show info");
        span.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.AlignSelf.CENTER, LumoUtility.Margin.Right.SMALL);
        span.add(VaadinIcon.INFO_CIRCLE.create());
        span.addClickListener(event -> {
            new AboutDialog().open();
        });
        return span;
    }

    private Button buildSearchButton(Input input) {
        final Button buttonSearch = new Button();
        buttonSearch.addClassName("button-search");
        final SearchDialog searchDialog = input.getSearchDialog();
        buttonSearch.addClickListener(event -> searchDialog.open());
//        buttonSearch.getStyle().setCursor(CURSOR_POINTER);
//        buttonSearch.getStyle().setBorder("1px var(--lumo-utility-border-style, solid) var(--lumo-utility-border-color, var(--lumo-contrast-10pct))");
        buttonSearch.addThemeVariants(ButtonVariant.LUMO_SMALL);
        buttonSearch.addClassName(LumoUtility.TextColor.SECONDARY);
        final Span spanShortCut = new Span("Ctrl K");
        spanShortCut.getStyle().setCursor(CURSOR_POINTER);
        spanShortCut.getElement().getThemeList().add("badge small pill constrast");
        spanShortCut.getStyle().setBoxShadow(XsdValidatorConstants.VAR_CUSTOM_BOX_SHADOW);
        Shortcuts.addShortcutListener(buttonSearch, listener -> {
            searchDialog.open();
        }, Key.KEY_K, KeyModifier.CONTROL);

        final Span animatedText = new Span();
        animatedText.addClassName("search-animation");

        final Icon icon = VaadinIcon.SEARCH.create();
        icon.setSize("15px");

        var row = new HorizontalLayout(icon, animatedText);
        row.setSpacing("var(--lumo-space-xs)");
        row.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        buttonSearch.setPrefixComponent(row);
        buttonSearch.setSuffixComponent(spanShortCut);
        //buttonSearch.addClassName(LumoUtility.FontSize.SMALL);
        buttonSearch.setTooltipText("Search xsd or xml");

        return buttonSearch;
    }

}
