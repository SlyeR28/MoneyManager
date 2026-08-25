package org.moneymanagement.service;

import org.moneymanagement.entity.ProfileEntity;
import org.moneymanagement.payload.request.ProfileRequest;
import org.moneymanagement.payload.response.ProfileResponse;

public interface ProfileService {

    ProfileResponse createProfile(ProfileRequest profileRequest);
    ProfileResponse updateProfile(Long  profileId , ProfileRequest profileRequest);
    boolean activateProfile(String activationToken);
    boolean isAccountActivated(String email);
    ProfileEntity getCurrentProfile();
    ProfileResponse getPublicProfile(String email);
}
