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
            System.out.println("=== WEBHOOK RECEIVED ===");

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
            System.out.println("Webhook event type: " + eventType);

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
            System.out.println("❌ Webhook error: " + e.getMessage());
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
        System.out.println("\n" + "=".repeat(50));
        System.out.println("=== DELETE WEBHOOK PROCESSING ===");
        System.out.println("Time: " + new java.util.Date());

        String clerkId = data.path("id").asText().trim();
        System.out.println("Clerk ID to delete: '" + clerkId + "'");
        System.out.println("Clerk ID length: " + clerkId.length());

        try {
            userService.deleteByClerkId(clerkId);
            System.out.println("✅ DELETE SUCCESS - User removed from database");
        } catch (UsernameNotFoundException e) {
            System.out.println("⚠️ USER NOT FOUND - Already deleted or never existed: " + clerkId);
            // This is OK - user might have been deleted already
        } catch (Exception e) {
            System.out.println("❌ DELETE ERROR: " + e.getMessage());
            e.printStackTrace();
            // Don't rethrow - just log it
        }

        System.out.println("=== DELETE WEBHOOK COMPLETED ===");
        System.out.println("=".repeat(50) + "\n");
    }

    private void handleUserUpdated(JsonNode data) {
        System.out.println("=== WEBHOOK - USER UPDATED ===");
        String clerkId = data.path("id").asText();
        System.out.println("Updating user: " + clerkId);

        try {
            UserDTO existingUser = userService.getUserByClerkId(clerkId);

            System.out.println("Found existing user, updating...");

            String newEmail = data.path("email_addresses").path(0).path("email_address").asText();
            String newFirstName = data.path("first_name").asText();
            String newLastName = data.path("last_name").asText();
            String newPhotoUrl = data.path("image_url").asText();

            System.out.println("New email: " + newEmail);
            System.out.println("New first name: " + newFirstName);
            System.out.println("New last name: " + newLastName);
            System.out.println("New photo URL: " + newPhotoUrl);

            existingUser.setEmail(newEmail);
            existingUser.setFirstName(newFirstName);
            existingUser.setLastName(newLastName);
            existingUser.setPhotoUrl(newPhotoUrl);

            userService.saveUser(existingUser);
            System.out.println("✅ User updated successfully");

        } catch (UsernameNotFoundException e) {
            System.out.println("⚠️ User not found for update - might be a delete operation");
            System.out.println("Skipping update for clerkId: " + clerkId);
            // Don't create new user, just ignore
        } catch (Exception e) {
            System.out.println("❌ Error in user update: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleUserCreated(JsonNode data) {
        System.out.println("=== WEBHOOK - USER CREATED ===");

        String clerkId = data.path("id").asText();
        String email = data.path("email_addresses").path(0).path("email_address").asText();
        String firstName = data.path("first_name").asText();
        String lastName = data.path("last_name").asText();
        String photoUrl = data.path("image_url").asText();

        System.out.println("Creating user:");
        System.out.println("- clerkId: " + clerkId);
        System.out.println("- email: " + email);
        System.out.println("- firstName: " + firstName);
        System.out.println("- lastName: " + lastName);
        System.out.println("- photoUrl: " + photoUrl);

        UserDTO newUser = UserDTO.builder()
                .clerkId(clerkId)
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .photoUrl(photoUrl) // ✅ ADD THIS LINE - THIS WAS MISSING!
                .build();

        userService.saveUser(newUser);
        System.out.println("✅ User created successfully");
    }

    private boolean verifyWebhookSignature(String svixId, String svixTimestamp, String svixSignature, String payload) {
        // For now, return true for testing
        // In production, implement proper Svix signature verification
        return true;
    }
}