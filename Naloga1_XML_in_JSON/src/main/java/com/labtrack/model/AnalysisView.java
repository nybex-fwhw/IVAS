
package com.labtrack.model;

import com.labtrack.model.enums.*;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class AnalysisView {

    private final String analysisId;
    private final String sampleId;
    private final SampleType sampleType;

    private final String testId;
    private final String testName;
    private final TestCategory category;

    private final LocalDate startDate;
    private final LocalDate completionDate;
    private final LocalDate reviewDate;

    private final AnalysisStatus status;
    private final QcResult qcResult;

    public AnalysisView(
            Analysis analysis,
            Sample sample,
            TestType testType
    ) {
        this.analysisId = analysis.getAnalysisId();
        this.sampleId = sample.getSampleId();
        this.sampleType = sample.getSampleType();

        this.testId = testType.getTestId();
        this.testName = testType.getTestName();
        this.category = testType.getTestCategory();

        this.startDate = analysis.getStartDate();
        this.completionDate = analysis.getCompletionDate();
        this.reviewDate = analysis.getReviewDate();

        this.status = analysis.getAnalysisStatus();
        this.qcResult = analysis.getQcResult();
    }
}
