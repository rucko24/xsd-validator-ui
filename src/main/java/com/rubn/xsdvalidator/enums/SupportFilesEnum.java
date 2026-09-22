package com.rubn.xsdvalidator.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum SupportFilesEnum {

    XML(".xml"),
    XSD(".xsd"),
    ZIP(".zip"),
    FILE_7Z(".7z"),
    RAR(".rar"),
    UNKNOWN("");

    final String extension;

    public static SupportFilesEnum fromExtension(String filenameOrExtension) {
        if (StringUtils.isBlank(filenameOrExtension)) return UNKNOWN;
        String lowerInput = filenameOrExtension.toLowerCase();
        return Arrays.stream(SupportFilesEnum.values())
                .filter(type -> type != UNKNOWN)
                .filter(type -> lowerInput.endsWith(type.extension) || lowerInput.equals(type.extension.replace(".", "")))
                .findFirst()
                .orElse(UNKNOWN);
    }

}
