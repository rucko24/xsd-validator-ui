package com.rubn.xsdvalidator.view;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.component.react.ReactAdapterComponent;
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.shared.Registration;

/**
 * A Vaadin component wrapper for an interactive terminal powered by xterm.js via React.
 * This component bridges Java server-side code with a client-side TypeScript React adapter,
 * supporting persistent state across page reloads (F5) via {@link PreserveOnRefresh}.
 */
//@NpmPackage(value = "@xterm/xterm", version = "^6.0.0")
//@NpmPackage(value = "@xterm/addon-fit", version = "^0.11.0")
//@NpmPackage(value = "@xterm/addon-search", version = "^0.16.0")
//@JsModule("./xterm-terminal.tsx")
//@Tag("xterm-terminal")
public class XtermTerminal extends ReactAdapterComponent {

    /** Buffer that stores the terminal session history to restore state on refresh. */
    private final StringBuilder historyBuffer = new StringBuilder();

    /** Maximum allowed character size for the history buffer to prevent excessive memory usage. */
    private static final int MAX_BUFFER_SIZE = 100_000;

    /**
     * Constructs a new XtermTerminal component, initializes its layout dimensions,
     * and sets up the history restoration listener when the frontend signals it is ready.
     */
    public XtermTerminal() {
        // Initial layout styles configuration
        getStyle().set("width", "100%");
        getStyle().set("height", "100%");

        // Replay history buffer to the terminal upon initialization or browser refresh
        addListener(TerminalReadyEvent.class, e -> {
            if (!historyBuffer.isEmpty()) {
                getElement().callJsFunction("writeText", historyBuffer.toString());
            }
        });
    }

    /**
     * Scrolls the terminal viewport view directly to the bottom line.
     */
    public void scrollToBottom() {
        getElement().callJsFunction("scrollToBottom");
    }

    /**
     * Zooms the terminal text font size in or out.
     *
     * @param delta positive to increase font size, negative to decrease
     */
    public void changeFontSize(int delta) {
        getElement().callJsFunction("changeFontSize", delta);
    }

    /**
     * Writes raw text to the terminal and appends it to the history buffer.
     *
     * @param text the text content to write
     */
    public void write(String text) {
        appendAndExecute(text);
    }

    /**
     * Writes a prompt or command string, ensuring correct interactive terminal formatting.
     *
     * @param promptText the prompt text to render
     */
    public void writePrompt(String promptText) {
        appendAndExecute(promptText);
    }

    /**
     * Writes text to the terminal followed by a carriage return and newline sequence (\r\n).
     *
     * @param text the text content to write with a newline
     */
    public void writeln(String text) {
        appendAndExecute(text + "\r\n");
    }

    /**
     * Clears the internal history buffer and resets the terminal screen view.
     */
    public void clear() {
        historyBuffer.setLength(0);
        getElement().callJsFunction("clear");
    }

    /**
     * Appends the given text to the history buffer (truncating if it exceeds limits)
     * and triggers the client-side JavaScript rendering function.
     *
     * @param text the text string to append and execute
     */
    private void appendAndExecute(String text) {
        historyBuffer.append(text);
        if (historyBuffer.length() > MAX_BUFFER_SIZE) {
            historyBuffer.delete(0, historyBuffer.length() - (MAX_BUFFER_SIZE / 2));
        }
        getElement().callJsFunction("writeText", text);
    }

    /**
     * Sets whether the terminal is read-only.
     * When read-only, user keyboard input is disabled, but programmatic writing and selection remain enabled.
     *
     * @param readOnly true to make the terminal read-only, false otherwise
     */
    public void setReadOnly(boolean readOnly) {
        getElement().callJsFunction("setReadOnly", readOnly);
    }

    /**
     * Enables or disables pasting text from the system clipboard using a right-click action.
     *
     * @param enabled true to allow right-click paste, false otherwise
     */
    public void setPasteWithRightClick(boolean enabled) {
        getElement().callJsFunction("setPasteWithRightClick", enabled);
    }

    /**
     * Scrolls the terminal viewport view directly to the top line of the history buffer.
     */
    public void scrollToTop() {
        getElement().callJsFunction("scrollToTop");
    }

    /**
     * Toggles the terminal display mode between normal layout and full screen expansion.
     */
    public void toggleFullscreen() {
        getElement().callJsFunction("toggleFullscreen");
    }

    /**
     * Event captured from the custom 'terminal-input' DOM event dispatched by the React component.
     */
    @DomEvent("terminal-input")
    public static class TerminalInputEvent extends ComponentEvent<XtermTerminal> {
        private final String value;

        /**
         * Instantiates a new terminal input event.
         *
         * @param source     the source component
         * @param fromClient whether the event originated from the client
         * @param value     the raw string typed or submitted by the user
         */
        public TerminalInputEvent(XtermTerminal source, boolean fromClient, @EventData("event.detail.value") String value) {
            super(source, fromClient);
            this.value = value;
        }

        /**
         * Returns the input value sent from the client terminal.
         *
         * @return the input string value
         */
        public String getValue() {
            return value;
        }
    }

    /**
     * Event dispatched when the React xterm component has fully mounted and is ready to accept commands.
     */
    @DomEvent("terminal-ready")
    public static class TerminalReadyEvent extends ComponentEvent<XtermTerminal> {

        /**
         * Instantiates a new terminal ready event.
         *
         * @param source     the source component
         * @param fromClient whether the event originated from the client
         */
        public TerminalReadyEvent(XtermTerminal source, boolean fromClient) {
            super(source, fromClient);
        }
    }

    /**
     * Registers a listener to listen for user keyboard input events coming from the terminal.
     *
     * @param listener the component event listener to register
     * @return a Registration object that can be used to remove the listener
     */
    public Registration addTerminalInputListener(ComponentEventListener<TerminalInputEvent> listener) {
        return addListener(TerminalInputEvent.class, listener);
    }
}