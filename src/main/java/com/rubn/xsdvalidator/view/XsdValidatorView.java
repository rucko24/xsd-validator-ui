package com.rubn.xsdvalidator.view;

import com.rubn.base.ui.Input;
import com.rubn.base.ui.ViewToolbar;
import com.rubn.xsdvalidator.service.ValidationXsdSchemaService;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.context.ApplicationEventPublisher;

@UIScope
@SpringComponent
@Route("xsd-validator")
@PageTitle("xsd-validator")
@Menu(order = 1, icon = "vaadin:clipboard-check", title = "XSD Validator")
class XsdValidatorView extends Main implements BeforeEnterObserver {

    private final ValidationXsdSchemaService validationXsdSchemaService;

    public XsdValidatorView(final ValidationXsdSchemaService validationXsdSchemaService) {
        this.validationXsdSchemaService = validationXsdSchemaService;
        setSizeFull();
        getStyle().setOverflow(Style.Overflow.VISIBLE);

        add(new ViewToolbar("XSD Validator", ViewToolbar.group(null)));


    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        add(new Input(validationXsdSchemaService, event.getUI()));
    }

}
