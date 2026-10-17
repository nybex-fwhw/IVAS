package com.labtrack.service.validation;

import com.labtrack.model.Analysis;
import com.labtrack.model.Sample;
import com.labtrack.model.TestType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class ValidationService {
    private static <T> boolean isDuplicatedId(
            List<T> list,
            Function<T, String> idExtractor
    ) {
        Set<String> ids = new HashSet<>();

        for (T object : list) {
            if (object == null) {
                return true;
            }

            String id = idExtractor.apply(object);

            if (!ids.add(id)) {
                return true;
            }
        }

        return false;
    }

    public static ValidationError validate(
            List<Sample> samples,
            List<TestType> testTypes,
            List<Analysis> analyses
    ) {
        List<String> errors = new ArrayList<>();

        if (samples == null || testTypes == null || analyses == null) {
            errors.add("Samples or TestTypes or Analyses cannot be null");
        }

        if (isDuplicatedId(samples, Sample::getSampleId)
        || isDuplicatedId(testTypes, TestType::getTestId)
        || isDuplicatedId(analyses, Analysis::getAnalysisId)) {
            errors.add("Samples or TestTypes or Analyses cannot be duplicated");
        }

        // TODO: Preverjanje referenc, datumov in poslovnih pravil

        return new ValidationError(errors);
    }
}
