package org.moneymanagement.Security.Services;

import lombok.RequiredArgsConstructor;
import org.moneymanagement.Entity.ProfileEntity;
import org.moneymanagement.Repository.ProfileRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsSerivce implements UserDetailsService {

    private final ProfileRepository profileRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        ProfileEntity profileEntity = profileRepository
                .findByEmail(username).orElseThrow(() -> new UsernameNotFoundException(username));
        if(profileEntity==null){
            throw new UsernameNotFoundException(username);
        }
        return new CustomUserDetail(profileEntity);
    }
}
