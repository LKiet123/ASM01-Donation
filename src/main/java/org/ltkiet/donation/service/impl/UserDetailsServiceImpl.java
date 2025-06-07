package org.ltkiet.donation.service.impl;

import org.ltkiet.donation.model.User;
import org.ltkiet.donation.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Khong tim thay nguoi dung voi ten dang nhap: " + username));

        if (user.getStatus() == User.UserStatus.LOCKED) {
            throw new UsernameNotFoundException("Tai khoan nguoi dung bi khoa: " + username);
        }

        Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
        if (user.getRole() != null) {
            grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().getName()));
        }

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                grantedAuthorities
        );
    }
}
