package org.moneymanagement.Service.impl;

import lombok.RequiredArgsConstructor;
import org.moneymanagement.Entity.ProfileEntity;
import org.moneymanagement.Mappers.ProfileMapper;
import org.moneymanagement.Payload.Request.ProfileRequest;
import org.moneymanagement.Payload.Response.ProfileResponse;
import org.moneymanagement.Repository.ProfileRepo;
import org.moneymanagement.Service.ProfileService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final ProfileRepo  profileRepo;
    private final ProfileMapper profileMapper;
    private final EmailService emailService;


    @Override
    public ProfileResponse createProfile(ProfileRequest profileRequest) {
       ProfileEntity profileEntity = profileMapper.toEntity(profileRequest);
       profileEntity.setActivationCode(UUID.randomUUID().toString());
        ProfileEntity savedEntity = profileRepo.save(profileEntity);
        //send activation email
        String activationLink = "http://localhost:8080/api/activation?token=" + savedEntity.getActivationCode();
        String subject = "Activate your Money Manager Account ";
        String body = "Click on the following link to activate your account : " + activationLink;
        emailService.sendEmail(profileEntity.getEmail(), subject, body);

        return profileMapper.toResponse(savedEntity);
    }

    @Override
    public ProfileResponse updateProfile(Long profileId, ProfileRequest profileRequest) {
        return null;
    }
}
