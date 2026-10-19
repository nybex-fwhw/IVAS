
package com.labtrack.console;

import com.labtrack.model.AnalysisView;
import com.labtrack.model.enums.AnalysisStatus;
import com.labtrack.model.enums.QcResult;
import com.labtrack.model.enums.TestCategory;
import com.labtrack.service.LaboratoryService;
import com.labtrack.service.FilterService;
import com.labtrack.service.filter.AnalysisFilter;
import com.labtrack.service.validation.ValidationResult;
import com.labtrack.service.validation.ValidationService;
import com.labtrack.util.ConsolePrinter;
import com.labtrack.export.JsonExporter;
import com.labtrack.export.XmlExporter;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class ConsoleApp {

    private final LaboratoryService service =
            new LaboratoryService(new ValidationService());

    private final FilterService filterService = new FilterService();
    private final Scanner scanner = new Scanner(System.in);

    private List<AnalysisView> currentResults = List.of();
    private boolean dataLoaded = false;

    public void run() {
        while (true) {
            printMenu();
            String option = scanner.nextLine().trim();

            try {
                switch (option) {
                    case "1" -> loadData();
                    case "2" -> showAll();
                    case "3" -> filterAnalyses();
                    case "4" -> exportJson();
                    case "5" -> exportXml();
                    case "0" -> {
                        System.out.println("Goodbye!");
                        return;
                    }
                    default -> System.out.println("Invalid option.");
                }
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }

    private void printMenu() {
        System.out.println("""
                
                ===== LABTRACK =====
                1. Load XML data
                2. Show all analyses
                3. Filter analyses
                4. Export JSON
                5. Export XML
                0. Exit
                """);
        System.out.print("Choose option: ");
    }

    private void loadData() {
        ValidationResult result = service.loadData(Path.of("data"));

        if (!result.isValid()) {
            System.err.println("Loading failed:");
            result.errors().forEach(System.err::println);
            return;
        }

        dataLoaded = true;
        currentResults = service.getAnalysisViews();

        System.out.println("Data loaded successfully.");
        System.out.println("Analyses: " + currentResults.size());
    }

    private boolean requireData() {
        if (!dataLoaded) {
            System.out.println("Please load data first (option 1).");
            return false;
        }
        return true;
    }

    private void showAll() {
        if (!requireData()) return;

        currentResults = service.getAnalysisViews();
        ConsolePrinter.printAnalyses(currentResults);
    }


    private void filterAnalyses() {
        if (!requireData()) {
            return;
        }

        System.out.println("\n===== ANALYSIS FILTERS =====");
        System.out.println("Press ENTER to skip any filter.\n");

        try {
            AnalysisStatus status = readEnum(
                    "Status",
                    AnalysisStatus.class
            );

            TestCategory category = readEnum(
                    "Category",
                    TestCategory.class
            );

            QcResult qcResult = readEnum(
                    "QC result",
                    QcResult.class
            );

            LocalDate dateFrom = readDate("Start date FROM");
            LocalDate dateTo = readDate("Start date TO");

            AnalysisFilter filter = new AnalysisFilter(
                    status,
                    category,
                    qcResult,
                    dateFrom,
                    dateTo
            );

            currentResults = filterService.filter(
                    service.getAnalysisViews(),
                    filter
            );

            System.out.println("\nMatching analyses: "
                    + currentResults.size());

            ConsolePrinter.printAnalyses(currentResults);

        } catch (IllegalArgumentException e) {
            System.err.println("Invalid filter: " + e.getMessage());
            System.out.println("Previous results were preserved.");
        }
    }

    private LocalDate readDate(String label) {

        System.out.print(
                label + " (yyyy-MM-dd, ENTER = All): "
        );

        String input = scanner.nextLine().trim();

        if (input.isEmpty()) {
            return null;
        }

        try {
            return LocalDate.parse(input);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Invalid date for " + label +
                            ". Expected yyyy-MM-dd, received: " + input
            );
        }
    }


    private <E extends Enum<E>> E readEnum(
            String label,
            Class<E> enumType
    ) {
        System.out.println("\nAvailable " + label + " values:");

        for (E value : enumType.getEnumConstants()) {
            System.out.println(" - " + value);
        }

        System.out.print(label + " (ENTER = All): ");

        String input = scanner.nextLine()
                .trim()
                .toUpperCase(Locale.ROOT);

        if (input.isEmpty()) {
            return null;
        }

        try {
            return Enum.valueOf(enumType, input);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Unknown " + label + ": " + input
            );
        }
    }



    private void exportJson() throws Exception {
        if (!requireData()) return;

        Path path = Path.of("exports/filtrirano.json");
        JsonExporter.export(currentResults, path);

        System.out.println("JSON exported to: " + path);
    }

    private void exportXml() throws Exception {
        if (!requireData()) return;

        Path path = Path.of("exports/filtrirano.xml");
        XmlExporter.export(currentResults, path);

        System.out.println("XML exported to: " + path);
    }
}
