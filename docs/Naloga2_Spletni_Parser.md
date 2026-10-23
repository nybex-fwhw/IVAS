# Naloga 2 – Spletni razčlenjevalnik podatkov iz registra ARES

> **Projekt:** IVAS – predhodno preverjanje strukturiranih elektronskih računov  
> **Status:** načrt implementacije (funkcionalnost še ni izvedena)  
> **Umestitev v repozitorij:** `docs/Naloga2_Spletni_Parser.md`  
> **Predvidena implementacija:** obstoječi glavni Maven projekt, `src/main/java/um/feri/ipt/tids/project/`

## 1. Namen naloge

Cilj naloge je razviti **spletni razčlenjevalnik (web parser/scraper)**, ki iz javnega spletnega prikaza češkega registra ARES samodejno pridobi podatke o izbranem gospodarskem subjektu in jih shrani v **strukturirano datoteko JSON**.

Razčlenjevalnik je del razvoja projekta **IVAS**, katerega končni namen je predhodno preverjanje prejetih elektronskih računov v formatu UBL 2.1. Registrski podatki, pridobljeni s to nalogo, bodo lahko kasneje uporabljeni za primerjavo s podatki dobavitelja na računu.

Naloga 2 **ni samostojen projekt**: implementira se znotraj glavne strukture IVAS. Mapa `Naloga1_XML_in_JSON/` ostane ločena, saj predstavlja neodvisno predhodno nalogo.

## 2. Obseg naloge

### Vključeno

- Sprejem identifikatorja češkega podjetja **IČO** kot vhodnega podatka.
- Preverjanje oblike vhodnega identifikatorja (osem števk, vključno z morebitnimi začetnimi ničlami).
- Odpiranje uradne spletne strani ARES s programskim brskalnikom.
- Branje **dejansko izrisanih HTML elementov** strani po prikazu rezultatov.
- Razčlenjevanje razpoložljivih registrskih podatkov v enoten podatkovni model.
- Osnovna normalizacija besedila (odstranitev odvečnih presledkov, praznih vrednosti).
- Shranjevanje rezultata v lokalno datoteko JSON.
- Obravnava neuspešnega iskanja, sprememb spletnega prikaza in tehničnih napak.
- Testiranje ter dokumentacija zagona in rezultatov.

### Ni vključeno

- Neposredno klicanje ARES REST API-ja ali drugih zunanjih API-jev.
- Razvoj lastnega REST API-ja oziroma kontrolerjev projekta IVAS.
- Razčlenjevanje računov UBL XML in primerjava njihovih polj z registrskimi podatki.
- Preverjanje veljavnosti DDV številke prek VIES.
- Preverjanje francoskih podjetij.
- Samodejno množično pregledovanje celotnega registra ali obhod zaščit spletnega mesta.

**Meja implementacije:** Playwright je dovoljen za navigacijo in branje DOM-a oziroma vidne vsebine. Odgovorov internih omrežnih/API zahtevkov strani ne bomo prestrezali ali uporabljali kot vir podatkov za parser.

## 3. Podatkovni vir

- **Register:** ARES (Administrativní registr ekonomických subjektů).
- **Upravljavec:** Ministrstvo za finance Češke republike.
- **Uradni spletni portal:** <https://ares.gov.cz/>
- **Izhodiščni naslov za iskanje:** `https://ares.gov.cz/ekonomicke-subjekty?ico={ICO}`
- **Primer identifikatorja za razvoj:** `27082440` (Alza.cz a.s.).

Spletni portal se lahko spreminja. Dejanske lokatorje elementov je treba pred implementacijo preveriti v lokalno zagnanem brskalniku. Vnaprej niso določeni neresnični ali nepreverjeni CSS selektorji.

Informacije iz ARES so informativne narave. Razčlenjevalnik ni nadomestilo za uradno potrdilo iz registra ali preverjanje veljavnosti DDV identifikatorja.

## 4. Predvidene tehnologije

| Tehnologija | Namen |
| --- | --- |
| Java v obstoječem projektu IVAS | Implementacija programske logike |
| Maven (`pom.xml` glavnega projekta) | Upravljanje odvisnosti in gradnja |
| Playwright for Java | Zagon brskalnika, navigacija, čakanje na izris in dostop do DOM-a |
| Jackson | Serializacija podatkov v JSON |
| JUnit | Testiranje validacije, normalizacije in obdelave rezultatov |

Predviden je brskalnik Chromium v načinu *headless* (za razvoj bo mogoča tudi vidna izvedba). Verzije odvisnosti določimo ob implementaciji glede na združljivost z obstoječim `pom.xml`.

