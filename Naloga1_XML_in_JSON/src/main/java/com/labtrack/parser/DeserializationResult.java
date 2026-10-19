package com.labtrack.parser;

import com.labtrack.model.*;
import java.util.List;

public sealed interface DeserializationResult {

    record Samples(List<Sample> values)
            implements DeserializationResult {}

    record Tests(List<TestType> values)
            implements DeserializationResult {}

    record Analyses(List<Analysis> values)
            implements DeserializationResult {}
}