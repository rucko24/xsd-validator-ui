package com.rubn.xsdvalidator.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class XsdValidatorConstants {

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
    public static final String COPY_TO_CLIPBOARD = "./scripts/copy_to_clipboard.js";
    public static final String CURSOS_POINTER = "pointer";
    public static final String RESOURCES_SVG_IMAGES = "/META-INF/resources/svg-images/";
    public static final String COPY_ALT_SVG = "copy-alt.svg";
    public static final String MENU_ITEM_NO_CHECKMARK = "menu-item-no-checkmark";
    public static final String DELETE_MENU_ITEM_NO_CHECKMARK = "delete";
    public static final String JS_COMMAND = """
                    const span = document.createElement('span');
                    span.className = 'error-word fade-in';
                    span.textContent = $0 + ' ';
                    this.appendChild(span);
                
                    // Trigger reflow para reiniciar animación
                    void span.offsetWidth;
                    span.classList.add('visible');
                
                    // Scroll suave
                    span.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
                """;
    public static final String CLICK_LIKE_HORIZONTAL_SCROLL = """
            const slider = this;
            let isDown = false;
            let startX;
            let scrollLeft;

            slider.addEventListener('mousedown', (e) => {
              isDown = true;
              slider.classList.add('grabbing'); // Cambia el cursor
              startX = e.pageX - slider.offsetLeft;
              scrollLeft = slider.scrollLeft;
            });

            slider.addEventListener('mouseleave', () => {
              isDown = false;
              slider.classList.remove('grabbing');
            });

            slider.addEventListener('mouseup', () => {
              isDown = false;
              slider.classList.remove('grabbing');
            });

            slider.addEventListener('mousemove', (e) => {
              if (!isDown) return;
              e.preventDefault(); // Evita seleccionar texto
              const x = e.pageX - slider.offsetLeft;
              const walk = (x - startX) * 2; // Ajusta la velocidad del scroll (* 2 es buena velocidad)
              slider.scrollLeft = scrollLeft - walk;
            });
            """;
}
