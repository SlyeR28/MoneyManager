package org.moneymanagement.Service;

import org.moneymanagement.Payload.Request.ProfileRequest;
import org.moneymanagement.Payload.Response.ProfileResponse;

public interface ProfileService {

    ProfileResponse createProfile(ProfileRequest profileRequest);
    ProfileResponse updateProfile(Long  profileId , ProfileRequest profileRequest);
}
