package com.labtrack.model;

import com.labtrack.model.enums.SampleStatus;
import com.labtrack.model.enums.SampleType;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

@Getter
@RequiredArgsConstructor
public class Sample {
    private final String sampleId;
    private final SampleType sampleType;
    private final LocalDate collectionDate;
    private final LocalDate receivedDate;
    private final SampleStatus sampleStatus;
}
