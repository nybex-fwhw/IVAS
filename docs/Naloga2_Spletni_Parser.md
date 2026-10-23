# Naloga 2 – Spletni razčlenjevalnik podatkov iz registra ARES

> **Projekt:** IVAS – predhodno preverjanje strukturiranih elektronskih računov  
> **Status:** implementirano; preverjeno na uradnem izrisanem DOM-u ARES ter z Maven testi in gradnjo JAR
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

*Poglavja 1–13 ohranjajo specifikacijo Naloge 2. Trenutno izvedbo, ukaze, omejitve in dejanske rezultate opisuje poglavje 14.*

## 14. Implementacija in preverjanje (23. 10. 2026)

### Trenutno stanje

Implementacija je v paketu `um.feri.ipt.tids.project.parser.ares` glavnega projekta.
Java 25 in Spring Boot 4.1.1 ostajata nespremenjena. Uporabljena sta že obstoječa
Playwright 1.64.0 in Jackson 3 (`tools.jackson`), katerega različico upravlja Spring Boot.
Podvojena odvisnost JUnit API 6.1.3 je odstranjena: `spring-boot-starter-test` zagotovi
usklajena JUnit API in engine 6.0.3.

**Uradni izrisani DOM je bil preverjen 23. 10. 2026 s Playwright Java in Chrome headless.**
Po odobritvi omrežnega dostopa sta živa primera `27082440` (Alza.cz a.s.) in
`00006947` (Ministerstvo financí) vrnila `SUCCESS`, naslov in prikazano pravno obliko.
Vodilne ničle so ohranjene. Iskanje `99999999` je bilo uporabljeno za preverjanje
izrecnega praznega rezultata. Podatkovni vir je v vseh primerih izrisani HTML;
API odgovori niso bili prebrani ali prestreženi.

### Organizacija

| Razred | Odgovornost |
| --- | --- |
| `Ico` | Natanko osem ASCII števk, brez krajšanja, dopolnjevanja ali izgube vodilnih ničel |
| `AresScraper` | Izoliran Playwright/Chromium, navigacija, roki in tehnične napake; viri se zaprejo s try-with-resources |
| `AresDomParser` | Branje vidnega DOM-a, izbira točno zahtevanega IČO, normalizacija in status |
| `AresResult` | Nespremenljiv rezultat in podatkovni model s strukturo iz poglavja 7 |
| `AresJsonExporter` | UTF-8 JSON, ustvarjanje map in zamenjava datoteke po uspešnem zapisu začasne datoteke |
| `AresParserCli` | Samostojen zagonski razred, argumenti in izhodne kode; brez zagona strežnika |

Na uradni strani so potrjene vidne vrstice `table[role="table"]`, glave `th[role="columnheader"]`
in celice `td[role="cell"]` z glavami `IČO`, `Obchodní název`, `Právní forma` in `Sídlo`.
Edini potreben dodatek za uspešen zajem podjetij je bila oznaka `Obchodní název`.
Obstoječe rezervne oznake `Obchodní jméno`, `Obchodní firma`, `Název`, `DIČ` in `Adresa sídla`
ostajajo podprte, vendar na uradni iskalni tabeli niso bile potrebne. Podprti so tudi pari `dt`/`dd` in vidni sosednji
elementi oznaka/vrednost (`label`, `span`, `div`, `th`). Podatki iz različnih vrstic
ali ločenih vsebinskih skupin se ne združujejo. Nasprotujoča si naziva pomenita napako.
Glave znotraj `thead` se ne obravnavajo kot pari oznaka/vrednost.

Prazen rezultat uradna stran prikaže kot prazno vrstico
`[data-pc-section="emptymessage"]` in vidni števec `Zobrazeno 0 až 0 z 0` znotraj
`[data-pc-name="datatable"] [data-pc-name="paginator"] [data-pc-section="current"]`.
Parser zahteva prepoznani glavi IČO in naziva, vidni prazni rezultat in števec ter
odsotnost vidnega indikatorja nalaganja ali opozorila. Sam prazen DOM ali besedilo
`Žádné výsledky` v izbirnikih filtra ne pomeni `NOT_FOUND`.

Parser uporablja `innerText` in preveri vidnost elementov. Ne bere skritih JSON podatkov,
globalnega stanja aplikacije ali teles omrežnih odgovorov. Preveri samo HTTP status vrhnjega
HTML dokumenta. REST zahtevkov strani ne kliče in jih ne prestreza. Neobveznih polj ne
izračunava; predvsem ne tvori DIČ iz IČO. Validacija IČO je skladno s specifikacijo
validacija oblike, ne izračun kontrolne številke ali dokaz obstoja subjekta.

### Predpogoji in priprava

