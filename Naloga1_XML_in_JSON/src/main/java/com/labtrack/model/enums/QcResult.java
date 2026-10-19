package com.labtrack.model.enums;

public enum QcResult {
    NOT_TESTED,          // Kontrola kakovosti se ni bila izvedena
    PENDING,             // Kontrola kakovosti poteka oz. caka na potrditev
    PASS,                // Analiza je uspesno prestala kontrolo kakovosti
    FAIL,                // Analiza ni prestala kontrole kakovosti
    INCONCLUSIVE,        // Iz razporozljivih podatkov ni mogoce zanesljivo dolociti ali je kontrola uspesna
}
