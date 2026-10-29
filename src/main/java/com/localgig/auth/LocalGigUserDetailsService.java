package com.localgig.auth;

import com.localgig.user.User;
import com.localgig.user.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class LocalGigUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public LocalGigUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .map(user -> (UserDetails) user)
                .orElseThrow(() ->
                        new UsernameNotFoundException("Account not found"));
    }
}