## 5. Vhodni podatki

Primarni vhod je `ico`, podan ob zagonu programa.

| Podatek | Primer | Zahteva |
| --- | --- | --- |
| `ico` | `27082440` | Niz natanko osmih števk; ohranijo se vodilne ničle |

Predviden je samostojen zagon razčlenjevalnika, brez potrebe po zagonu strežnika Spring Boot. Natančen ukaz za zagon in ime zagonskega razreda bosta določena v implementacijskem commitu ter naknadno dodana v ta dokument.

## 6. Podatki, ki jih želimo pridobiti

| Podatek v JSON | Opis | Zahtevnost |
| --- | --- | --- |
| `company.ico` | Registrski identifikator podjetja | Obvezen |
| `company.name` | Uradni naziv podjetja | Obvezen |
| `company.vatId` | Prikazana češka davčna identifikacijska številka (DIČ), če je na strani na voljo | Neobvezen |
| `company.registeredAddress` | Prikazani registrirani naslov/sedež | Neobvezen |
| `company.legalForm` | Prikazana pravna oblika | Neobvezen |

Neobveznih podatkov ni dovoljeno ugibati ali izračunati. Če polje ni prikazano oziroma ga ni mogoče zanesljivo prebrati, se shrani kot `null`. Zlasti **DIČ ni samodejno enak `CZ` + IČO** in prisotnost DIČ ne dokazuje veljavnosti za DDV.

## 7. Predlagana oblika izhodne datoteke JSON

Spodnji JSON je **primer predvidene strukture**, ne rezultat že izvedenega razčlenjevanja. Polja z vrednostjo `null` namenoma ponazarjajo podatke, ki jih parser morda ne bo mogel prebrati.

```json
{
  "schemaVersion": "1.0",
  "metadata": {
    "source": "ARES_WEB",
    "method": "HTML_DOM",
    "sourceUrl": "https://ares.gov.cz/ekonomicke-subjekty?ico=27082440",
    "extractedAt": "2026-10-23T17:00:00Z"
  },
  "query": {
    "ico": "27082440"
  },
  "status": "SUCCESS",
  "company": {
    "ico": "27082440",
    "name": "Alza.cz a.s.",
    "vatId": null,
    "registeredAddress": null,
    "legalForm": null
  },
  "missingFields": [
    "vatId",
    "registeredAddress",
    "legalForm"
  ],
  "errors": []
}
```

**Predvidena lokacija izvoza:** `data/ares/<ico>.json` glede na korensko mapo repozitorija. Program naj po potrebi ustvari manjkajočo mapo. Ali bodo generirane datoteke vključene v Git, določimo ob implementaciji; privzeto naj ne prihaja do nenamernega dodajanja velikih količin generiranih podatkov.

### Pravila interpretacije statusov

| Status | Pomen |
| --- | --- |
| `SUCCESS` | Podjetje je najdeno, IČO in naziv sta prebrana; neobvezna polja lahko manjkajo |
| `PARTIAL` | Podjetje je očitno najdeno, vendar ni mogoče zanesljivo prebrati vseh obveznih polj |
| `NOT_FOUND` | Spletna stran izrecno in nedvoumno sporoči, da ni zadetkov |
| `ERROR` | Tehnična napaka: nedosegljivost, prekinitev, časovna omejitev ali neprepoznana struktura prikaza |

Pri statusih `PARTIAL`, `NOT_FOUND` ali `ERROR` je `company` lahko delno izpolnjen ali `null`, `errors` pa vsebuje opis oziroma kodo težave. **Tehnične napake nikoli ne obravnavamo kot dokaz, da podjetje ne obstaja.**

## 8. Predviden potek razčlenjevanja

1. Uporabnik poda identifikator IČO.
2. Program preveri njegovo obliko.
3. Playwright zažene Chromium in odpre uradni spletni prikaz ARES.
4. Program počaka na prepoznaven rezultat iskanja ali jasno sporočilo o napaki.
5. Iz izrisanega HTML/DOM prebere razpoložljiva registrska polja.
6. Podatke normalizira in zabeleži manjkajoča polja.
7. Sestavi podatkovni objekt z virom, časom zajema in statusom.
8. Jackson zapiše rezultat v JSON datoteko.
9. Brskalnik in druge sistemske vire program pravilno zapre tudi ob napaki.

Pri čakanju na vsebino imajo prednost smiselni Playwright lokatorji in preverjanje stanja strani pred fiksnimi časovnimi zamiki. Konkretni lokatorji bodo določeni po dejanskem ogledu DOM-a.

## 9. Preizkusi in merila uspešnosti

