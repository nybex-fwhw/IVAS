package um.feri.ipt.tids.project.parser.ares;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import java.time.Clock;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.*;

/** Explicit opt-in: three official-site lookups, with no API access. */
@EnabledIfSystemProperty(named = "ares.live.tests", matches = "true")
class AresLiveTests {
    @Test
    void readsOfficialRenderedCompanyPages() throws Exception {
        var scraper = new AresScraper(Clock.systemUTC(), Duration.ofSeconds(30), false,
                System.getProperty("ares.browser.channel"));
        for (String ico : new String[]{"27082440", "00006947"}) {
            AresResult result = scraper.scrape(ico);
            new AresJsonExporter().write(result, java.nio.file.Path.of("target", "ares-live", ico + ".json"));
            assertEquals(AresResult.Status.SUCCESS, result.status(), () -> result.errors().toString());
            assertEquals(ico, result.company().ico());
            assertNotNull(result.company().name());
            assertNotNull(result.company().registeredAddress());
            assertNotNull(result.company().legalForm());
            if (ico.equals("27082440")) assertEquals("Alza.cz a.s.", result.company().name());
        }
    }

    @Test
    void recognizesOfficialEmptySearchResult() throws Exception {
        var scraper = new AresScraper(Clock.systemUTC(), Duration.ofSeconds(30), false,
                System.getProperty("ares.browser.channel"));
        AresResult result = scraper.scrape("99999999");
        new AresJsonExporter().write(result, java.nio.file.Path.of("target", "ares-live", "99999999.json"));
        assertEquals(AresResult.Status.NOT_FOUND, result.status(), () -> result.errors().toString());
        assertNull(result.company());
    }
}
