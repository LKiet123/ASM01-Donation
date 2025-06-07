package org.ltkiet.donation.service;

import jakarta.validation.Valid;
import org.ltkiet.donation.model.User;
import org.ltkiet.donation.model.UserDonation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing Users.
 */
public interface UserService {

    User saveUser(@Valid User user);

    Optional<User> getUserById(Long id);

    List<User> getAllUsers();

    Page<User> getAllUsers(Pageable pageable);

    Page<User> searchUsers(String searchTerm, Pageable pageable);

    void deleteUser(Long id);

    User updateUser(Long id, @Valid User userDetails);

    User lockUser(Long id);

    User unlockUser(Long id);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByPhoneNumber(String phoneNumber);

    Page<UserDonation> getDonationsByUserId(Long userId, Pageable pageable);
}

