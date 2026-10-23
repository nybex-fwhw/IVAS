package um.feri.ipt.tids.project.parser.ares;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public record AresResult(String schemaVersion, Metadata metadata, Query query, Status status,
                         Company company, List<String> missingFields, List<Problem> errors) {
    public enum Status { SUCCESS, PARTIAL, NOT_FOUND, ERROR }
    public record Metadata(String source, String method, String sourceUrl, String extractedAt) { }
    public record Query(String ico) { }
    public record Company(String ico, String name, String vatId, String registeredAddress, String legalForm) { }
    public record Problem(String code, String message) { }

    public AresResult {
        missingFields = List.copyOf(missingFields);
        errors = List.copyOf(errors);
    }

    static AresResult create(String ico, String url, Instant time, Status status, Company company,
                             List<Problem> errors) {
        var missing = new ArrayList<String>();
        if (company != null) {
            if (company.ico() == null) missing.add("ico");
            if (company.name() == null) missing.add("name");
            if (company.vatId() == null) missing.add("vatId");
            if (company.registeredAddress() == null) missing.add("registeredAddress");
            if (company.legalForm() == null) missing.add("legalForm");
        }
        return new AresResult("1.0", new Metadata("ARES_WEB", "HTML_DOM", url, time.toString()),
                new Query(ico), status, company, missing, errors);
    }
}
