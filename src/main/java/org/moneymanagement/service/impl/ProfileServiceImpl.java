package org.moneymanagement.service.impl;

import lombok.RequiredArgsConstructor;
import org.moneymanagement.entity.ProfileEntity;
import org.moneymanagement.mappers.ProfileMapper;
import org.moneymanagement.payload.request.ProfileRequest;
import org.moneymanagement.payload.response.ProfileResponse;
import org.moneymanagement.repository.ProfileRepository;
import org.moneymanagement.service.ProfileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final ProfileRepository profileRepository;
    private final ProfileMapper profileMapper;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.activation.url}")
     private String activationUrl;

    @Override
    public ProfileResponse createProfile(ProfileRequest profileRequest) {
       ProfileEntity profileEntity = profileMapper.toEntity(profileRequest);
       profileEntity.setActivationToken(UUID.randomUUID().toString());
       profileEntity.setPassword(passwordEncoder.encode(profileEntity.getPassword()));
        ProfileEntity savedEntity = profileRepository.save(profileEntity);
        //send activation email
        String activationLink = activationUrl + "/api/v1/activation?token=" + savedEntity.getActivationToken();
        String subject = "Activate your Money Manager Account ";
        String body = "Click on the following link to activate your account : " + activationLink;
        emailService.sendEmail(profileEntity.getEmail(), subject, body);

        return profileMapper.toResponse(savedEntity);
    }

    @Override
    public ProfileResponse updateProfile(Long profileId, ProfileRequest profileRequest) {
        return null;
    }

    @Override
    public boolean activateProfile(String activationToken) {
        return profileRepository.findByActivationToken(activationToken)
                .map(this::activateAndSaveProfile)
                .orElse(false);
    }

    private boolean activateAndSaveProfile(ProfileEntity profile) {
        profile.setIsActive(true);
        profileRepository.save(profile);
        return true;
    }

    @Override
    public boolean isAccountActivated(String email) {
        return profileRepository.findByEmail(email)
                .map(ProfileEntity::getIsActive)
                .orElse(false);
    }

    @Override
    public ProfileEntity getCurrentProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return profileRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Profile Not Found: " + authentication.getName()));
    }

    @Override
    public ProfileResponse getPublicProfile(String email) {
        ProfileEntity profile = (email == null)
                ? getCurrentProfile()
                : profileRepository.findByEmail(email)
                        .orElseThrow(() -> new UsernameNotFoundException("Profile Not Found: " + email));

        return profileMapper.toResponse(profile);
    }


}

