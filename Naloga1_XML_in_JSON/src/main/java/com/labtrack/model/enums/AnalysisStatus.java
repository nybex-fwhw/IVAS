package com.labtrack.model.enums;

public enum AnalysisStatus {
    PENDING,                // Analiza je narocena oz. ustvarjena v sistemu, vendar se se ni zacela
    IN_PROGRESS,            // Laboratorijska obdelava poteka
    QUALITY_CHECK,          // Preverja se kakovost izvedene analize oz. pridobljenih podatkov
    COMPLETED,              // Analiza je uspesno zakljucena
    FAILED,                 // Analize ne bilo mogoce uspesno zakljuciti
    CANCELLED,              // Analiza je bila preklicana
}