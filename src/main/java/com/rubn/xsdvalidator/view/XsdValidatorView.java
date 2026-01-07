package com.rubn.xsdvalidator.view;

import com.rubn.xsdvalidator.service.ValidationXsdSchemaService;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.shared.Tooltip;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import com.vaadin.flow.theme.lumo.LumoUtility;

import static com.rubn.xsdvalidator.util.XsdValidatorConstants.COPY_TO_CLIPBOARD;

@UIScope
@SpringComponent
@Route("")
@RouteAlias("xsd-validator")
@PageTitle("xsd-validator")
@Menu(order = 1, icon = "vaadin:clipboard-check", title = "XSD Validator")
@JsModule(COPY_TO_CLIPBOARD)
class XsdValidatorView extends Main {

    public XsdValidatorView(final ValidationXsdSchemaService validationXsdSchemaService) {
        setSizeFull();
        getStyle().setOverflow(Style.Overflow.VISIBLE);

        add(new ViewToolbar("XSD Validator", ViewToolbar.group(this.createInfoIcon())));
        add(new Input(validationXsdSchemaService));
    }

    private Span createInfoIcon() {
        final Span span = new Span();
        Tooltip.forComponent(span).setText("Show info");
        span.addClassName(LumoUtility.TextColor.SECONDARY);
        span.add(VaadinIcon.INFO_CIRCLE.create());
        return span;
    }

}
