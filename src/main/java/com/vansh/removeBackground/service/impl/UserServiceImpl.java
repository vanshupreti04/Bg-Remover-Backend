package com.vansh.removeBackground.service.impl;

import com.vansh.removeBackground.dto.UserDTO;
import com.vansh.removeBackground.entity.UserEntity;
import com.vansh.removeBackground.repository.UserRepository;
import com.vansh.removeBackground.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDTO saveUser(UserDTO userDTO) {
        System.out.println("=== USER SERVICE DEBUG ===");
        System.out.println("Received userDTO photoUrl: " + userDTO.getPhotoUrl());
        System.out.println("Received userDTO photoUrl is null? " + (userDTO.getPhotoUrl() == null));
        System.out.println("Received userDTO photoUrl is empty? " + (userDTO.getPhotoUrl() != null && userDTO.getPhotoUrl().isEmpty()));

        Optional<UserEntity> optionalUser = userRepository.findByClerkId(userDTO.getClerkId());

        if(optionalUser.isPresent()){
            System.out.println("Found existing user, updating...");
            UserEntity existingUser = optionalUser.get();
            System.out.println("Old photoUrl in DB: " + existingUser.getPhotoUrl());

            existingUser.setEmail(userDTO.getEmail());
            existingUser.setFirstName(userDTO.getFirstName());
            existingUser.setLastName(userDTO.getLastName());
            existingUser.setPhotoUrl(userDTO.getPhotoUrl()); // ✅ This sets it

            if(userDTO.getCredits() != null){
                existingUser.setCredits(userDTO.getCredits());
            }
            existingUser = userRepository.save(existingUser);
            System.out.println("Saved photoUrl: " + existingUser.getPhotoUrl());
            return mapToDTO(existingUser);
        }

        System.out.println("Creating new user...");
        UserEntity newUser = mapToEntity(userDTO);
        userRepository.save(newUser);
        System.out.println("New user photoUrl: " + newUser.getPhotoUrl());

        return mapToDTO(newUser);
    }

    @Override
    public UserDTO getUserByClerkId(String clerkId) {
        UserEntity userEntity = userRepository.findByClerkId(clerkId)
                .orElseThrow(() -> new UsernameNotFoundException("User not Found"));
        return mapToDTO(userEntity);
    }

    @Override
    public void deleteByClerkId(String clerkId) {
        System.out.println("=== DELETE USER BY CLERK ID ===");
        System.out.println("Looking for user with clerkId: '" + clerkId + "'");

        try {
            Optional<UserEntity> userOptional = userRepository.findByClerkId(clerkId);

            if (userOptional.isPresent()) {
                UserEntity userEntity = userOptional.get();
                System.out.println("Found user to delete:");
                System.out.println("- ID: " + userEntity.getId());
                System.out.println("- Email: " + userEntity.getEmail());
                System.out.println("- Clerk ID: " + userEntity.getClerkId());

                userRepository.delete(userEntity);
                System.out.println("✅ User deleted from database");

                // Verify
                boolean stillExists = userRepository.existsByClerkId(clerkId);
                System.out.println("Verification - User still exists? " + stillExists);
            } else {
                System.out.println("⚠️ User not found with clerkId: '" + clerkId + "'");
                throw new UsernameNotFoundException("User not found with clerkId: " + clerkId);
            }

        } catch (Exception e) {
            System.out.println("❌ Error in deleteByClerkId: " + e.getMessage());
            throw e; // Re-throw to be caught in controller
        }
    }

    private UserEntity mapToEntity(UserDTO userDTO){
        return UserEntity.builder()
                .clerkId(userDTO.getClerkId())
                .email(userDTO.getEmail())
                .firstName(userDTO.getFirstName())
                .lastName(userDTO.getLastName())
                .photoUrl(userDTO.getPhotoUrl()) // ✅ This sets it
                .build();
    }

    private UserDTO mapToDTO(UserEntity newUser){
        return UserDTO.builder()
                .clerkId(newUser.getClerkId())
                .credits(newUser.getCredits())
                .email(newUser.getEmail())
                .firstName(newUser.getFirstName())
                .lastName(newUser.getLastName())
                .photoUrl(newUser.getPhotoUrl()) // ✅ ADD THIS LINE
                .build();
    }
}