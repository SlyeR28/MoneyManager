package org.moneymanagement.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.moneymanagement.Entity.ProfileEntity;
import org.moneymanagement.Mappers.ProfileMapper;
import org.moneymanagement.Payload.Request.ProfileRequest;
import org.moneymanagement.Payload.Response.ProfileResponse;
import org.moneymanagement.Repository.ProfileRepository;
import org.moneymanagement.Service.impl.EmailService;
import org.moneymanagement.Service.impl.ProfileServiceImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceImplTest {

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private ProfileMapper profileMapper;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ProfileServiceImpl profileService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(profileService, "activationUrl", "http://localhost:8080");
    }

    @Test
    @DisplayName("createProfile should encode password, generate token, send activation email and return response")
    void testCreateProfile_Success() {
        ProfileRequest request = new ProfileRequest();
        request.setFullName("John Doe");
        request.setEmail("john@example.com");
        request.setPassword("PlainPassword123");

        ProfileEntity entity = new ProfileEntity();
        entity.setFullName("John Doe");
        entity.setEmail("john@example.com");
        entity.setPassword("PlainPassword123");

        ProfileEntity savedEntity = new ProfileEntity();
        savedEntity.setId(10L);
        savedEntity.setFullName("John Doe");
        savedEntity.setEmail("john@example.com");
        savedEntity.setPassword("encoded_password");
        savedEntity.setActivationToken("uuid-token-123");

        ProfileResponse response = ProfileResponse.builder()
                .id(10L)
                .fullName("John Doe")
                .email("john@example.com")
                .build();

        when(profileMapper.toEntity(request)).thenReturn(entity);
        when(passwordEncoder.encode("PlainPassword123")).thenReturn("encoded_password");
        when(profileRepository.save(any(ProfileEntity.class))).thenReturn(savedEntity);
        when(profileMapper.toResponse(savedEntity)).thenReturn(response);
        doNothing().when(emailService).sendEmail(anyString(), anyString(), anyString());

        ProfileResponse result = profileService.createProfile(request);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("john@example.com");
        verify(emailService).sendEmail(eq("john@example.com"), anyString(), contains("uuid-token-123"));
        verify(profileRepository).save(any(ProfileEntity.class));
    }

    @Test
    @DisplayName("activateProfile should set isActive to true and return true when token is found")
    void testActivateProfile_Success() {
        ProfileEntity entity = new ProfileEntity();
        entity.setId(1L);
        entity.setIsActive(false);

        when(profileRepository.findByActivationToken("valid-token")).thenReturn(Optional.of(entity));
        when(profileRepository.save(entity)).thenReturn(entity);

        boolean activated = profileService.activateProfile("valid-token");

        assertThat(activated).isTrue();
        assertThat(entity.getIsActive()).isTrue();
        verify(profileRepository).save(entity);
    }

    @Test
    @DisplayName("activateProfile should return false when token is not found")
    void testActivateProfile_NotFound() {
        when(profileRepository.findByActivationToken("invalid-token")).thenReturn(Optional.empty());

        boolean activated = profileService.activateProfile("invalid-token");

        assertThat(activated).isFalse();
        verify(profileRepository, never()).save(any());
    }

    @Test
    @DisplayName("isAccountActivated should return true when account isActive is true")
    void testIsAccountActivated_True() {
        ProfileEntity entity = new ProfileEntity();
        entity.setIsActive(true);

        when(profileRepository.findByEmail("active@example.com")).thenReturn(Optional.of(entity));

        boolean isActive = profileService.isAccountActivated("active@example.com");

        assertThat(isActive).isTrue();
    }

    @Test
    @DisplayName("isAccountActivated should return false when user not found")
    void testIsAccountActivated_NotFound() {
        when(profileRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        boolean isActive = profileService.isAccountActivated("missing@example.com");

        assertThat(isActive).isFalse();
    }

    @Test
    @DisplayName("getCurrentProfile should return profile from security context authentication")
    void testGetCurrentProfile_Success() {
        Authentication auth = mock(Authentication.class);
        SecurityContext secCtx = mock(SecurityContext.class);
        when(auth.getName()).thenReturn("user@example.com");
        when(secCtx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(secCtx);

        ProfileEntity entity = new ProfileEntity();
        entity.setId(5L);
        entity.setEmail("user@example.com");

        when(profileRepository.findByEmail("user@example.com")).thenReturn(Optional.of(entity));

        ProfileEntity current = profileService.getCurrentProfile();

        assertThat(current).isNotNull();
        assertThat(current.getEmail()).isEqualTo("user@example.com");
    }

    @Test
    @DisplayName("getCurrentProfile should throw UsernameNotFoundException when user not in DB")
    void testGetCurrentProfile_ThrowsException() {
        Authentication auth = mock(Authentication.class);
        SecurityContext secCtx = mock(SecurityContext.class);
        when(auth.getName()).thenReturn("user@example.com");
        when(secCtx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(secCtx);

        when(profileRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.getCurrentProfile())
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    @DisplayName("getPublicProfile should return profile response for given email")
    void testGetPublicProfile() {
        ProfileEntity entity = new ProfileEntity();
        entity.setEmail("user@example.com");

        ProfileResponse response = ProfileResponse.builder().email("user@example.com").build();

        when(profileRepository.findByEmail("user@example.com")).thenReturn(Optional.of(entity));
        when(profileMapper.toResponse(entity)).thenReturn(response);

        ProfileResponse result = profileService.getPublicProfile("user@example.com");

        assertThat(result.getEmail()).isEqualTo("user@example.com");
    }
}
