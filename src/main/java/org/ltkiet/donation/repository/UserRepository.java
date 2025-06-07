package org.ltkiet.donation.repository;

import org.ltkiet.donation.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByPhoneNumber(String phoneNumber);

    Optional<User> findByUsername(String username);

    @Query("SELECT u FROM User u WHERE u.phoneNumber LIKE %:searchTerm% OR u.email LIKE %:searchTerm%")
    Page<User> findByPhoneNumberContainingOrEmailContaining(@Param("searchTerm") String searchTerm, Pageable pageable);

    Page<User> findAll(Pageable pageable);
}