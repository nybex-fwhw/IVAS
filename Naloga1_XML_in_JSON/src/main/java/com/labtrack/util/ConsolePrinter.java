
package com.labtrack.util;

import com.labtrack.model.AnalysisView;

import java.util.List;

public final class ConsolePrinter {

    private ConsolePrinter() {}

    public static void printAnalyses(List<AnalysisView> analyses) {

        if (analyses == null || analyses.isEmpty()) {
            System.out.println("No analyses to display.");
            return;
        }

        String format = "%-18s %-15s %-27.27s %-16s %-12s%n";

        System.out.printf(format,
                "ANALYSIS ID", "SAMPLE TYPE", "TEST NAME", "STATUS", "QC RESULT");

        System.out.println("-".repeat(94));

        for (AnalysisView analysis : analyses) {
            System.out.printf(format,
                    analysis.getAnalysisId(),
                    analysis.getSampleType(),
                    analysis.getTestName(),
                    analysis.getStatus(),
                    analysis.getQcResult()
            );
        }

        System.out.println("-".repeat(94));
        System.out.println("Total results: " + analyses.size());
    }
}
