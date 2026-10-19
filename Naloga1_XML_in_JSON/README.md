# LabTrack

LabTrack je projekt, inspiriran z laboratorijskimi informacijskimi sistemi (LIMS), kakršne uporabljajo podjetja s področja genetike in biomedicine, kot je GenePlanet.

Namenjen je spremljanju laboratorijskih vzorcev, vrst genetskih testov in pripadajočih analiz ter omogoča pregledovanje, povezovanje in filtriranje podatkov.

Podatki so razdeljeni v tri povezane XML dokumente:

- `samples.xml` – podatki o laboratorijskih vzorcih.
- `test_types.xml` – podatki o vrstah laboratorijskih testov.
- `analyses.xml` – podatki o izvedbi analiz, njihovih statusih in kontroli kakovosti.

Aplikacija omogoča filtriranje analiz glede na status, kategorijo testa, rezultat kontrole kakovosti in datumski interval. Filtre je mogoče medsebojno kombinirati, filtrirane rezultate pa izvoziti v formata **JSON** in **XML**.

## Zagon aplikacije

Za delovanje aplikacije je potrebna **Java 21**.

### 1. Zagon z Mavenom

V korenski mapi projekta izvedemo:

```bash
mvn clean compile exec:java
```

### 2. Zagon z JAR datoteko

Najprej z Mavenom ustvarimo izvršljivo JAR datoteko:

```bash
mvn clean package
```

Nato aplikacijo zaženemo z ukazom:

```bash
java -jar target/labtrack-1.0.0-SNAPSHOT.jar
```

Pri zagonu z JAR datoteko mora biti mapa `data/` z vhodnimi XML dokumenti dostopna v trenutnem delovnem imeniku.

## Izvoz podatkov

Aplikacija omogoča izvoz trenutno filtriranih analiz v dva formata:

- **JSON** – `exports/filtrirano.json`
- **XML** – `exports/filtrirano.xml`

Če filtri niso uporabljeni, je mogoče izvoziti tudi vse analize. Izhodni datoteki se samodejno ustvarita v mapi `exports/`.