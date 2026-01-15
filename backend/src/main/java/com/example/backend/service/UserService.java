package com.example.backend.service;

import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findByActiveTrue(pageable);
    }
    
    public Page<User> searchUsers(String search, Pageable pageable) {
        if (search == null || search.trim().isEmpty()) {
            return getAllUsers(pageable);
        }
        return userRepository.findActiveUsersBySearch(search.trim(), pageable);
    }
    
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id)
                .filter(User::getActive);
    }
    
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .filter(User::getActive);
    }
    
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .filter(User::getActive);
    }
    
    public User createUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }
    
    public Optional<User> updateUser(Long id, User userDetails) {
        return userRepository.findById(id)
                .filter(User::getActive)
                .map(user -> {
                    user.setUsername(userDetails.getUsername());
                    user.setEmail(userDetails.getEmail());
                    if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
                        user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
                    }
                    return userRepository.save(user);
                });
    }
    
    public boolean deleteUser(Long id) {
        return userRepository.findById(id)
                .filter(User::getActive)
                .map(user -> {
                    user.setActive(false);
                    userRepository.save(user);
                    return true;
                })
                .orElse(false);
    }
    
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}