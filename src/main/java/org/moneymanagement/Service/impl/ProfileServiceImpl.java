package org.moneymanagement.Service.impl;

import lombok.RequiredArgsConstructor;
import org.moneymanagement.Entity.ProfileEntity;
import org.moneymanagement.Mappers.ProfileMapper;
import org.moneymanagement.Payload.Request.ProfileRequest;
import org.moneymanagement.Payload.Response.ProfileResponse;
import org.moneymanagement.Repository.ProfileRepository;
import org.moneymanagement.Service.ProfileService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
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


    @Override
    public ProfileResponse createProfile(ProfileRequest profileRequest) {
       ProfileEntity profileEntity = profileMapper.toEntity(profileRequest);
       profileEntity.setActivationToken(UUID.randomUUID().toString());
       profileEntity.setPassword(passwordEncoder.encode(profileEntity.getPassword()));
        ProfileEntity savedEntity = profileRepository.save(profileEntity);
        //send activation email
        String activationLink = "http://localhost:8080/api/v1/activation?token=" + savedEntity.getActivationToken();
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
        return profileRepository.findByActivationToken(activationToken).map(
                profile -> {
                    profile.setIsActive(true);
                    profileRepository.save(profile);
                    return true;
                }
        ).orElse(false);
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
       return  profileRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Profile Not Found" + authentication.getName()));

    }

    @Override
    public ProfileResponse getPublicProfile(String email) {

        ProfileEntity currentUser = null;

        if(email==null){
          currentUser =  getCurrentProfile();
        }{
            currentUser=  profileRepository.findByEmail(email).
                    orElseThrow(() -> new UsernameNotFoundException("Profile Not Found" + email));
        }

        return profileMapper.toResponse(currentUser);

    }


}

