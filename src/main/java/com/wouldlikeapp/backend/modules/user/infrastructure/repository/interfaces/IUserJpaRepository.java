package com.wouldlikeapp.backend.modules.user.infrastructure.repository.interfaces;

import com.wouldlikeapp.backend.modules.user.infrastructure.entity.UserJpaEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IUserJpaRepository extends JpaRepository<UserJpaEntity, Long> {
  Optional<UserJpaEntity> findByEmail(String email);
  boolean existsByEmail(String email);
}
