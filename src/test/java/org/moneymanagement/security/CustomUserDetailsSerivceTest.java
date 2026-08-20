package org.moneymanagement.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.moneymanagement.Entity.ProfileEntity;
import org.moneymanagement.Repository.ProfileRepository;
import org.moneymanagement.Security.Services.CustomUserDetailsSerivce;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsSerivceTest {

    @Mock
    private ProfileRepository profileRepository;

    @InjectMocks
    private CustomUserDetailsSerivce userDetailsService;

    @Test
    @DisplayName("loadUserByUsername should return UserDetails when user exists")
    void testLoadUserByUsername_Success() {
        ProfileEntity entity = ProfileEntity.builder()
                .email("user@example.com")
                .password("password123")
                .build();

        when(profileRepository.findByEmail("user@example.com")).thenReturn(Optional.of(entity));

        UserDetails userDetails = userDetailsService.loadUserByUsername("user@example.com");

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("user@example.com");
        assertThat(userDetails.getPassword()).isEqualTo("password123");
    }

    @Test
    @DisplayName("loadUserByUsername should throw UsernameNotFoundException when user does not exist")
    void testLoadUserByUsername_NotFound() {
        when(profileRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("unknown@example.com"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
