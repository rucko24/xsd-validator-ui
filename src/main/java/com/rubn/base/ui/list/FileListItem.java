package com.rubn.base.ui.list;

import com.rubn.base.ui.Layout;
import com.rubn.base.ui.Span;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.theme.lumo.LumoUtility.Background;
import com.vaadin.flow.theme.lumo.LumoUtility.BorderRadius;
import com.vaadin.flow.theme.lumo.LumoUtility.FontSize;
import com.vaadin.flow.theme.lumo.LumoUtility.Padding;

public class FileListItem extends ListItem {

    public FileListItem(String prefixFileName, String contentFileSize) {
        addClassNames(Background.CONTRAST_5, BorderRadius.LARGE, Padding.Horizontal.SMALL);
        getStyle().setCursor("pointer");
        removeClassName(Padding.Horizontal.MEDIUM);

        super.setPrefix(this.createFileIcon(prefixFileName.substring(prefixFileName.lastIndexOf('.') + 1)));

        setPrimary(new Span(prefixFileName, FontSize.XSMALL));
        setSecondary(new Span(contentFileSize, FontSize.XXSMALL));
        this.column.removeClassName(Padding.Vertical.XSMALL);

        setGap(Layout.Gap.SMALL);
    }

    private Component createFileIcon(String fileName) {

        Layout corner = new Layout();
        corner.setSizeFull();

        String pathFileName = fileName.contains("xml")
                ? "/META-INF/resources/svg-images/file-xml-icon.svg"
                : "/META-INF/resources/svg-images/file-xsd-icon.svg";

        final DownloadHandler downloadHandler = DownloadHandler.forClassResource(getClass(), pathFileName);
        SvgIcon icon = new SvgIcon(downloadHandler);
        icon.setSize("40px");
        corner.add(icon);

        Layout fileIcon = new Layout(corner);
        fileIcon.setPosition(Layout.Position.RELATIVE);
        fileIcon.setWidth(2.3f, Unit.REM);

        return fileIcon;
    }
}

