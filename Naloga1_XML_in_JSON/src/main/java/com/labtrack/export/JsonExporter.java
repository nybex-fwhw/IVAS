
package com.labtrack.export;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.labtrack.model.AnalysisView;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class JsonExporter {

    private JsonExporter() {}

    public static void export(
            List<AnalysisView> analyses,
            Path outputPath
    ) throws IOException {

        Objects.requireNonNull(analyses);
        Objects.requireNonNull(outputPath);

        List<Map<String, Object>> records = new ArrayList<>();

        for (AnalysisView analysis : analyses) {
            Map<String, Object> record = new LinkedHashMap<>();

            record.put("analysisId", analysis.getAnalysisId());
            record.put("sampleId", analysis.getSampleId());
            record.put("sampleType", analysis.getSampleType());
            record.put("testId", analysis.getTestId());
            record.put("testName", analysis.getTestName());
            record.put("category", analysis.getCategory());
            record.put("startDate", dateText(analysis.getStartDate()));
            record.put("completionDate", dateText(analysis.getCompletionDate()));
            record.put("reviewDate", dateText(analysis.getReviewDate()));
            record.put("status", analysis.getStatus());
            record.put("qcResult", analysis.getQcResult());

            records.add(record);
        }

        Path parent = outputPath.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        mapper.writeValue(outputPath.toFile(), records);
    }

    private static String dateText(LocalDate date) {
        return date == null ? null : date.toString();
    }
}