| Scenarij | Pričakovano ravnanje |
| --- | --- |
| Veljavno IČO z obstoječim podjetjem (`27082440`) | Zapis JSON z ustrezno prebranima IČO in nazivom |
| IČO z vodilnimi ničlami (`00006947`) | Vodilne ničle se ohranijo |
| Napačna oblika IČO (črke, napačna dolžina) | Jasna validacijska napaka brez odpiranja strani |
| IČO, za katerega spletni prikaz izrecno ne vrne zadetka | Status `NOT_FOUND` |
| Neobvezno polje ni prikazano | Vrednost `null`, polje je navedeno v `missingFields` |
| Nedosegljiva stran ali časovna omejitev | Status `ERROR`; brez lažne trditve, da podjetje ne obstaja |
| Spremenjena struktura strani | Varna obravnava neprepoznane vsebine; sporočilo o napaki |
| Ponovni zagon z istim IČO | Predvidljivo prepisovanje oziroma posodobitev izhodne datoteke |

Zaključek naloge pomeni, da je mogoče z enim vhodnim IČO pridobiti in lokalno shraniti strukturiran JSON, da obstajajo avtomatizirani testi osnovne obdelave ter da sta zagon in omejitve postopka dokumentirana.

## 10. Omejitve in odgovorna uporaba vira

- Spletni prikaz se lahko spremeni, kar lahko zahteva posodobitev lokatorjev.
- Spletna stran je lahko začasno nedosegljiva, počasna ali zaščitena pred avtomatizacijo.
- Brez množičnih, vzporednih ali ponavljajočih se poizvedb, ki bi po nepotrebnem obremenjevale ARES.
- Brez obhajanja zaščitnih mehanizmov, omejitev dostopa ali CAPTCHA.
- Če spletna stran uporabi JavaScript za izris podatkov, je dovoljeno čakanje in branje končnega DOM-a; neposredno branje API odgovorov ni del naloge.
- Izluščene vrednosti so informativne in niso preverjanje veljavnosti za davčne namene.

## 11. Povezava z drugimi nalogami in IVAS

Ta dokument opisuje funkcionalnost, implementirano oziroma načrtovano **v okviru Naloge 2**. Njena odgovornost je omejena na pridobitev podatkov s spletne strani in shranjevanje JSON.

V prihodnjih nalogah bo mogoče uporabiti rezultat oziroma podatkovni model za:

- primerjavo registrskega naziva in IČO dobavitelja s podatki iz UBL 2.1 računa;
- integracijo z uradnimi zunanjimi API-ji (ARES, VIES in francoski register), ne da bi jih implementirali že v tej nalogi;
- tvorjenje ugotovitev in priporočil v končnem poročilu IVAS;
- morebitno nadomestitev spletnega razčlenjevanja z zanesljivejšim podatkovnim virom, ob ohranitvi enotnega izhodnega modela.

Pri razvoju prihodnje API-integracije naj se dokumentacija sklicuje na ta dokument z relativno potjo: [`docs/Naloga2_Spletni_Parser.md`](Naloga2_Spletni_Parser.md).

**Pomembno:** Izhodni JSON Naloge 2 predstavlja registrske podatke, ne rezultata preverjanja UBL računa. Ne vsebuje končne ocene `assessment`, `checks`, `findings` ali `recommendedAction` sistema IVAS.

## 12. Predvidena organizacija kode

Implementacija ostane znotraj obstoječega `src/main/java/um/feri/ipt/tids/project/`. Predlagani paket je `parser.ares`, z razredi za:

- upravljanje brskalnika in HTML razčlenjevanje;
- podatkovni model podjetja;
- normalizacijo in izvoz JSON;
- samostojen zagon parserja.

Uporabi se obstoječi korenski `pom.xml`; **ne ustvarja se dodatnega Maven projekta**. Dokončna imena razredov in njihovih paketov bodo dokumentirana po implementaciji.

## 13. Literatura in viri

1. Ministrstvo za finance Češke republike – uradni opis ARES in pogoji uporabe: <https://mf.gov.cz/cs/ministerstvo/informacni-systemy/ares>
2. ARES – uradni spletni prikaz: <https://ares.gov.cz/>
3. Playwright for Java – namestitev in osnovna uporaba: <https://playwright.dev/java/docs/intro>
4. Playwright for Java – lokatorji: <https://playwright.dev/java/docs/locators>

---

*Dokument je začetna specifikacija Naloge 2. Izvedbeni podatki (preverjeni lokatorji, ukaz za zagon, različice knjižnic in dejanski testni rezultati) se dopolnijo po implementaciji.*
