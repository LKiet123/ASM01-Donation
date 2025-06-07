package org.ltkiet.donation.service.impl;

import org.ltkiet.donation.exception.UserNotFoundException;
import org.ltkiet.donation.model.Role;
import org.ltkiet.donation.model.User;
import org.ltkiet.donation.model.UserDonation;
import org.ltkiet.donation.repository.RoleRepository;
import org.ltkiet.donation.repository.UserDonationRepository;
import org.ltkiet.donation.repository.UserRepository;
import org.ltkiet.donation.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserDonationRepository userDonationRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<UserDonation> getDonationsByUserId(Long userId, Pageable pageable) {
        log.debug("Lay danh sach dong gop cua nguoi dung co ID: {} voi phan trang: {}", userId, pageable);
        if (!userRepository.existsById(userId)) {
            log.warn("Nguoi dung khong ton tai voi ID: {}", userId);
            throw new UserNotFoundException("Khong tim thay nguoi dung voi ID: " + userId);
        }
        return userDonationRepository.findByUserId(userId, pageable);
    }

    @Override
    @Transactional
    public User saveUser(User user) {
        log.info("Dang luu nguoi dung moi voi ten dang nhap: {}", user.getUsername());

        if (user.getRole() == null || user.getRole().getId() == null) {
            throw new IllegalArgumentException("Phai co vai tro cho nguoi dung moi.");
        }
        if (!StringUtils.hasText(user.getPassword())) {
            throw new IllegalArgumentException("Mat khau la bat buoc.");
        }

        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new DataIntegrityViolationException("Ten dang nhap '" + user.getUsername() + "' da ton tai.");
        }
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new DataIntegrityViolationException("Email '" + user.getEmail() + "' da ton tai.");
        }
        if (userRepository.findByPhoneNumber(user.getPhoneNumber()).isPresent()) {
            throw new DataIntegrityViolationException("So dien thoai '" + user.getPhoneNumber() + "' da ton tai.");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        Role userRole = roleRepository.findById(user.getRole().getId())
                .orElseThrow(() -> new IllegalArgumentException("Vai tro khong hop le voi ID: " + user.getRole().getId()));
        user.setRole(userRole);

        if (user.getStatus() == null) {
            user.setStatus(User.UserStatus.ACTIVE);
        }

        User savedUser = userRepository.save(user);
        log.info("Da luu nguoi dung thanh cong voi ID: {} va Ten dang nhap: {}", savedUser.getId(), savedUser.getUsername());
        return savedUser;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> getUserById(Long id) {
        log.debug("Lay nguoi dung theo ID: {}", id);
        return userRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        log.debug("Lay tat ca nguoi dung (khong phan trang)");
        return userRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> getAllUsers(Pageable pageable) {
        log.debug("Lay tat ca nguoi dung voi phan trang: {}", pageable);
        return userRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> searchUsers(String searchTerm, Pageable pageable) {
        String term = StringUtils.hasText(searchTerm) ? searchTerm.trim() : null;
        log.debug("Tim kiem nguoi dung voi tu khoa: '{}', phan trang: {}", term, pageable);
        if (term == null) {
            return userRepository.findAll(pageable);
        } else {
            return userRepository.findByPhoneNumberContainingOrEmailContaining(term, pageable);
        }
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        log.info("Dang xoa nguoi dung voi ID: {}", id);
        if (!userRepository.existsById(id)) {
            log.warn("Xoa that bai: Khong tim thay nguoi dung voi ID: {}", id);
            throw new UserNotFoundException("Khong tim thay nguoi dung voi ID: " + id);
        }

        try {
            userRepository.deleteById(id);
            log.info("Da xoa nguoi dung thanh cong voi ID: {}", id);
        } catch (DataIntegrityViolationException e) {
            log.error("Loi du lieu lien ket khi xoa nguoi dung {}: {}", id, e.getMessage());
            throw new RuntimeException("Khong the xoa nguoi dung. Co the co ban ghi lien quan.", e);
        }
    }

    @Override
    @Transactional
    public User updateUser(Long id, User userDetails) {
        log.info("Dang cap nhat nguoi dung voi ID: {}", id);
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Cap nhat that bai: Khong tim thay nguoi dung voi ID: {}", id);
                    return new UserNotFoundException("Khong tim thay nguoi dung voi ID: " + id);
                });

        existingUser.setFullName(userDetails.getFullName());
        existingUser.setAddress(userDetails.getAddress());
        existingUser.setNote(userDetails.getNote());

        if (!existingUser.getPhoneNumber().equals(userDetails.getPhoneNumber())) {
            if (userRepository.findByPhoneNumber(userDetails.getPhoneNumber()).filter(u -> !u.getId().equals(id)).isPresent()) {
                throw new DataIntegrityViolationException("So dien thoai '" + userDetails.getPhoneNumber() + "' da duoc su dung.");
            }
            existingUser.setPhoneNumber(userDetails.getPhoneNumber());
        }

        if (userDetails.getRole() != null && userDetails.getRole().getId() != null &&
                !existingUser.getRole().getId().equals(userDetails.getRole().getId())) {
            Role newRole = roleRepository.findById(userDetails.getRole().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Vai tro khong hop le voi ID: " + userDetails.getRole().getId()));
            existingUser.setRole(newRole);
            log.info("Da cap nhat vai tro cho nguoi dung {} thanh {}", id, newRole.getName());
        }

        if (userDetails.getStatus() != null && existingUser.getStatus() != userDetails.getStatus()) {
            existingUser.setStatus(userDetails.getStatus());
            log.info("Da cap nhat trang thai nguoi dung {} thanh {}", id, userDetails.getStatus());
        }

        User updatedUser = userRepository.save(existingUser);
        log.info("Da cap nhat nguoi dung thanh cong voi ID: {}", updatedUser.getId());
        return updatedUser;
    }

    @Override
    @Transactional
    public User lockUser(Long id) {
        log.info("Dang khoa nguoi dung voi ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Khoa that bai: Khong tim thay nguoi dung voi ID: {}", id);
                    return new UserNotFoundException("Khong tim thay nguoi dung voi ID: " + id);
                });
        if (user.getStatus() == User.UserStatus.LOCKED) {
            log.warn("Nguoi dung {} da bi khoa san.", id);
            return user;
        }
        user.setStatus(User.UserStatus.LOCKED);
        User savedUser = userRepository.save(user);
        log.info("Da khoa nguoi dung voi ID: {}", id);
        return savedUser;
    }

    @Override
    @Transactional
    public User unlockUser(Long id) {
        log.info("Dang mo khoa nguoi dung voi ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Mo khoa that bai: Khong tim thay nguoi dung voi ID: {}", id);
                    return new UserNotFoundException("Khong tim thay nguoi dung voi ID: " + id);
                });
        if (user.getStatus() == User.UserStatus.ACTIVE) {
            log.warn("Nguoi dung {} da hoat dong san.", id);
            return user;
        }
        user.setStatus(User.UserStatus.ACTIVE);
        User savedUser = userRepository.save(user);
        log.info("Da mo khoa nguoi dung voi ID: {}", id);
        return savedUser;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        log.debug("Tim nguoi dung theo ten dang nhap: {}", username);
        return userRepository.findByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        log.debug("Tim nguoi dung theo email: {}", email);
        return userRepository.findByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByPhoneNumber(String phoneNumber) {
        log.debug("Tim nguoi dung theo so dien thoai: {}", phoneNumber);
        return userRepository.findByPhoneNumber(phoneNumber);
    }
}
