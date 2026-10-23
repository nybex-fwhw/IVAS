package um.feri.ipt.tids.project.parser.ares;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.Clock;
import java.time.Duration;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static um.feri.ipt.tids.project.parser.ares.AresResult.*;

class AresCoreTests {
    @Test
    void validatesFormatAndPreservesLeadingZeroes() {
        assertEquals("00006947", Ico.validate("00006947"));
        assertEquals("27082440", Ico.validate("27082440"));
        for (String input : new String[]{"", "1234567", "123456789", " 27082440", "27082440 ", "abcdefgh", "２７０８２４４０", "1234567\n"}) {
            assertThrows(IllegalArgumentException.class, () -> Ico.validate(input));
        }
        assertThrows(IllegalArgumentException.class, () -> Ico.validate(null));
    }

    @Test
    void normalizesUnicodeWhitespaceAndEmptyValues() {
        assertEquals("Žlutý kůň a.s.", AresDomParser.normalize("\n Žlutý\u00a0kůň\u202f a.s. \t"));
        assertNull(AresDomParser.normalize(" \n\u00a0"));
        assertNull(AresDomParser.normalize(null));
    }

    @Test
    void rejectsInvalidInputAndOptionsBeforeBrowserLaunch() {
        assertThrows(IllegalArgumentException.class, () -> new AresScraper().scrape("bad"));
        assertThrows(IllegalArgumentException.class, () -> new AresScraper(Clock.systemUTC(), Duration.ZERO, false, null));
        assertThrows(IllegalArgumentException.class, () -> new AresScraper(Clock.systemUTC(), Duration.ofNanos(1), false, null));
        assertThrows(IllegalArgumentException.class, () -> new AresScraper(Clock.systemUTC(), Duration.ofSeconds(1), false, "unknown"));
        var output = new ByteArrayOutputStream();
        var stream = new PrintStream(output);
        assertEquals(2, AresParserCli.run(new String[]{"27082440", "--output"}, stream, stream));
        assertEquals(2, AresParserCli.run(new String[]{"bad"}, stream, stream));
        assertEquals(2, AresParserCli.run(new String[]{"27082440", "--unknown"}, stream, stream));
        assertEquals(0, AresParserCli.run(new String[]{"--help"}, stream, stream));
    }

    @Test
    void exportsUtf8NullsMetadataAndReplacesExistingFile() throws Exception {
        Path directory = Files.createTempDirectory(Path.of("target"), "ares-json-test-");
        Path output = directory.resolve("nested/00006947.json");
        var result = create("00006947", "https://ares.gov.cz/ekonomicke-subjekty?ico=00006947",
                Instant.parse("2026-10-23T17:00:00Z"), Status.SUCCESS,
                new Company("00006947", "Český podnik", null, null, null), List.of());
        new AresJsonExporter().write(result, output);
        var json = JsonMapper.builder().build().readTree(Files.readString(output));
        assertEquals("00006947", json.path("company").path("ico").asText());
        assertEquals("Český podnik", json.path("company").path("name").asText());
        assertTrue(json.path("company").path("vatId").isNull());
        assertEquals(3, json.path("missingFields").size());
        assertEquals("HTML_DOM", json.path("metadata").path("method").asText());
        assertEquals("2026-10-23T17:00:00Z", json.path("metadata").path("extractedAt").asText());
        var failed = create("00006947", "https://ares.gov.cz/", Instant.now(), Status.ERROR,
                null, List.of(new Problem("TIMEOUT", "Timed out.")));
        new AresJsonExporter().write(failed, output);
        json = JsonMapper.builder().build().readTree(Files.readString(output));
        assertEquals("ERROR", json.path("status").asText());
        assertTrue(json.path("company").isNull());
        try (var paths = Files.list(output.getParent())) {
            assertEquals(List.of(output), paths.toList());
        }
        Files.delete(output);
        Files.delete(output.getParent());
        Files.delete(directory);
    }
}
