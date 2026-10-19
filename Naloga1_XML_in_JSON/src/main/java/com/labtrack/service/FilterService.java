package com.labtrack.service;

import com.labtrack.model.AnalysisView;
import com.labtrack.service.filter.AnalysisFilter;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public class FilterService {

    public List<AnalysisView> filter(
            List<AnalysisView> analyses,
            AnalysisFilter filter
    ) {
        Objects.requireNonNull(analyses, "Analyses cannot be null.");
        Objects.requireNonNull(filter, "Filter cannot be null.");

        return analyses.stream()
                .filter(Objects::nonNull)
                .filter(a -> filter.status() == null
                        || a.getStatus() == filter.status())
                .filter(a -> filter.category() == null
                        || a.getCategory() == filter.category())
                .filter(a -> filter.qcResult() == null
                        || a.getQcResult() == filter.qcResult())
                .filter(a -> isWithinDateRange(
                        a.getStartDate(),
                        filter.dateFrom(),
                        filter.dateTo()
                ))
                .toList();
    }

    private boolean isWithinDateRange(
            LocalDate date,
            LocalDate from,
            LocalDate to
    ) {
        if (from == null && to == null) {
            return true;
        }

        if (date == null) {
            return false;
        }

        return (from == null || !date.isBefore(from))
                && (to == null || !date.isAfter(to));
    }
}