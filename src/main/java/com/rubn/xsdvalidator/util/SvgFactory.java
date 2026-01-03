package com.rubn.xsdvalidator.util;

import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.server.streams.DownloadHandler;
import lombok.experimental.UtilityClass;

import java.util.Objects;

import static com.rubn.xsdvalidator.util.XsdValidatorConstants.COPY_ALT_SVG;
import static com.rubn.xsdvalidator.util.XsdValidatorConstants.RESOURCES_SVG_IMAGES;


/**
 * @author rubn
 */
@UtilityClass
public class SvgFactory {

    /**
     * Button with svg copy style /images/copy-alt.svg
     *
     * @return {@link SvgIcon}
     */
    public static SvgIcon createCopyButtonFromSvg() {
        //copy-alt.svg
        final DownloadHandler downloadHandler = DownloadHandler.forClassResource(SvgFactory.class, RESOURCES_SVG_IMAGES + COPY_ALT_SVG);
        final SvgIcon icon = new SvgIcon(downloadHandler);
        icon.setSize("25px");
        return icon;
    }

    /**
     * Button with svg copy style /images/filename.svg
     *
     * @param customHeight in pixels, optional parameter
     * @param size         in pixels
     * @return {@link SvgIcon}
     */
    public static SvgIcon createIconFromSvg(String fileName, String size, String customHeight) {
        Objects.requireNonNull(fileName, "fileName is null, we must put it in the svg-icons folder");
        final DownloadHandler downloadHandler = DownloadHandler.forClassResource(SvgFactory.class, RESOURCES_SVG_IMAGES + fileName);
        var icon = new SvgIcon(downloadHandler);
        icon.setSize(size);
        if (Objects.nonNull(customHeight)) {
            icon.getStyle().set("height", customHeight);
        }
        return icon;
    }

}
