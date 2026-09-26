package com.tech10x.ukm.security;

import com.tech10x.ukm.entity.Role;
import com.tech10x.ukm.entity.Users;
import com.tech10x.ukm.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Bridges Spring Security to our own "users" table. Spring Security has no idea
 * how our schema looks - this class is the only place that translates our User
 * entity into the UserDetails contract Security actually understands.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsersRepository usersRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        Users user = usersRepository.findByUserId(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        String[] authorities = user.getRoles().stream()
                .map(Role::getName)
                .toArray(String[]::new);

        return User
                .withUsername(user.getUserId())
                .password(user.getPassword())
                .authorities(authorities)
                .build();
    }
}
