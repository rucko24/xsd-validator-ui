package com.rubn.base.ui;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Constants {

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
    public static final String CONTEXT_MENU_ITEM_NO_CHECKMARK = "context-menu-item-no-checkmark";
    public static final String VAR_CUSTOM_BOX_SHADOW = "var(--custom-box-shadow)";
}
