package com.labtrack.model.enums;

/*
    Predviden zivljenski cikel ustreznega vzorca: REGISTERED -> RECIVED -> ACCEPTED -> STORED -> DISPOSED
 */

public enum SampleStatus {
    REGISTERED,           // Vzorec je evidentiran v sistemu temvec se ne pomeni, da je prispe lv laboratorij
    RECEIVED,             // Vzorec je fizicno prispel v laboratorij
    ACCEPTED,             // Laboratorij je potrdil, da je primeren za nadaljno obdelavo
    REJECTED,             // Vzorec ni primeren za nadaljno analizo, na primer poskodovana embalaza, neustreznega materiala, ...
    STORED,               // Vzorece je shranjen na doloceni laboratorijski lokaciji
    DISPOSED,             // Vzorec je po ustreznem postopku odstranjen
}
