package com.rubn.xsdvalidator.service;

import com.rubn.xsdvalidator.records.DecompressedFile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class DecompressionServiceTest {

    private static final Path RESOURCE_TEST_PATH = Path.of("src/test/resources/");

    @InjectMocks
    private DecompressionService decompressionService;

    @ParameterizedTest
    @CsvSource({
            "archivo.zip, true",
            "archivo.rar, true",
            "archivo.7z, true",
            "archivo.xml, false",
            "archivo.xsd, false",
            "archivo.txt, false",
            "archivo_sin_extension, false"
    })
    @DisplayName("It must correctly detect whether a file is compressed")
    void testIsCompressedFile(String fileName, boolean expected) {
        boolean isCompressed = decompressionService.isCompressedFile(fileName);

        assertThat(isCompressed).isEqualTo(expected);
    }

    @Test
    @DisplayName("It should extract files properly when no Consumer is provided (Overloaded method)")
    void testDecompressFile_WithoutConsumer_ShouldExtractProperlyAndIgnoreWarnings() throws IOException {
        InputStream zipInputStream = Files.newInputStream(RESOURCE_TEST_PATH.resolve("test-archives/test.zip"));

        List<DecompressedFile> result = decompressionService.decompressFile("test-archives/test.zip", zipInputStream);

        assertThat(result)
                .isNotNull()
                .hasSize(2);

        assertThat(result)
                .extracting(DecompressedFile::fileName)
                .containsExactlyInAnyOrder("test/product.xsd", "test/order-instance.xml")
                .doesNotContain("ignore.txt", "virus.exe");
    }

    @Test
    @DisplayName("It should throw an exception if the format is not supported")
    void testDecompressFile_UnsupportedFormat() {
        InputStream dummyStream = new ByteArrayInputStream(new byte[0]);

        assertThatThrownBy(() -> decompressionService.decompressFile("file.txt", dummyStream, msg -> {}))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported format");
    }

    @Test
    @DisplayName("It should unzip the ZIP file and filter ONLY the allowed files (XML, XSD)")
    void testDecompressZip_FiltersProperly() throws IOException {
        InputStream zipInputStream = Files.newInputStream(RESOURCE_TEST_PATH.resolve("test-archives/test.zip"));
        List<String> warnings = new ArrayList<>();

        List<DecompressedFile> result = decompressionService.decompressFile("test-archives/test.zip", zipInputStream, warnings::add);

        assertThat(result)
                .isNotNull()
                .as("Should have extracted exactly 2 allowed files")
                .hasSize(2);

        assertThat(result)
                .extracting(DecompressedFile::fileName)
                .containsExactlyInAnyOrder("test/product.xsd", "test/order-instance.xml")
                .doesNotContain("ignore.txt", "virus.exe");

        DecompressedFile xmlFile = result.stream()
                .filter(f -> f.fileName().equals("test/order-instance.xml"))
                .findFirst()
                .orElseThrow();

        assertThat(new String(xmlFile.content(), StandardCharsets.UTF_8))
                .isEqualTo(Files.readString(Path.of("src/test/resources/documents/validation-failure/order-instance.xml")));

        assertThat(warnings)
                .as("Should have captured warnings for ignored files")
                .isNotEmpty();
    }

    @Test
    @DisplayName("It should extract the 7Z file by reading from resources")
    void testDecompress7z_FiltersProperly() throws IOException {
        InputStream sevenZInputStream = Files.newInputStream(RESOURCE_TEST_PATH.resolve("test-archives/test.7z"));
        List<String> warnings = new ArrayList<>();

        assertThat(sevenZInputStream)
                .as("The test.7z file must exist in src/test/resources/test-archives/")
                .isNotNull();

        List<DecompressedFile> result = decompressionService.decompressFile("test-archives/test.7z", sevenZInputStream, warnings::add);

        assertThat(result)
                .isNotNull()
                .as("Must have extracted files from the 7Z archive")
                .isNotEmpty();

        assertThat(result)
                .extracting(DecompressedFile::fileName)
                .anyMatch(name -> name.endsWith(".xml"))
                .anyMatch(name -> name.endsWith(".xsd"))
                .noneMatch(name -> name.endsWith(".png"));

        assertThat(warnings)
                .anyMatch(msg -> msg.contains(".png"));
    }

    @Test
    @DisplayName("It should extract the RAR file by reading from resources")
    void testDecompressRar_FiltersProperly() throws IOException {
        InputStream rarInputStream = Files.newInputStream(RESOURCE_TEST_PATH.resolve("test-archives/test.rar"));
        List<String> warnings = new ArrayList<>();

        assertThat(rarInputStream)
                .as("The test.rar file must exist in src/test/resources/test-archives/")
                .isNotNull();

        List<DecompressedFile> result = decompressionService.decompressFile("test-archives/test.rar", rarInputStream, warnings::add);

        assertThat(result)
                .isNotNull()
                .as("Must have extracted files from the RAR archive")
                .isNotEmpty();

        assertThat(result)
                .extracting(DecompressedFile::fileName)
                .anyMatch(name -> name.endsWith(".xml"))
                .noneMatch(name -> name.endsWith(".png"));

        assertThat(warnings)
                .anyMatch(msg -> msg.contains(".png"));
    }
}