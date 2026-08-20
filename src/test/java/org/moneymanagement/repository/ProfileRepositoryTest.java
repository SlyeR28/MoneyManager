package org.moneymanagement.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.moneymanagement.Entity.ProfileEntity;
import org.moneymanagement.Repository.ProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ProfileRepositoryTest {

    @Autowired
    private ProfileRepository profileRepository;

    @Test
    @DisplayName("Should find profile by existing email")
    void testFindByEmail_Success() {
        Optional<ProfileEntity> found = profileRepository.findByEmail("testuser@example.com");

        assertThat(found).isPresent();
        assertThat(found.get().getFullName()).isEqualTo("Test User");
        assertThat(found.get().getIsActive()).isTrue();
    }

    @Test
    @DisplayName("Should return empty when email does not exist")
    void testFindByEmail_NotFound() {
        Optional<ProfileEntity> found = profileRepository.findByEmail("nonexistent@example.com");

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should find profile by activation token")
    void testFindByActivationToken_Success() {
        Optional<ProfileEntity> found = profileRepository.findByActivationToken("sample-activation-token-12345");

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("inactive@example.com");
        assertThat(found.get().getIsActive()).isFalse();
    }

    @Test
    @DisplayName("Should return empty when activation token is invalid")
    void testFindByActivationToken_NotFound() {
        Optional<ProfileEntity> found = profileRepository.findByActivationToken("invalid-token");

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should save and persist new profile")
    void testSaveProfile() {
        ProfileEntity profile = ProfileEntity.builder()
                .fullName("Jane Doe")
                .email("jane@example.com")
                .password("encoded_pass")
                .isActive(true)
                .build();

        ProfileEntity saved = profileRepository.save(profile);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getEmail()).isEqualTo("jane@example.com");
    }
}
