package com.rubn.xsdvalidator.view.list;

import com.rubn.xsdvalidator.view.Span;
import com.rubn.xsdvalidator.util.Layout;
import com.rubn.xsdvalidator.util.SvgFactory;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.theme.lumo.LumoUtility.Background;
import com.vaadin.flow.theme.lumo.LumoUtility.BorderRadius;
import com.vaadin.flow.theme.lumo.LumoUtility.FontSize;
import com.vaadin.flow.theme.lumo.LumoUtility.Padding;

import static com.rubn.xsdvalidator.util.XsdValidatorConstants.CURSOS_POINTER;

public class FileListItem extends ListItem {

    public FileListItem(String prefixFileName, String contentFileSize) {
        setHeight("40px");
        setId("file-list-item");
        addClassNames(Background.CONTRAST_5, BorderRadius.LARGE, Padding.Horizontal.SMALL);
        getStyle().setCursor(CURSOS_POINTER);
        removeClassName(Padding.Horizontal.MEDIUM);

        super.setPrefix(this.createFileIcon(prefixFileName.substring(prefixFileName.lastIndexOf('.') + 1)));

        setPrimary(new Span(prefixFileName, FontSize.XSMALL));
        setSecondary(new Span(contentFileSize, FontSize.XXSMALL));
        this.column.removeClassName(Padding.Vertical.XSMALL);

        setGap(Layout.Gap.SMALL);
    }

    private Component createFileIcon(String paramfileName) {

        Layout corner = new Layout();
        corner.setSizeFull();

        String fileName = paramfileName.contains("xml")
                ? "file-xml-icon.svg"
                : "file-xsd-icon.svg";

        SvgIcon icon = SvgFactory.createIconFromSvg(fileName, "40px", null);
        icon.setSize("40px");
        corner.add(icon);

        Layout fileIcon = new Layout(corner);
        fileIcon.setPosition(Layout.Position.RELATIVE);
        fileIcon.setWidth(2.3f, Unit.REM);

        return fileIcon;
    }
}

