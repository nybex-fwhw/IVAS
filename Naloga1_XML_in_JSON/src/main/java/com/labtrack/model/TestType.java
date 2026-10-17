package com.labtrack.model;

import com.labtrack.model.enums.TestCategory;
import com.labtrack.model.enums.SampleType;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class TestType {
    private final String testId;
    private final String testName;
    private final TestCategory testCategory;
    private final String testDescription;
    private final SampleType requiredSampleType;
}
