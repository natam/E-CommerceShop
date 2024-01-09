package com.nkh.ECommerceShop.repository;

import com.nkh.ECommerceShop.model._User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface _UsersRepository extends JpaRepository<_User,Long> {
    Boolean existsByEmail(String email);
    Optional<_User> findByEmail(String email);
}
