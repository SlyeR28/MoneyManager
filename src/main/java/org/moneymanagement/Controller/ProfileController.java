package org.moneymanagement.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.moneymanagement.Payload.Request.AuthRequest;
import org.moneymanagement.Payload.Request.ProfileRequest;
import org.moneymanagement.Payload.Response.ProfileResponse;
import org.moneymanagement.Security.Jwt.JwtUtils;
import org.moneymanagement.Service.ProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService  profileService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @PostMapping("/register")
    public ResponseEntity<ProfileResponse> saveProfile(@Valid @RequestBody ProfileRequest profileRequest) {
           ProfileResponse profileResponse = profileService.createProfile(profileRequest);
           return ResponseEntity.status(HttpStatus.CREATED).body(profileResponse);
    }


    @GetMapping("/activation")
    public ResponseEntity<String>activateProfile(@RequestParam String token) {
        boolean isActivated = profileService.activateProfile(token );
        if(isActivated) {
            return ResponseEntity.status(HttpStatus.OK).body("Profile activated successfully");
        }else  {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Activation Token Not Found or Expired ");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody AuthRequest authRequest) {
        try {
            if (!profileService.isAccountActivated(authRequest.getEmail())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "Account not activated. Please check your email."));
            }

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String token = jwtUtils.generateToken(authRequest.getEmail());

            return ResponseEntity.ok(Map.of(
                    "message", "Login successful",
                    "token", token,
                    "email", authRequest.getEmail()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid credentials: " + e.getMessage()));
        }
    }
}
