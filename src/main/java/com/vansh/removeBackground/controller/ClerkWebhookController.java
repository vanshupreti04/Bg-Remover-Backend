package com.vansh.removeBackground.controller;

import com.vansh.removeBackground.dto.UserDTO;
import com.vansh.removeBackground.response.RemoveBgResponse;
import com.vansh.removeBackground.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
public class ClerkWebhookController {

    @Value("${clerk.webhook.secret}")
    private String webhookSecret;

    private final UserService userService;

    @PostMapping("/clerk")
    public ResponseEntity<?> handleClerkWebhook(@RequestHeader("svix-id") String svixId,
                                                @RequestHeader("svix-timestamp") String svixTimestamp,
                                                @RequestHeader("svix-signature") String svixSignature,
                                                @RequestBody String payload){
        RemoveBgResponse response = null;
        try{

            boolean isValid = verifyWebhookSignature(svixId, svixTimestamp, svixSignature, payload);
            if(!isValid){
                response = RemoveBgResponse.builder()
                        .statusCode(HttpStatus.UNAUTHORIZED)
                        .data("Invalid webhook Signature")
                        .success(false)
                        .build();
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(response);
            }

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(payload);

            String eventType = rootNode.path("type").asText();

            switch (eventType){
                case "user.created":
                    handleUserCreated(rootNode.path("data"));
                    break;
                case "user.updated":
                    handleUserUpdated(rootNode.path("data"));
                    break;
                case "user.deleted":
                    handleUserDeleted(rootNode.path("data"));
                    break;
                default:
                    System.out.println("Unknown event type: " + eventType);
            }
            return ResponseEntity.ok().build();
        }
        catch(Exception e){
            e.printStackTrace();
            response = RemoveBgResponse.builder()
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR)
                    .data("Something went Wrong")
                    .success(false)
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }
    }

    private void handleUserDeleted(JsonNode data) {

        String clerkId = data.path("id").asText().trim();

        try {
            userService.deleteByClerkId(clerkId);
            System.out.println("✅ DELETE SUCCESS - User removed from database");
        } catch (UsernameNotFoundException e) {
            System.out.println("⚠️ USER NOT FOUND - Already deleted or never existed: " + clerkId);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void handleUserUpdated(JsonNode data) {

        String clerkId = data.path("id").asText();

        try {
            UserDTO existingUser = userService.getUserByClerkId(clerkId);

            String newEmail = data.path("email_addresses").path(0).path("email_address").asText();
            String newFirstName = data.path("first_name").asText();
            String newLastName = data.path("last_name").asText();
            String newPhotoUrl = data.path("image_url").asText();

            existingUser.setEmail(newEmail);
            existingUser.setFirstName(newFirstName);
            existingUser.setLastName(newLastName);
            existingUser.setPhotoUrl(newPhotoUrl);

            userService.saveUser(existingUser);

        } catch (UsernameNotFoundException e) {
            System.out.println("⚠️ User not found for update - might be a delete operation");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleUserCreated(JsonNode data) {

        String clerkId = data.path("id").asText();
        String email = data.path("email_addresses").path(0).path("email_address").asText();
        String firstName = data.path("first_name").asText();
        String lastName = data.path("last_name").asText();
        String photoUrl = data.path("image_url").asText();


        UserDTO newUser = UserDTO.builder()
                .clerkId(clerkId)
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .photoUrl(photoUrl)
                .build();

        userService.saveUser(newUser);
    }

    private boolean verifyWebhookSignature(String svixId, String svixTimestamp, String svixSignature, String payload) {
        return true;
    }
}