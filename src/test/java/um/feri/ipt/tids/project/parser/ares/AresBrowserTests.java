package um.feri.ipt.tids.project.parser.ares;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;
import static um.feri.ipt.tids.project.parser.ares.AresResult.*;

/** Synthetic rendered fixtures, not captures of the official ARES site. No external network required. */
@EnabledIfSystemProperty(named = "ares.browser.tests", matches = "true")
class AresBrowserTests {
    private void withPage(java.util.function.Consumer<Page> test) {
        try (Playwright playwright = Playwright.create();
             Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                     .setChannel(System.getProperty("ares.browser.channel")))) {
            Page page = browser.newPage();
            test.accept(page);
        }
    }

    private AresResult parse(Page page) {
        return new AresDomParser().parse(page, "27082440", "https://ares.gov.cz/", Instant.EPOCH);
    }

    @Test
    void selectsExactCompanyRowAndNormalizesRenderedText() {
        withPage(page -> {
            page.setContent("""
                <table><tr><th>IČO</th><th>Obchodní jméno</th><th>Sídlo</th><th>Právní forma</th><th>DIČ</th></tr>
                <tr><td>00006947</td><td>Other company</td><td>Other address</td><td>Other form</td><td>Other tax ID</td></tr>
                <tr><td>27082440</td><td> Alza.cz&nbsp; a.s. </td><td>Praha<br>Česká republika</td><td>Akciová společnost</td><td>CZ27082440</td></tr></table>
                """);
            AresResult result = parse(page);
            assertEquals(Status.SUCCESS, result.status());
            assertEquals("Alza.cz a.s.", result.company().name());
            assertEquals("Praha Česká republika", result.company().registeredAddress());
            assertEquals("CZ27082440", result.company().vatId());
            assertTrue(result.missingFields().isEmpty());
        });
    }

    @Test
    void readsOfficialSearchTableHeadersAndKeepsDisplayedLegalFormCode() {
        withPage(page -> {
            // Reduced fixture based on the rendered ARES table inspected on 2026-10-23.
            page.setContent("""
                <table role="table"><thead><tr role="row">
                <th role="columnheader"><div><span>IČO</span><svg aria-hidden="true"></svg></div></th>
                <th role="columnheader"><div><span>Obchodní název</span></div></th>
                <th role="columnheader"><div><span>Právní forma</span></div></th>
                <th role="columnheader"><div><span>Sídlo</span></div></th>
                <th role="columnheader">Odkazy</th></tr></thead><tbody><tr role="row">
                <td role="cell">27082440</td><td role="cell">Alza.cz a.s.</td>
                <td role="cell"><span>121</span></td>
                <td role="cell">Jankovcova 1522/53, Holešovice, 17000 Praha 7</td>
                <td role="cell"><a href="/ekonomicke-subjekty/res/27082440">RES</a></td>
                </tr></tbody></table>
                """);
            AresResult result = parse(page);
            assertEquals(Status.SUCCESS, result.status());
            assertEquals("Alza.cz a.s.", result.company().name());
            assertEquals("121", result.company().legalForm());
            assertEquals("Jankovcova 1522/53, Holešovice, 17000 Praha 7", result.company().registeredAddress());
            assertNull(result.company().vatId());
            assertEquals(java.util.List.of("vatId"), result.missingFields());
        });
    }

    @Test
    void recognizesOfficialEmptyResultOnlyWithAnIdleVisibleResultTable() {
        withPage(page -> {
            String emptyTable = """
                <div data-pc-name="datatable"><table><thead><tr><th>IČO</th><th>Obchodní název</th></tr></thead>
                <tbody><tr data-pc-section="emptymessage"><td colspan="2"></td></tr></tbody></table>
                <nav><div data-pc-name="paginator"><span data-pc-section="current">Zobrazeno 0 až 0 z 0</span></div></nav>
                </div>
                """;
            page.setContent(emptyTable);
            assertEquals(Status.NOT_FOUND, parse(page).status());
            page.setContent(emptyTable.replace("</nav>", "</nav><div data-pc-section='loadingoverlay'>Loading</div>"));
            assertEquals(Status.ERROR, parse(page).status());
            page.setContent(emptyTable + "<div role='alert'>Server error</div>");
            assertEquals(Status.ERROR, parse(page).status());
            page.setContent("<p>Zobrazeno 0 až 0 z 0</p>");
            assertEquals(Status.ERROR, parse(page).status());
            page.setContent("<div hidden>" + emptyTable + "</div>");
            assertEquals(Status.ERROR, parse(page).status());
        });
    }

    @Test
    void preservesZeroesAndDoesNotInferVatId() {
        withPage(page -> {
            page.setContent("<dl><dt>IČO:</dt><dd>00006947</dd><dt>Název:</dt><dd>Český podnik</dd></dl>");
            AresResult result = new AresDomParser().parse(page, "00006947", "https://ares.gov.cz/", Instant.EPOCH);
            assertEquals(Status.SUCCESS, result.status());
            assertEquals("00006947", result.company().ico());
            assertNull(result.company().vatId());
            assertEquals(java.util.List.of("vatId", "registeredAddress", "legalForm"), result.missingFields());
        });
    }

    @Test
    void distinguishesPartialMissingAndUnknownResults() {
        withPage(page -> {
            page.setContent("<dl><dt>IČO</dt><dd>27082440</dd></dl>");
            assertEquals(Status.PARTIAL, parse(page).status());
            page.setContent("<p>Nebyly nalezeny žádné ekonomické subjekty</p>");
            assertEquals(Status.NOT_FOUND, parse(page).status());
            page.setContent("<p>Server unavailable</p>");
            assertEquals(Status.ERROR, parse(page).status());
            page.setContent("<dl><dt>IČO</dt><dd>00006947</dd><dt>Název</dt><dd>Other company</dd></dl>");
            assertEquals(Status.ERROR, parse(page).status());
            page.setContent("<p hidden>Nebyly nalezeny žádné ekonomické subjekty</p><p>Loading</p>");
            assertEquals(Status.ERROR, parse(page).status());
        });
    }

    @Test
    void ignoresHiddenRecordsAndRejectsConflictingNames() {
        withPage(page -> {
            page.setContent("<dl hidden><dt>IČO</dt><dd>27082440</dd><dt>Název</dt><dd>Hidden</dd></dl>");
            assertEquals(Status.ERROR, parse(page).status());
            page.setContent("""
                <table><tr><th>IČO</th><th>Název</th></tr>
                <tr><td>27082440</td><td>First</td></tr><tr><td>27082440</td><td>Second</td></tr></table>
                """);
            assertEquals("AMBIGUOUS_RESULT", parse(page).errors().getFirst().code());
        });
    }

    @Test
    void waitsForJavascriptRenderingAndHandlesUnknownDomDeadline() {
        withPage(page -> {
            page.setContent("<p>Loading</p><script>setTimeout(() => document.body.innerHTML = '<dl><dt>IČO</dt><dd>27082440</dd><dt>Název</dt><dd>Rendered company</dd></dl>', 150)</script>");
            var parser = new AresDomParser();
            assertEquals(Status.SUCCESS, parser.awaitResult(page, "27082440", "https://ares.gov.cz/", Instant.EPOCH, 3000).status());
            page.setContent("<p>Changed layout</p>");
            assertEquals("UNRECOGNIZED_DOM", parser.awaitResult(page, "27082440", "https://ares.gov.cz/", Instant.EPOCH, 200).errors().getFirst().code());
        });
    }

    @Test
    void readsVisibleLabelValueBlocksWithoutCombiningCompanies() {
        withPage(page -> {
            page.setContent("""
                <section><div><span>IČO:</span><span>00006947</span></div>
                <div><span>Název:</span><span>Other company</span></div></section>
                <section><div><span>IČO:</span><span>27082440</span></div>
                <div><span>Název:</span><span>Alza.cz a.s.</span></div></section>
                <section><dl><dt>IČO</dt><dd>27082440</dd><dt>Název</dt><dd></dd>
                <dt>DIČ</dt><dd>CZ27082440</dd><dt>Sídlo</dt><dd>Praha</dd><dt>Právní forma</dt><dd>Akciová společnost</dd></dl></section>
                """);
            assertEquals("Alza.cz a.s.", parse(page).company().name());
        });
    }
}
