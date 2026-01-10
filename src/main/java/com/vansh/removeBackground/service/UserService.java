package com.vansh.removeBackground.service;

import com.vansh.removeBackground.dto.UserDTO;

public interface UserService {

    UserDTO saveUser(UserDTO userDTO);

    UserDTO getUserByClerkId(String clerkId);

    void deleteByClerkId(String clerkId);
}
