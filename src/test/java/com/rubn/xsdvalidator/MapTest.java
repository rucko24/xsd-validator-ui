package com.rubn.xsdvalidator;

import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Stream;

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
                .sorted((a, b) -> a.endsWith(".xml") ? -1 : 0)
                .toArray(String[]::new);

        assertThat(nombresOrdenados[0]).isEqualTo("pain.xml");
        assertThat(nombresOrdenados[1]).isEqualTo("schema.xsd");

    }

    @Test
    @SneakyThrows
    @DisplayName("Delay for each word in the error list")
    void case2() {
        String error = "ERROR: Línea 45, Columna 63: cvc-pattern-valid: El valor 'ES79 2100 0000 0000 0000 0000' no es de faceta válida con respecto al patrón '[A-Z]{2,2}[0-9]{2,2}[a-zA-Z0-9]{1,30}' para el tipo 'IBAN2007Identifier'.";
        List<String> errorList = List.of(error);

        var processList = errorList
                .stream()
                .flatMap(item -> Stream.of(item.split(" ")))
                .toList();

        final CountDownLatch countDownLatch = new CountDownLatch(1);
        Flux.fromIterable(processList)
                .delayElements(Duration.ofSeconds(1))
                .log()
                .doOnTerminate(countDownLatch::countDown)
                .subscribe();

        countDownLatch.await();

    }

}
