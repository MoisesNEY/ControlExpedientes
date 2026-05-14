package ni.edu.mney.service.report;

import java.util.Arrays;
import java.util.Objects;

public final class ReportTextUtils {

    private ReportTextUtils() {}

    public static String defaultText(String value) {
        if (value == null) {
            return "N/D";
        }

        String normalized = value.trim();
        if (normalized.isEmpty() || "null".equalsIgnoreCase(normalized) || "undefined".equalsIgnoreCase(normalized)) {
            return "N/D";
        }

        return normalized;
    }

    public static String fullName(String firstName, String lastName, String fallback) {
        String fullName = Arrays.stream(new String[] { firstName, lastName })
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(part -> !part.isEmpty())
                .filter(part -> !"null".equalsIgnoreCase(part))
                .filter(part -> !"undefined".equalsIgnoreCase(part))
                .reduce((left, right) -> left + " " + right)
                .orElse("");

        return fullName.isBlank() ? fallback : fullName;
    }
}
