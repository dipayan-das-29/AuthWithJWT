package com.boardwith.admin.helper;

import java.util.Random;

/**
 * Centralized generator for {@code businessId} values.
 *
 * <p>Format: {@code <category>_<name>} which produces ids such as
 * {@code AppAdmin_admin}, {@code AppAdmin_superadmin}, {@code AppAdmin_user},
 * {@code Role_ADMIN} or {@code Permission_admin_users:read}.
 *
 * <p>The {@code businessId} column is unique and limited to 50 characters. When the
 * generated id already exists, a unique suffix made of 2 alphanumeric characters and
 * 4 digits is appended (e.g. {@code AppAdmin_admin_AB1234}), truncating the base so
 * the result never exceeds 50 characters.
 */
public final class BusinessIdGenerator {

    private static final Random RANDOM = new Random();
    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int MAX_LENGTH = 50;
    private static final int SUFFIX_LENGTH = 6; // 2 alphanumeric + 4 digits
    private static final int MAX_ATTEMPTS = 1000;

    private BusinessIdGenerator() {
    }

    /**
     * Generate a unique businessId for the given category and name.
     *
     * @param category      entity category prefix (e.g. "AppAdmin", "Role", "Permission")
     * @param name          unique business name (e.g. username, role name, permission name)
     * @param existsChecker optional callback to verify uniqueness against the database.
     *                      Pass {@code null} when the name is already unique (e.g. a unique
     *                      username) so no database lookup is needed.
     * @return a unique businessId not longer than 50 characters
     * @throws IllegalStateException if a unique id cannot be produced within {@value #MAX_ATTEMPTS} attempts
     */
    public static String generate(String category, String name, BusinessIdExistsChecker existsChecker) {
        String base = category + "_" + name;
        if (base.length() > MAX_LENGTH) {
            base = base.substring(0, MAX_LENGTH);
        }

        if (existsChecker == null || !existsChecker.exists(base)) {
            return base;
        }

        // Reserve room for the separator '_' and the 6-char suffix.
        int maxBaseLength = MAX_LENGTH - 1 - SUFFIX_LENGTH;
        String prefix = base.length() > maxBaseLength ? base.substring(0, maxBaseLength) : base;

        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            String candidate = prefix + "_" + randomSuffix();
            if (!existsChecker.exists(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException(
                "Could not generate a unique businessId for base '" + base + "' after " + MAX_ATTEMPTS + " attempts");
    }

    private static String randomSuffix() {
        StringBuilder sb = new StringBuilder(SUFFIX_LENGTH);
        sb.append(randomAlphanumeric());
        sb.append(randomAlphanumeric());
        for (int i = 0; i < 4; i++) {
            sb.append(randomDigit());
        }
        return sb.toString();
    }

    private static char randomAlphanumeric() {
        return ALPHANUMERIC.charAt(RANDOM.nextInt(ALPHANUMERIC.length()));
    }

    private static char randomDigit() {
        return (char) ('0' + RANDOM.nextInt(10));
    }
}