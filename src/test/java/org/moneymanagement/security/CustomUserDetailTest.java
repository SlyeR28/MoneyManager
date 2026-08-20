package org.moneymanagement.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.moneymanagement.Entity.ProfileEntity;
import org.moneymanagement.Security.Services.CustomUserDetail;

import static org.assertj.core.api.Assertions.assertThat;

class CustomUserDetailTest {

    @Test
    @DisplayName("CustomUserDetail should correctly wrap ProfileEntity attributes")
    void testCustomUserDetail() {
        ProfileEntity entity = ProfileEntity.builder()
                .id(1L)
                .email("test@example.com")
                .password("secretPassword")
                .build();

        CustomUserDetail userDetail = new CustomUserDetail(entity);

        assertThat(userDetail.getUsername()).isEqualTo("test@example.com");
        assertThat(userDetail.getPassword()).isEqualTo("secretPassword");
        assertThat(userDetail.getAuthorities()).isEmpty();
        assertThat(userDetail.isAccountNonExpired()).isTrue();
        assertThat(userDetail.isAccountNonLocked()).isTrue();
        assertThat(userDetail.isCredentialsNonExpired()).isTrue();
        assertThat(userDetail.isEnabled()).isTrue();
    }
}
