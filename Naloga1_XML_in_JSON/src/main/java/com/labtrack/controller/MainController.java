package com.labtrack.controller;

import com.labtrack.parser.XmlDataLoader;
import com.labtrack.service.LaboratoryService;
import com.labtrack.service.validation.ValidationService;

public class MainController {
    private void loadData() {
        XmlDataLoader loader = new XmlDataLoader();
        ValidationService validator = new ValidationService();

        LaboratoryService service = new LaboratoryService(loader, validator);

    }
}