Potrebni so JDK 25, omrežni dostop do Maven Central za manjkajoče odvisnosti ter do
uradnega ARES, in Playwright Chromium ali nameščen Chrome/Edge. Ukaze izvajamo iz korena
IVAS. Naslednja priprava PowerShell usmeri prenose, Maven repozitorij, brskalnike in
začasne datoteke v delovno mapo:

```powershell
New-Item -ItemType Directory -Force .local/tmp | Out-Null
$env:MAVEN_USER_HOME = "$PWD/.local/maven"
$env:PLAYWRIGHT_BROWSERS_PATH = "$PWD/.local/browsers"
$env:TEMP = "$PWD/.local/tmp"
$env:TMP = $env:TEMP
$repo = "-Dmaven.repo.local=$PWD/.local/repository"
```

Namestitev Chromiuma s Playwright CLI (potrebuje omrežje in Maven exec plugin):

```powershell
.\mvnw.cmd $repo compile exec:java '-Dexec.mainClass=com.microsoft.playwright.CLI' '-Dexec.args=install chromium'
```

Ob nameščenem Chromu je mogoče namestitev Chromiuma izpustiti in uporabiti `--channel chrome`.
Za Edge je na voljo `--channel msedge`. Brez `--channel` se uporabi Playwright Chromium.

### Gradnja in uporaba

```powershell
.\mvnw.cmd $repo -Pares package
java -jar target/project-0.0.1-SNAPSHOT.jar 27082440
java -jar target/project-0.0.1-SNAPSHOT.jar 00006947 --channel chrome --headed --timeout-seconds 45 --output data/ares/00006947.json
java -jar target/project-0.0.1-SNAPSHOT.jar --help
```

Profil `ares` izbere `AresParserCli` kot zagonski razred. Brez profila ostane glavni razred
`ProjectApplication`. Gradnja s profilom `ares` je potrjena z Maven `verify`, vključno
s Spring Boot prepakiranjem odvisnosti v izvršljiv JAR. Pri uporabi nameščenega Chroma
lahko nastavimo `$env:PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD = '1'`, da Playwright ob prvem
zagonu ne prenaša dodatnih brskalnikov.

Preverjen je tudi neposreden zagon JAR z vgrajenim Playwright driverjem, brez
`PLAYWRIGHT_DRIVER_DIR`: uspešen zajem vrne kodo `0`, izrecno prazno iskanje pa `3`.
Preverjeni parser JAR je po dodatni gradnji privzetega profila ohranjen kot
`target/ivas-ares.jar`; privzeti `target/project-0.0.1-SNAPSHOT.jar` po gradnji brez
`-Pares` vsebuje `ProjectApplication`. Naslednja gradnja s `-Pares` ponovno ustvari
parser pod standardnim imenom. Oba zagonska razreda sta preverjena v manifestih JAR.

Privzeto se zapisuje `data/ares/<ico>.json`. Ponovni zagon zamenja prejšnji rezultat,
tudi če je nov rezultat `ERROR`. Do uspešnega zapisa ostane stara datoteka nedotaknjena.
Zamenjava je atomska, kjer jo datotečni sistem podpira, sicer se uporabi običajna zamenjava.
Generirani podatki in `.local/` so izključeni iz Git. Izhod je lepo oblikovan UTF-8 JSON
po poglavju 7; vsebuje dejanski čas UTC, začetni URL poizvedbe in `null` za manjkajoča
neobvezna polja. Primer v poglavju 7 je še vedno ilustracija, ne živ rezultat.

| Izhodna koda | Pomen |
| --- | --- |
| `0` | `SUCCESS` ali izpis pomoči |
| `1` | `ERROR` ali napaka zapisa JSON |
| `2` | Neveljavni argumenti; brskalnik se ne odpre in JSON se ne zapiše |
| `3` | Izrecni `NOT_FOUND` |
| `4` | `PARTIAL` |

Prepoznane kode težav so `BROWSER_ERROR`, `TIMEOUT`, `HTTP_ERROR`, `UNRECOGNIZED_DOM`,
`AMBIGUOUS_RESULT`, `MISSING_REQUIRED_FIELD` in `NOT_FOUND`. Neprepoznan prikaz po izteku
čakanja pomeni `UNRECOGNIZED_DOM`, ne odsotnosti podjetja. Delni zapis z IČO brez naziva
ostane `PARTIAL`. `NOT_FOUND` zahteva izrecno prikazano podprto sporočilo o praznem rezultatu.
Ob odsotnosti ali spremembi takega sporočila parser varno vrne `ERROR`.

### Avtomatizirani testi

