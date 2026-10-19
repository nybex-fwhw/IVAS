package com.labtrack.service.filter;

import com.labtrack.model.enums.AnalysisStatus;
import com.labtrack.model.enums.TestCategory;
import com.labtrack.model.enums.QcResult;

import java.time.LocalDate;

public record AnalysisFilter(
        AnalysisStatus status,
        TestCategory category,
        QcResult qcResult,
        LocalDate dateFrom,
        LocalDate dateTo
) {
    public AnalysisFilter {
        if (dateFrom != null && dateTo != null
                && dateFrom.isAfter(dateTo)) {
            throw new IllegalArgumentException(
                    "Start date cannot be after end date."
            );
        }
    }
}