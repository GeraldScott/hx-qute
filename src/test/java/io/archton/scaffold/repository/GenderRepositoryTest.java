package io.archton.scaffold.repository;

import io.archton.scaffold.entity.Gender;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Exemplar repository test: @QuarkusTest boots the application against the
 * configured datasource (Flyway seed data is present), and @TestTransaction
 * rolls back any writes after each test method.
 */
@QuarkusTest
class GenderRepositoryTest {

    @Inject
    GenderRepository genderRepository;

    @Test
    void listAllOrdered_returnsSeedDataSortedByCode() {
        List<Gender> genders = genderRepository.listAllOrdered();

        assertFalse(genders.isEmpty(), "Flyway seed data should be present");
        for (int i = 1; i < genders.size(); i++) {
            assertTrue(genders.get(i - 1).code.compareTo(genders.get(i).code) < 0,
                    "Genders should be ordered by code ascending");
        }
    }

    @Test
    void findByCode_returnsSeededGender() {
        Optional<Gender> female = genderRepository.findByCode("F");

        assertTrue(female.isPresent());
        assertEquals("Female", female.get().description);
    }

    @Test
    void findByCode_returnsEmptyForUnknownCode() {
        assertTrue(genderRepository.findByCode("Z").isEmpty());
    }

    @Test
    @TestTransaction
    void persist_setsIdAndAuditTimestamps() {
        // Pick a code not present in the shared dev database so the unique
        // constraint can't fire; @TestTransaction rolls the insert back.
        String freeCode = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".chars()
                .mapToObj(c -> String.valueOf((char) c))
                .filter(c -> genderRepository.findByCode(c).isEmpty())
                .findFirst().orElseThrow();

        Gender gender = new Gender(freeCode, "Test Gender");
        gender.createdBy = "test";
        gender.updatedBy = "test";

        genderRepository.persist(gender);
        genderRepository.flush();

        assertNotNull(gender.id);
        assertNotNull(gender.createdAt, "@PrePersist should set createdAt");
        assertNotNull(gender.updatedAt, "@PrePersist should set updatedAt");
    }

    @Test
    @TestTransaction
    void existsByCodeAndIdNot_excludesTheGivenRow() {
        Gender female = genderRepository.findByCode("F").orElseThrow();

        assertFalse(genderRepository.existsByCodeAndIdNot("F", female.id),
                "A row must not collide with itself on update");
        assertTrue(genderRepository.existsByCodeAndIdNot("F", -1L),
                "Another row with the same code must be detected");
    }
}
