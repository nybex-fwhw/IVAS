package com.labtrack.model;

import com.labtrack.model.enums.AnalysisStatus;
import com.labtrack.model.enums.QcResult;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

@Getter
@RequiredArgsConstructor
public class Analysis {
    private final String analysisId;
    private final String sampleId;
    private final String testId;
    private final LocalDate startDate;
    private final LocalDate completionDate;
    private final AnalysisStatus analysisStatus;
    private final QcResult qcResult;
    private final LocalDate reviewDate;
}
