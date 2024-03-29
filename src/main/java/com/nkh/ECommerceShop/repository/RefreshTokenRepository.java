package com.nkh.ECommerceShop.repository;

import com.nkh.ECommerceShop.model.RefreshToken;
import com.nkh.ECommerceShop.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    @Modifying
    int deleteByUser(Users user);

    @Modifying
    int deleteAllByUser(Users user);
}
