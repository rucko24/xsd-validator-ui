package com.rubn.xsdvalidator.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.vaadin.firitin.components.RichText;

public class AboutDialog extends Dialog {

    public AboutDialog() {
        super.addClassName("about-dialog");

        final Button closeButton = new Button(new Icon("lumo", "cross"));
        closeButton.setTooltipText("Close");
        closeButton.addClassName(LumoUtility.Margin.Left.AUTO);
        closeButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        closeButton.addClickListener(e -> close());
        super.getHeader().add(closeButton);

        super.add(new RichText().withMarkDown("""
                ### Steps
                
                1. Upload at least one xsd and xml file using the **Attachment** button below.
                2. Then press the validate button.
                3. A series of errors will be displayed if there are any, otherwise a message saying **Validation successful** will appear.
                
                - You can **copy** the errors one by one, copy them **all**, or **download** them to a text file.
                
                """));
    }

}
