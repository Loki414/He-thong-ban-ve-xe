package com.example.busticket.repository;

import com.example.busticket.entity.PasswordResetToken;
import com.example.busticket.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByUserAndCodeAndConsumedFalse(User user, String code);

    @Modifying
    @Transactional
    void deleteByUser(User user);

    @Modifying
    @Transactional
    void deleteByExpiresAtBefore(Instant instant);
}
