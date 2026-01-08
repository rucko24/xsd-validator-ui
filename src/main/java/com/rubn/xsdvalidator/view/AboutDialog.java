package com.rubn.xsdvalidator.view;

import com.vaadin.flow.component.dialog.Dialog;
import org.vaadin.firitin.components.RichText;

public class AboutDialog extends Dialog {

    public AboutDialog() {

        add(new RichText().withMarkDown("""
       ### Steps

       1. Upload at least one xsd and xml file using the **Attachment** button below.
       2. Then press the validate button.
       3. A series of errors will be displayed if there are any, otherwise a message saying **Validation successful** will appear.
      
       """));
    }

}
