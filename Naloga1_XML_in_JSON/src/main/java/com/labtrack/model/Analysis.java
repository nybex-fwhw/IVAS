package com.labtrack.model;

import com.labtrack.model.enums.AnalysisStatus;
import com.labtrack.model.enums.QcResult;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Getter
//@RequiredArgsConstructor
public class Analysis {
    private final String analysisId;
    private final String sampleId;
    private final String testId;
    private final LocalDate startDate;
    private final LocalDate completionDate;
    private final AnalysisStatus analysisStatus;
    private final QcResult qcResult;
    private final LocalDate reviewDate;


    public Analysis(String analysisId, String sampleId, String testId, LocalDate startDate, LocalDate completionDate, AnalysisStatus analysisStatus, QcResult qcResult, LocalDate reviewDate) {
        this.analysisId = analysisId;
        this.sampleId = sampleId;
        this.testId = testId;
        this.startDate = startDate;
        this.completionDate = completionDate;
        this.analysisStatus = analysisStatus;
        this.qcResult = qcResult;
        this.reviewDate = reviewDate;
    }
}
