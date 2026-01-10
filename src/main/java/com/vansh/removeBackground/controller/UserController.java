package com.vansh.removeBackground.controller;

import com.vansh.removeBackground.dto.UserDTO;
import com.vansh.removeBackground.response.RemoveBgResponse;
import com.vansh.removeBackground.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<?> createOrUpdateUser(@RequestBody UserDTO userDTO, Authentication authentication){
        System.out.println("=== SPRING BOOT DEBUG ===");
        System.out.println("Received request to /api/users");
        System.out.println("UserDTO: " + userDTO);
        System.out.println("Authentication: " + authentication);
        System.out.println("Authentication name: " + (authentication != null ? authentication.getName() : "null"));

        RemoveBgResponse response = null;
        try{
            // Log the check
            System.out.println("Checking if authentication.getName() equals userDTO.getClerkId()");
            System.out.println("auth.getName(): " + (authentication != null ? authentication.getName() : "null"));
            System.out.println("userDTO.getClerkId(): " + userDTO.getClerkId());

            if(!authentication.getName().equals(userDTO.getClerkId())){
                System.out.println("❌ Permission denied: Clerk IDs don't match");
                response = RemoveBgResponse.builder()
                        .success(false)
                        .data("User does not have permission")
                        .statusCode(HttpStatus.FORBIDDEN)
                        .build();
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            System.out.println("✅ Clerk IDs match, saving user...");
            UserDTO user = userService.saveUser(userDTO);
            response = RemoveBgResponse.builder()
                    .success(true)
                    .data(user)
                    .statusCode(HttpStatus.OK)
                    .build();
            System.out.println("✅ User saved successfully");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
        catch(Exception exception){
            System.out.println("❌ Exception occurred: " + exception.getMessage());
            exception.printStackTrace();
            response = RemoveBgResponse.builder()
                    .success(false)
                    .data(exception.getMessage())
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/credits")
    public ResponseEntity<?> getUserCredits(Authentication authentication){
        RemoveBgResponse bgResponse = null;
        try{
            if(authentication.getName().isEmpty() || authentication.getName() == null){
                bgResponse = RemoveBgResponse.builder().statusCode(HttpStatus.FORBIDDEN).data("User does not have permission").success(false).build();
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(bgResponse);
            }
            String clerkId = authentication.getName();
            UserDTO existingUser = userService.getUserByClerkId(clerkId);

            Map<String,Integer> map = new HashMap<>();
            map.put("credits",existingUser.getCredits());

            bgResponse = RemoveBgResponse.builder().statusCode(HttpStatus.OK).data(map).success(true).build();

            return ResponseEntity.status(HttpStatus.OK).body(bgResponse);
        }
        catch (Exception e){

            bgResponse = RemoveBgResponse.builder().statusCode(HttpStatus.INTERNAL_SERVER_ERROR).data("Something went wrong").success(false).build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(bgResponse);
        }
    }
}
