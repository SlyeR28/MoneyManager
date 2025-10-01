package org.moneymanagement.Repository;

import org.moneymanagement.Entity.ProfileEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfileRepo extends JpaRepository<ProfileEntity,Long> {
   Optional<Profile> findByEmail(String email);
}
