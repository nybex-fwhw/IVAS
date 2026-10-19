package com.labtrack.model;

import com.labtrack.model.enums.AnalysisStatus;
import com.labtrack.model.enums.QcResult;
import com.labtrack.model.enums.SampleType;
import com.labtrack.model.enums.TestCategory;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Getter
//@RequiredArgsConstructor
public class AnalysisView {

    private final String analysisId;
    private final String sampleId;
    private final SampleType sampleType;

    private final String testName;
    private final TestCategory category;

    private final AnalysisStatus status;
    private final QcResult qcResult;

    public AnalysisView(String analysisId, String sampleId, SampleType sampleType, String testName, TestCategory category, AnalysisStatus status, QcResult qcResult) {
        this.analysisId = analysisId;
        this.sampleId = sampleId;
        this.sampleType = sampleType;
        this.testName = testName;
        this.category = category;
        this.status = status;
        this.qcResult = qcResult;
    }
}