```powershell
# Osnovni testi in obstoječi Spring context test; brez dostopa do ARES
.\mvnw.cmd $repo test

# Sintetični HTML v pravem brskalniku, brez zunanjih zahtevkov
.\mvnw.cmd $repo '-Dares.browser.tests=true' '-Dares.browser.channel=chrome' test

# Izrecno omrežno preverjanje uradnega izrisanega DOM-a, trije primeri
.\mvnw.cmd $repo '-Dares.live.tests=true' '-Dares.browser.channel=chrome' '-Dtest=AresLiveTests' test

# Celotna zbirka, živi primeri in gradnja izvršljivega parserja
.\mvnw.cmd $repo '-Dares.browser.tests=true' '-Dares.live.tests=true' '-Dares.browser.channel=chrome' -Pares verify
```

`AresCoreTests` pokrije obliko IČO, Unicode presledke, argumente CLI, zavrnitev napačnih
vhodov pred zagonom brskalnika, JSON in ponovno zapisovanje. `AresBrowserTests` pokrije
izbiro vrstice po IČO, vodilne ničle, odsotnost DIČ, ločitev statusov, skrite elemente,
nasprotujoča si naziva, skupine oznak in vrednosti ter naknaden izris JavaScript.
Dodani regresiji temeljita na dejansko pregledani strukturi uradne tabele in števca
praznih rezultatov. Preverita tudi, da nalaganje, opozorilo ali skriti števec ne pomenijo
praznega rezultata.
`AresLiveTests` se privzeto ne izvaja in shrani rezultate v `target/ares-live/`.

Dejanski rezultati preverjanja (JDK 25.0.4.1, Maven 3.10.0, Chrome headless):

| Preverjanje | Rezultat |
| --- | --- |
| Maven `test-compile`, Java 25 | **PASS**, glavna koda in vsi testni razredi se prevedejo |
| `AresCoreTests` | **4 PASS**, JUnit prek Maven Surefire |
| `AresBrowserTests` | **8 PASS**, vključno z regresijama uradnega prikaza |
| `AresLiveTests` | **2 PASS**, živa zajema podjetij in izrecni prazni rezultat; rezultati v `target/ares-live/` |
| `ProjectApplicationTests` | **1 PASS**, obstoječi Spring context test |
| Maven `verify -Pares` z obema opt-in zastavicama | **BUILD SUCCESS**, 15 testov, 0 napak, 0 preskočenih; izvršljiv JAR z odvisnostmi |
| Maven Wrapper `verify` brez profila in brez opt-in zastavic | **BUILD SUCCESS**, 5 izvedenih testov, 10 pravilno preskočenih; privzeti Spring Boot JAR |
| Izvršljiv parser JAR, vgrajeni Playwright driver | **PASS**, `27082440`: `SUCCESS` / izhod `0`; `99999999`: `NOT_FOUND` / izhod `3`; JSON v `target/ares-jar/` |
| CLI ob blokiranem omrežju, pred odobritvijo dostopa | **PASS**, izhodna koda `1`, JSON `ERROR` / `BROWSER_ERROR`, `company: null` |

Prvi poskus v omejenem peskovniku je dopuščal le prevajanje in neposreden zagon
desetih testnih metod; omrežje, odvisnosti in Git zapisi so bili blokirani. Nadaljevanje
je uporabilo odobren dostop za konkretne ukaze, obstoječi Maven samo za branje ter JDK,
Maven repozitorij, Playwright driver in začasne datoteke znotraj IVAS. Končno preverjanje
uporablja pravi Maven/Surefire/JUnit življenjski cikel. Pri dodajanju praznega rezultata
sta nova regresijska in živa testa odkrila napačno obravnavo glav tabele kot podrobnosti;
izključitev `thead` to popravi.
Poročila celotnega uspešnega izvajanja so ohranjena v `target/verification/full-suite/`,
ker naslednji privzeti zagon posodobi `target/surefire-reports/` s preskočenimi opt-in testi.

Potrjena izhodna podatka (čas je čas konkretnega zajema, ne stalna vrednost):

| IČO | Naziv | Naslov | Prikazana pravna oblika | DIČ |
| --- | --- | --- | --- | --- |
| `27082440` | Alza.cz a.s. | Jankovcova 1522/53, Holešovice, 17000 Praha 7 | `121` | `null` |
| `00006947` | Ministerstvo financí | Letenská 525/15, Malá Strana, 11800 Praha 1 | `325` | `null` |

Polje `legalForm` ohrani kodo, ker jo iskalna tabela prikazuje kot kodo. `vatId` ostane
`null`, ker DIČ ni prikazan kot polje tabele; parser ga ne sklepa iz povezave DPH.

### Preostale omejitve

Spremembe uradnega prikaza lahko zahtevajo ponovno preverjanje lokatorjev. Podatki, prikazani samo na drugem pogledu ali
za uporabniškim dejanjem, se trenutno ne pridobijo; ostanejo `null`. Parser ne izvaja
samodejnih ponovnih zahtevkov, množičnega zajema, obhoda zaščit, preverjanja DDV ali
validacije računov. Mapa `Naloga1_XML_in_JSON` ni bila spremenjena.
