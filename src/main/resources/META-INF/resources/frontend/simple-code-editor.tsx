import {ReactAdapterElement, RenderHooks} from 'Frontend/generated/flow/ReactAdapter';
import React, {ReactElement, useEffect, useRef, useState} from 'react';
import Editor from '@monaco-editor/react';

function MyReactEditor({ content, onContentChange, themeName, wrapEnabled,
    goToLine, lineTrigger}: any) {

    const [altPressed, setAltPressed] = useState(false);
    const editorRef = useRef<any>(null);

    useEffect(() => {
        const handleKeyDown = (e: KeyboardEvent) => { if (e.altKey) setAltPressed(true); };
        const handleKeyUp = (e: KeyboardEvent) => { if (!e.altKey) setAltPressed(false); };
        const handleBlur = () => setAltPressed(false);

        window.addEventListener('keydown', handleKeyDown);
        window.addEventListener('keyup', handleKeyUp);
        window.addEventListener('blur', handleBlur);

        return () => {
            window.removeEventListener('keydown', handleKeyDown);
            window.removeEventListener('keyup', handleKeyUp);
            window.removeEventListener('blur', handleBlur);
        };
    }, []);

    // Efecto para ir a una línea específica cuando Java lo solicite
    useEffect(() => {
        if (editorRef.current && goToLine) {
            // revealLineInCenter hace que la línea aparezca en medio de la pantalla
            editorRef.current.revealLineInCenter(goToLine);

            // Opcional: Poner el cursor al inicio de esa línea
            editorRef.current.setPosition({ lineNumber: goToLine, column: 1 });

            // Opcional: Resaltar la línea brevemente o darle el foco
            editorRef.current.focus();
        }
    }, [goToLine, lineTrigger]); // Se dispara cuando cambia la línea o el trigger

    function handleEditorDidMount(editor: any) {
        editorRef.current = editor;

        editor.onMouseDown((e: any) => {
            if (e.event.altKey) {
                const position = e.target.position;
                if (position) {
                    editor.setPosition(position);
                    editor.setSelection({
                        startLineNumber: position.lineNumber,
                        startColumn: position.column,
                        endLineNumber: position.lineNumber,
                        endColumn: position.column
                    });
                }
            }
        });
    }

    const handleEditorChange = (value: string | undefined) => {
        if (onContentChange && value !== undefined) onContentChange(value);
    };

    return (
        <div className={altPressed ? "alt-mode-active" : ""} style={{ height: '100%', width: '100%', overflow: 'hidden' }}>
            <style>{`
                .alt-mode-active .view-lines, .alt-mode-active .view-line,
                .alt-mode-active .monaco-editor, .alt-mode-active .overflow-guard {
                    cursor: crosshair !important;
                }
            `}</style>

            <Editor
                height="100%"
                defaultLanguage="xml"
                theme={themeName || "vs-dark"}
                value={content || ""}
                onChange={handleEditorChange}
                onMount={handleEditorDidMount} // <-- IMPORTANTE
                options={{
                    wordWrap: wrapEnabled ? 'on' : 'off',
                    minimap: { enabled: false },
                    automaticLayout: true,
                    columnSelection: altPressed,
                    multiCursorModifier: 'alt',
                    fontSize: 14,
                    lineNumbers: 'on',
                    scrollBeyondLastLine: false,
                }}
            />
        </div>
    );
}

class SimpleCodeEditorElement extends ReactAdapterElement {

    protected render(hooks: RenderHooks): ReactElement | null {
        const [content, setContent] = hooks.useState<string>("content");
        const [theme] = hooks.useState<string>("theme");
        const [wordWrap] = hooks.useState<boolean>("wordWrap");
        const [goToLine] = hooks.useState<number>("goToLine");
        const [lineTrigger] = hooks.useState<number>("lineTrigger");

        return (
            <MyReactEditor
                content={content}
                themeName={theme}
                wrapEnabled={wordWrap}
                goToLine={goToLine}
                lineTrigger={lineTrigger}
                onContentChange={(newVal: string) => {
                    setContent(newVal);
                    this.dispatchEvent(new CustomEvent("editor-change", {
                        detail: { value: newVal },
                        bubbles: true,
                        composed: true
                    }));
                }}
            />
        );
    }
}

customElements.define('simple-code-editor', SimpleCodeEditorElement);