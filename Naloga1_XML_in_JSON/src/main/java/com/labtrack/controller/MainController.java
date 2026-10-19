package com.labtrack.controller;

import com.labtrack.service.LaboratoryService;
import com.labtrack.service.validation.ValidationResult;
import com.labtrack.service.validation.ValidationService;

import java.nio.file.Path;

public class MainController {

    private final LaboratoryService service =
            new LaboratoryService(new ValidationService());

    public void loadData() {

        ValidationResult result = service.loadData(
                Path.of("data")
        );

        if (!result.isValid()) {
            System.err.println("Loading failed:");
            result.errors().forEach(System.err::println);
            return;
        }

        System.out.println("Data loaded successfully!");
        System.out.println("Samples: " + service.getSamples().size());
        System.out.println("Tests: " + service.getTestTypes().size());
        System.out.println("Analyses: " + service.getAnalyses().size());
        System.out.println("Views: " + service.getAnalysisViews().size());

        service.getAnalysisViews().forEach(view ->
                System.out.printf(
                        "%s | %s | %s | %s | %s%n",
                        view.getAnalysisId(),
                        view.getSampleType(),
                        view.getTestName(),
                        view.getStatus(),
                        view.getQcResult()
                )
        );
    }
}