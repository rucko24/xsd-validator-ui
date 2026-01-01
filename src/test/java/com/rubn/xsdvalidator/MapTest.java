package com.rubn.xsdvalidator;

import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class MapTest {

//    @RepeatedTest(5000)
    @Test
    void getFiles() {
        Map<String, String> map = Map.of(
                "schema.xsd", "2kb",
                "pain.xml", "1kb"
        );

        String[] nombresOrdenados = map.keySet()
                .stream()
                .sorted((a, b) -> a.endsWith(".xml") ? -1 : 1)
                .toArray(String[]::new);

        assertThat(nombresOrdenados[0]).isEqualTo("pain.xml");
        assertThat(nombresOrdenados[1]).isEqualTo("schema.xsd");

    }

}
