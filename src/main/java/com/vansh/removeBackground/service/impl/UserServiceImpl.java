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

        Optional<UserEntity> optionalUser = userRepository.findByClerkId(userDTO.getClerkId());

        if(optionalUser.isPresent()){
            UserEntity existingUser = optionalUser.get();

            existingUser.setEmail(userDTO.getEmail());
            existingUser.setFirstName(userDTO.getFirstName());
            existingUser.setLastName(userDTO.getLastName());
            existingUser.setPhotoUrl(userDTO.getPhotoUrl());

            if(userDTO.getCredits() != null){
                existingUser.setCredits(userDTO.getCredits());
            }
            existingUser = userRepository.save(existingUser);
            return mapToDTO(existingUser);
        }

        UserEntity newUser = mapToEntity(userDTO);
        userRepository.save(newUser);

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

        try {
            Optional<UserEntity> userOptional = userRepository.findByClerkId(clerkId);

            if (userOptional.isPresent()) {
                UserEntity userEntity = userOptional.get();
                userRepository.delete(userEntity);

                boolean stillExists = userRepository.existsByClerkId(clerkId);
            } else {
                throw new UsernameNotFoundException("User not found with clerkId: " + clerkId);
            }

        } catch (Exception e) {
            throw e;
        }
    }

    private UserEntity mapToEntity(UserDTO userDTO){
        return UserEntity.builder()
                .clerkId(userDTO.getClerkId())
                .email(userDTO.getEmail())
                .firstName(userDTO.getFirstName())
                .lastName(userDTO.getLastName())
                .photoUrl(userDTO.getPhotoUrl())
                .build();
    }

    private UserDTO mapToDTO(UserEntity newUser){
        return UserDTO.builder()
                .clerkId(newUser.getClerkId())
                .credits(newUser.getCredits())
                .email(newUser.getEmail())
                .firstName(newUser.getFirstName())
                .lastName(newUser.getLastName())
                .photoUrl(newUser.getPhotoUrl())
                .build();
    }
}