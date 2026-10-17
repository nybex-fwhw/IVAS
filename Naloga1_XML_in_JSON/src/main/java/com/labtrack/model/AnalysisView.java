package com.labtrack.model;

import com.labtrack.model.enums.AnalysisStatus;
import com.labtrack.model.enums.QcResult;
import com.labtrack.model.enums.SampleType;
import com.labtrack.model.enums.TestCategory;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AnalysisView {

    private final String analysisId;
    private final String sampleId;
    private final SampleType sampleType;

    private final String testName;
    private final TestCategory category;

    private final AnalysisStatus status;
    private final QcResult qcResult;
}