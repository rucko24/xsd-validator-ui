package com.rubn.xsdvalidator.view.list;

import com.rubn.xsdvalidator.util.Layout;
import com.rubn.xsdvalidator.util.SvgFactory;
import com.rubn.xsdvalidator.view.Span;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.shared.Tooltip;
import com.vaadin.flow.theme.lumo.LumoUtility.Background;
import com.vaadin.flow.theme.lumo.LumoUtility.BorderRadius;
import com.vaadin.flow.theme.lumo.LumoUtility.FontSize;
import com.vaadin.flow.theme.lumo.LumoUtility.Padding;

public class FileListItem extends ListItem {

    public FileListItem(String prefixFileName, String contentFileSize) {
        addClassName("file-list-item");
        addClassNames(Background.CONTRAST_5, BorderRadius.LARGE, Padding.Vertical.SMALL, Padding.Horizontal.SMALL);

        super.setPrefix(this.createFileIcon(prefixFileName.substring(prefixFileName.lastIndexOf('.') + 1)));

        final Span spanPrefixName = new Span(prefixFileName, FontSize.XSMALL);
        Tooltip tooltip = Tooltip.forComponent(spanPrefixName);
        tooltip.setText(prefixFileName);
        tooltip.setPosition(Tooltip.TooltipPosition.TOP);
        setPrimary(spanPrefixName);
        setSecondary(new Span(contentFileSize, FontSize.XXSMALL));
        this.column.removeClassName(Padding.Vertical.XSMALL);

        setGap(Layout.Gap.SMALL);
    }

    private Component createFileIcon(String paramfileName) {

        Layout corner = new Layout();
        corner.setSizeFull();

        String fileName = paramfileName.contains("xml")
                ? "file-xml-icon.svg"
                : "xsd.svg";

        SvgIcon icon = SvgFactory.createIconFromSvg(fileName, "40px", null);
        icon.setSize("40px");
        corner.add(icon);

        Layout fileIcon = new Layout(corner);
        fileIcon.setPosition(Layout.Position.RELATIVE);
        fileIcon.setWidth(2.3f, Unit.REM);

        return fileIcon;
    }
}

