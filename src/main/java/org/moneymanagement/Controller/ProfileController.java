package org.moneymanagement.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.moneymanagement.Payload.Request.ProfileRequest;
import org.moneymanagement.Payload.Response.ProfileResponse;
import org.moneymanagement.Repository.ProfileRepo;
import org.moneymanagement.Service.ProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService  profileService;

    @PostMapping("/register")
    public ResponseEntity<ProfileResponse> saveProfile(@Valid @RequestBody ProfileRequest profileRequest) {
           ProfileResponse profileResponse = profileService.createProfile(profileRequest);
           return ResponseEntity.status(HttpStatus.CREATED).body(profileResponse);
    }


}
