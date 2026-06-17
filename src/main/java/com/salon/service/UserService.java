package com.salon.service;

import com.salon.model.User;
import com.salon.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public boolean register(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            return false;
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            return false;
        }
        user.setRole("customer");
        user.setCreatedAt(LocalDateTime.now());
        userRepository.save(user);
        return true;
    }

    // ADD THIS METHOD
    public boolean updateUserProfile(User updatedUser) {
        Optional<User> optionalUser = userRepository.findById(updatedUser.getId());
        if (optionalUser.isEmpty()) {
            return false;
        }

        User existingUser = optionalUser.get();

        // Update fields (except password, username, role)
        existingUser.setFullName(updatedUser.getFullName());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setPhone(updatedUser.getPhone());

        userRepository.save(existingUser);
        return true;
    }

    public User login(String username, String password) {
        Optional<User> user = userRepository.findByUsernameAndPassword(username, password);
        return user.orElse(null);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // ADD THIS METHOD - it's missing!
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // ADD THIS METHOD - for convenience
    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }
}