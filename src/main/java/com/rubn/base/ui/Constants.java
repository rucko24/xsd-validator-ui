package com.rubn.base.ui;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Constants {

    public static final String JAVA_IO_USER_HOME_DIR_OS = System.getProperty("user.home");
    public static final String OUTPUT_DIR_XSD_VALIDATOR_UI = "/xsd-validator-ui/";
    public static final String OK = "OK";
    public static final String INFORMATION = "Information";
    public static final String WARNING = "Warning";
    public static final String BORDER_BOTTOM_COLOR = "1px solid var(--lumo-contrast-10pct)";

    public static final String SCROLLBAR_CUSTOM_STYLE = """
                const style = document.createElement('style');
                style.textContent = `
                    ::-webkit-scrollbar {
                        width: 8px;
                        height: 8px;
                    }
                    ::-webkit-scrollbar-track {
                        background-color: var(--bg2);
                    }
                    ::-webkit-scrollbar-thumb {
                        background-color: hsla(0, 0%, 49.8%, 0.5);
                        border-radius: 4px;
                    }
                `;
                this.appendChild(style);
            """;
    public static final String WINDOW_COPY_TO_CLIPBOARD = "window.copyToClipboard($0)";
    public static final String BOX_SHADOW_PROPERTY = "box-shadow";
    public static final String BOX_SHADOW_VALUE = "0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19)";
}
