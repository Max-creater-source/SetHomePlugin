package de.maxhirtzbruch.sethomeplugin.utils;

/**
 * Validates home names.
 */
public final class HomeValidator {

    private HomeValidator() {}

    private static final int MAX_LENGTH = 24;

    /**
     * A valid home name:
     *  - 1 to MAX_LENGTH characters long
     *  - Letters (unicode), digits, hyphens, underscores only
     */
    public static ValidationResult validate(String name) {
        if (name == null || name.isEmpty()) {
            return ValidationResult.INVALID_CHARS;
        }
        if (name.length() > MAX_LENGTH) {
            return ValidationResult.TOO_LONG;
        }
        if (!name.matches("[\\w\\-]+")) {
            return ValidationResult.INVALID_CHARS;
        }
        return ValidationResult.OK;
    }

    public enum ValidationResult {
        OK,
        TOO_LONG,
        INVALID_CHARS
    }
}
