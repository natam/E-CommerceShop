package com.nkh.ECommerceShop.repository;

import com.nkh.ECommerceShop.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface _UsersRepository extends JpaRepository<Users,Long> {
    Boolean existsByEmail(String email);
    Optional<Users> findByEmail(String email);
}
