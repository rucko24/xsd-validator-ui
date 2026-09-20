package com.rubn.xsdvalidator.view;

import com.flowingcode.vaadin.addons.xterm.XTerm;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.shared.Tooltip;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import com.vaadin.flow.theme.lumo.LumoUtility;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.vaadin.firitin.components.RichText;

import java.io.IOException;

@Log4j2
@PreserveOnRefresh
@UIScope
@SpringComponent
@Route("xterm")
@PageTitle("xterm")
@Menu(order = 2, icon = "vaadin:clipboard-check", title = "xterm-test")
public class XtermView extends Main {

    private final VerticalLayout layout = new VerticalLayout();
    private final Button button = new Button(VaadinIcon.TRASH.create());
    private XTerm terminal = new  XTerm();

    public XtermView() throws IOException {
        getStyle().set("overflow", "hidden");
        terminal.setPasteWithRightClick(true);
        
        button.addThemeVariants(ButtonVariant.LUMO_ERROR);
        terminal.writeln("A simple text");

        button.addClickListener(event -> terminal.clear());

        layout.setSizeFull();
        layout.getStyle().set("overflow", "hidden");
        layout.add(button, terminal);

        add(new ViewToolbar(StringUtils.EMPTY, new HelperDialog().buildInfoIcon()));
        add(layout);
    }


    public class HelperDialog extends Dialog {

        public Span buildInfoIcon() {
            super.setHeaderTitle("Steps");

            final Button closeButton = new Button(VaadinIcon.CLOSE.create());
            closeButton.setTooltipText("Close");
            closeButton.addClassName(LumoUtility.Margin.Left.AUTO);
            closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
            closeButton.addClickListener(e -> close());
            super.getHeader().add(closeButton);

            super.add(new RichText().withMarkDown("""   
                1. Upload at least one xsd and xml file using the **Attachment** button below.
                2. Then press the validate button.
                3. A series of errors will be displayed if there are any, otherwise a message saying **Validation successful** will appear.
                
                - You can **copy** the errors one by one, copy them **all**, or **download** them to a text file.
                
                """));

            final Span span = new Span();
            span.getStyle().setCursor("pointer");
            Tooltip.forComponent(span).setText("Show info");
            span.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.AlignSelf.CENTER, LumoUtility.Margin.Right.SMALL);
            span.add(VaadinIcon.INFO_CIRCLE.create());
            span.addClickListener(event -> {
                this.open();
            });
            return span;
        }

    }


}
