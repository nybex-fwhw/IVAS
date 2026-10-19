package com.labtrack.model;

import com.labtrack.model.enums.TestCategory;
import com.labtrack.model.enums.SampleType;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Getter
//@RequiredArgsConstructor
public class TestType {
    private final String testId;
    private final String testName;
    private final TestCategory testCategory;
    private final String testDescription;
    private final SampleType requiredSampleType;

    public TestType(String testId, String testName, TestCategory testCategory, String testDescription, SampleType requiredSampleType) {
        this.testId = testId;
        this.testName = testName;
        this.testCategory = testCategory;
        this.testDescription = testDescription;
        this.requiredSampleType = requiredSampleType;
    }
}
