package com.rubn.xsdvalidator.view;

import com.rubn.xsdvalidator.records.DecompressedFile;
import com.rubn.xsdvalidator.service.DecompressionService;
import com.rubn.xsdvalidator.service.ValidationXsdSchemaService;
import com.rubn.xsdvalidator.view.list.CustomList;
import com.rubn.xsdvalidator.view.list.FileListItem;
import com.vaadin.browserless.BrowserlessUIContext;
import com.vaadin.flow.component.progressbar.ProgressBar;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vaadin.firitin.components.upload.UploadFileHandler;
import reactor.core.publisher.Flux;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InputTest {

    @InjectMocks
    private Input inputView;

    @Mock
    private ValidationXsdSchemaService validationService;
    @Mock
    private DecompressionService decompressionService;

    @Mock
    private ProgressBar progressBar;

    /**
     * A single browser window showing only the component under test, no routes or Spring context needed.
     */
    private BrowserlessUIContext window;

    @BeforeEach
    public void setup() {
        window = BrowserlessUIContext.forComponent(inputView);
    }

    @AfterEach
    public void tearDown() {
        window.close();
    }

    @Test
    public void smokeTest_componentsArePresent() {
        window.findButton().withText("Validate").component();

        CustomList list = window.find(CustomList.class).single();
        assertEquals(0, list.getChildren().count());
    }

    @Test
    @DisplayName("Using xml and xsd inputs")
    public void validateFlow_success() throws Exception {

        when(validationService.validateXmlInputWithXsdSchema(any(), any(), any())).thenReturn(Flux.just(List.of("Validation successful!!!")));

        injectFile("main.xsd", "<schema>...</schema>".getBytes(StandardCharsets.UTF_8));
        injectFile("data.xml", "<data>...</data>".getBytes(StandardCharsets.UTF_8));

        List<FileListItem> items = window.find(FileListItem.class).all();
        assertEquals(2, items.size());

        FileListItem xsdItem = items.stream().filter(i -> i.getFileName().endsWith(".xsd")).findFirst().get();
        FileListItem xmlItem = items.stream().filter(i -> i.getFileName().endsWith(".xml")).findFirst().get();

        xsdItem.setSelected(true);
        xmlItem.setSelected(true);

        window.findButton().withText("Validate").click();

        verify(validationService, times(1)).validateXmlInputWithXsdSchema(any(), any(), any());

    }

    private void injectFile(String fileName, byte[] content) throws Exception {

        Method method = Input.class.getDeclaredMethod("processFile",
                UploadFileHandler.FileDetails.class,
                byte[].class,
                boolean.class,
                DecompressedFile.class);
        method.setAccessible(true);

        UploadFileHandler.FileDetails metadata = new UploadFileHandler.FileDetails(fileName, "text/xml", content.length, ".");

        method.invoke(inputView, metadata, content, false, new DecompressedFile(null, null, 0L));
    }

    @Test
    public void clearAllData_shouldResetEverything() throws Exception {
        String xsdContent = "DUMMY XSD CONTENT";
        injectFile("main.xsd", xsdContent.getBytes(StandardCharsets.UTF_8));
        injectFile("data.xml", "DUMMY XML CONTENT".getBytes(StandardCharsets.UTF_8));

        CustomList list = window.find(CustomList.class).single();
        assertEquals(2, list.getChildren().count(), "Deben haber 2 items visuales antes de limpiar");

        inputView.clearAllData();

        assertEquals(0, list.getChildren().count(), "La lista visual debe estar vacía");

        java.lang.reflect.Field mapField = Input.class.getDeclaredField("mapPrefixFileNameAndContent");
        mapField.setAccessible(true);
        Map<?, ?> map = (Map<?, ?>) mapField.get(inputView);
        assertTrue(map.isEmpty(), "El mapa de memoria debe estar vacío");

        java.lang.reflect.Field xsdField = Input.class.getDeclaredField("selectedMainXsd");
        xsdField.setAccessible(true);
        assertEquals("", xsdField.get(inputView), "La selección de XSD debe haberse borrado");
    }

}