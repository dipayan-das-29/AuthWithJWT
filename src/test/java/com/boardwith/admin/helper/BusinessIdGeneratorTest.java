package com.boardwith.admin.helper;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BusinessIdGeneratorTest {

    @Test
    void shouldGenerateCategoryNameFormat() {
        assertThat(BusinessIdGenerator.generate("AppAdmin", "admin", null)).isEqualTo("AppAdmin_admin");
        assertThat(BusinessIdGenerator.generate("AppAdmin", "superadmin", null)).isEqualTo("AppAdmin_superadmin");
        assertThat(BusinessIdGenerator.generate("AppAdmin", "user", null)).isEqualTo("AppAdmin_user");
        assertThat(BusinessIdGenerator.generate("Permission", "admin_users:read", null))
                .isEqualTo("Permission_admin_users:read");
    }

    @Test
    void shouldReturnBaseWhenNotTaken() {
        Set<String> taken = new HashSet<>();
        taken.add("Role_ADMIN");

        assertThat(BusinessIdGenerator.generate("Role", "ADMIN", taken::contains)).isEqualTo("Role_ADMIN");
    }

    @Test
    void shouldAppendUniqueSuffixWhenTaken() {
        Set<String> taken = new HashSet<>();
        taken.add("AppAdmin_admin");

        String result = BusinessIdGenerator.generate("AppAdmin", "admin", taken::contains);

        assertThat(result).startsWith("AppAdmin_admin_");
        assertThat(result).hasSize(14 + 1 + 6); // "AppAdmin_admin" + "_" + 6-char suffix
        assertThat(taken).doesNotContain(result);
        assertThat(result.substring(15)).matches("[A-Z0-9]{2}[0-9]{4}");
    }

    @Test
    void shouldNotExceedMaxLengthWhenSuffixAppended() {
        String longName = "a".repeat(45);
        String base = ("AppAdmin_" + longName).substring(0, 50); // truncated base
        Set<String> taken = new HashSet<>();
        taken.add(base); // collision on the truncated base

        String result = BusinessIdGenerator.generate("AppAdmin", longName, taken::contains);

        assertThat(result).hasSizeLessThanOrEqualTo(50);
        assertThat(result).startsWith("AppAdmin_");
        assertThat(result.substring(result.length() - 7)).matches("_[A-Z0-9]{2}[0-9]{4}");
    }

    @Test
    void shouldTruncateLongBase() {
        String result = BusinessIdGenerator.generate("Role", "a".repeat(60), null);

        assertThat(result).hasSize(50);
        assertThat(result).startsWith("Role_");
    }

    @Test
    void shouldThrowWhenUnableToGenerateUniqueId() {
        Set<String> taken = new HashSet<>();
        taken.add("Role_SUPER_ADMIN");

        // Every candidate collides because the checker always returns true.
        assertThatThrownBy(() -> BusinessIdGenerator.generate("Role", "SUPER_ADMIN", s -> true))
                .isInstanceOf(IllegalStateException.class);
    }
}