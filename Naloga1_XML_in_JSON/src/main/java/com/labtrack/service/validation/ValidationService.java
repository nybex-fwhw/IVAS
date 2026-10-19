
package com.labtrack.service.validation;

import com.labtrack.model.Analysis;
import com.labtrack.model.Sample;
import com.labtrack.model.TestType;
import com.labtrack.model.enums.AnalysisStatus;
import com.labtrack.model.enums.QcResult;
import com.labtrack.model.enums.SampleStatus;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ValidationService {

    public ValidationResult validate(
            List<Sample> samples,
            List<TestType> testTypes,
            List<Analysis> analyses
    ) {
        List<String> errors = new ArrayList<>();

        // Check input lists
        if (samples == null || testTypes == null || analyses == null) {
            errors.add("Input lists cannot be null.");
            return new ValidationResult(errors);
        }

        // 1. Validate IDs
        validateIds(samples, Sample::getSampleId, "Sample", errors);
        validateIds(testTypes, TestType::getTestId, "TestType", errors);
        validateIds(analyses, Analysis::getAnalysisId, "Analysis", errors);

        // 2. Validate references
        validateReferences(samples, testTypes, analyses, errors);

        // 3. Validate sample and test compatibility
        validateSampleCompatibility(samples, testTypes, analyses, errors);

        // 4. Validate dates
        validateDates(samples, analyses, errors);

        // 5. Validate statuses and QC results
        validateStatuses(samples, analyses, errors);

        return new ValidationResult(errors);
    }


    // **************** ID VALIDATION ****************

    private static <T> void validateIds(
            List<T> objects,
            Function<T, String> idExtractor,
            String entityType,
            List<String> errors
    ) {
        Set<String> seenIds = new HashSet<>();

        for (int i = 0; i < objects.size(); i++) {
            T object = objects.get(i);

            if (object == null) {
                errors.add(entityType + "[" + i + "]: Object cannot be null.");
                continue;
            }

            String id = idExtractor.apply(object);

            if (id == null || id.isBlank()) {
                errors.add(entityType + "[" + i + "]: ID cannot be empty.");
                continue;
            }

            if (!seenIds.add(id)) {
                errors.add(entityType + ": Duplicate ID: " + id);
            }
        }
    }


    // **************** REFERENCE VALIDATION ****************

    private static void validateReferences(
            List<Sample> samples,
            List<TestType> testTypes,
            List<Analysis> analyses,
            List<String> errors
    ) {
        Set<String> sampleIds = samples.stream()
                .filter(Objects::nonNull)
                .map(Sample::getSampleId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<String> testIds = testTypes.stream()
                .filter(Objects::nonNull)
                .map(TestType::getTestId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        for (Analysis analysis : analyses) {
            if (analysis == null) {
                continue;
            }

            String analysisId = analysis.getAnalysisId();

            if (analysis.getSampleId() == null
                    || !sampleIds.contains(analysis.getSampleId())) {

                errors.add(
                        "Analysis " + analysisId +
                                ": Referenced sample does not exist: " +
                                analysis.getSampleId()
                );
            }

            if (analysis.getTestId() == null
                    || !testIds.contains(analysis.getTestId())) {

                errors.add(
                        "Analysis " + analysisId +
                                ": Referenced test does not exist: " +
                                analysis.getTestId()
                );
            }
        }
    }


    // **************** SAMPLE COMPATIBILITY ****************

    private static void validateSampleCompatibility(
            List<Sample> samples,
            List<TestType> testTypes,
            List<Analysis> analyses,
            List<String> errors
    ) {
        Map<String, Sample> samplesById = samples.stream()
                .filter(Objects::nonNull)
                .filter(s -> s.getSampleId() != null)
                .collect(Collectors.toMap(
                        Sample::getSampleId,
                        Function.identity(),
                        (first, duplicate) -> first
                ));

        Map<String, TestType> testsById = testTypes.stream()
                .filter(Objects::nonNull)
                .filter(t -> t.getTestId() != null)
                .collect(Collectors.toMap(
                        TestType::getTestId,
                        Function.identity(),
                        (first, duplicate) -> first
                ));

        for (Analysis analysis : analyses) {
            if (analysis == null) {
                continue;
            }

            Sample sample = samplesById.get(analysis.getSampleId());
            TestType test = testsById.get(analysis.getTestId());

            // Missing references are handled separately.
            if (sample == null || test == null) {
                continue;
            }

            if (sample.getSampleType() == null
                    || test.getRequiredSampleType() == null) {
                errors.add(
                        "Analysis " + analysis.getAnalysisId() +
                                ": Sample type or required sample type is missing."
                );
                continue;
            }

            if (sample.getSampleType() != test.getRequiredSampleType()) {
                errors.add(
                        "Analysis " + analysis.getAnalysisId() +
                                ": Incompatible sample type. Expected " +
                                test.getRequiredSampleType() +
                                ", but received " + sample.getSampleType()
                );
            }
        }
    }


    // **************** DATE VALIDATION ****************

    private static void validateDates(
            List<Sample> samples,
            List<Analysis> analyses,
            List<String> errors
    ) {
        Map<String, Sample> samplesById = samples.stream()
                .filter(Objects::nonNull)
                .filter(s -> s.getSampleId() != null)
                .collect(Collectors.toMap(
                        Sample::getSampleId,
                        Function.identity(),
                        (first, duplicate) -> first
                ));

        for (Sample sample : samples) {
            if (sample == null) {
                continue;
            }

            LocalDate collectionDate = sample.getCollectionDate();
            LocalDate receivedDate = sample.getReceivedDate();

            if (collectionDate == null) {
                errors.add(
                        "Sample " + sample.getSampleId() +
                                ": Collection date is missing."
                );
            }

            if (collectionDate != null && receivedDate != null
                    && receivedDate.isBefore(collectionDate)) {
                errors.add(
                        "Sample " + sample.getSampleId() +
                                ": Received date cannot be before collection date."
                );
            }

            if (sample.getSampleStatus() == SampleStatus.REGISTERED
                    && receivedDate != null) {
                errors.add(
                        "Sample " + sample.getSampleId() +
                                ": REGISTERED sample should not have a received date."
                );
            }

            if (sample.getSampleStatus() != null
                    && sample.getSampleStatus() != SampleStatus.REGISTERED
                    && receivedDate == null) {
                errors.add(
                        "Sample " + sample.getSampleId() +
                                ": Received date is required for status " +
                                sample.getSampleStatus()
                );
            }
        }

        for (Analysis analysis : analyses) {
            if (analysis == null) {
                continue;
            }

            String id = analysis.getAnalysisId();

            LocalDate startDate = analysis.getStartDate();
            LocalDate completionDate = analysis.getCompletionDate();
            LocalDate reviewDate = analysis.getReviewDate();

            if (startDate != null && completionDate != null
                    && completionDate.isBefore(startDate)) {
                errors.add(
                        "Analysis " + id +
                                ": Completion date cannot be before start date."
                );
            }

            if (completionDate != null && reviewDate != null
                    && reviewDate.isBefore(completionDate)) {
                errors.add(
                        "Analysis " + id +
                                ": Review date cannot be before completion date."
                );
            }

            Sample sample = samplesById.get(analysis.getSampleId());

            if (sample != null && startDate != null
                    && sample.getReceivedDate() != null
                    && startDate.isBefore(sample.getReceivedDate())) {
                errors.add(
                        "Analysis " + id +
                                ": Start date cannot be before sample received date."
                );
            }

            if (analysis.getAnalysisStatus() == AnalysisStatus.COMPLETED) {
                if (startDate == null || completionDate == null) {
                    errors.add(
                            "Analysis " + id +
                                    ": COMPLETED analysis requires start and completion dates."
                    );
                }
            }

            if (analysis.getAnalysisStatus() == AnalysisStatus.PENDING
                    && completionDate != null) {
                errors.add(
                        "Analysis " + id +
                                ": PENDING analysis cannot have a completion date."
                );
            }
        }
    }


    // **************** STATUS AND QC VALIDATION ****************

    private static void validateStatuses(
            List<Sample> samples,
            List<Analysis> analyses,
            List<String> errors
    ) {
        Map<String, Sample> samplesById = samples.stream()
                .filter(Objects::nonNull)
                .filter(s -> s.getSampleId() != null)
                .collect(Collectors.toMap(
                        Sample::getSampleId,
                        Function.identity(),
                        (first, duplicate) -> first
                ));

        for (Sample sample : samples) {
            if (sample != null && sample.getSampleStatus() == null) {
                errors.add(
                        "Sample " + sample.getSampleId() +
                                ": Status is missing."
                );
            }
        }

        for (Analysis analysis : analyses) {
            if (analysis == null) {
                continue;
            }

            String id = analysis.getAnalysisId();
            AnalysisStatus status = analysis.getAnalysisStatus();
            QcResult qc = analysis.getQcResult();

            if (status == null || qc == null) {
                errors.add(
                        "Analysis " + id +
                                ": Status or QC result is missing."
                );
                continue;
            }

            switch (status) {

                case PENDING -> {
                    if (qc != QcResult.NOT_TESTED) {
                        errors.add(
                                "Analysis " + id +
                                        ": PENDING requires QC NOT_TESTED."
                        );
                    }
                }

                case IN_PROGRESS -> {
                    if (qc != QcResult.NOT_TESTED) {
                        errors.add(
                                "Analysis " + id +
                                        ": IN_PROGRESS requires QC NOT_TESTED."
                        );
                    }
                }

                case QUALITY_CHECK -> {
                    if (qc != QcResult.PENDING
                            && qc != QcResult.INCONCLUSIVE) {
                        errors.add(
                                "Analysis " + id +
                                        ": QUALITY_CHECK requires QC PENDING or INCONCLUSIVE."
                        );
                    }
                }

                case COMPLETED -> {
                    if (qc != QcResult.PASS) {
                        errors.add(
                                "Analysis " + id +
                                        ": COMPLETED requires QC PASS."
                        );
                    }
                }

                case FAILED -> {
                    if (qc != QcResult.FAIL
                            && qc != QcResult.NOT_TESTED
                            && qc != QcResult.INCONCLUSIVE) {
                        errors.add(
                                "Analysis " + id +
                                        ": FAILED has an invalid QC result."
                        );
                    }
                }

                case CANCELLED -> {
                    // QC result is preserved as historical information.
                }
            }

            Sample sample = samplesById.get(analysis.getSampleId());

            if (sample != null
                    && sample.getSampleStatus() == SampleStatus.REJECTED
                    && status != AnalysisStatus.CANCELLED) {
                errors.add(
                        "Analysis " + id +
                                ": Rejected sample must have a CANCELLED analysis."
                );
            }

            if (sample != null
                    && sample.getSampleStatus() == SampleStatus.REGISTERED
                    && status != AnalysisStatus.PENDING
                    && status != AnalysisStatus.CANCELLED) {
                errors.add(
                        "Analysis " + id +
                                ": Unreceived sample cannot have an active or completed analysis."
                );
            }
        }
    }
}
