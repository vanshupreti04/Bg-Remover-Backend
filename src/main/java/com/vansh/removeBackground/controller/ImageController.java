package com.vansh.removeBackground.controller;

import com.vansh.removeBackground.dto.UserDTO;
import com.vansh.removeBackground.response.RemoveBgResponse;
import com.vansh.removeBackground.service.RemoveBackgroundService;
import com.vansh.removeBackground.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {

    private final RemoveBackgroundService removeBackgroundService;
    private final UserService userService;

    @PostMapping("/remove-background")
    public ResponseEntity<?> removeBackground(@RequestParam("file")MultipartFile file, Authentication authentication){

        RemoveBgResponse response = null;
        Map<String, Object> responseMap = new HashMap<>();

        try{
            if(authentication.getName().isEmpty() || authentication.getName() == null){
                response = RemoveBgResponse.builder().statusCode(HttpStatus.FORBIDDEN).success(false).data("User does not have permission").build();
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            UserDTO userDTO = userService.getUserByClerkId(authentication.getName());

            if(userDTO.getCredits() == 0){
                responseMap.put("message", "No credit balance");
                responseMap.put("creditBalance", userDTO.getCredits());
                RemoveBgResponse.builder().success(false).data(responseMap).statusCode(HttpStatus.OK).build();
                return ResponseEntity.ok(response);
            }

            byte[] imageBytes = removeBackgroundService.removeBackground(file);
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);

            userDTO.setCredits(userDTO.getCredits()-1);

            userService.saveUser(userDTO);

            return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body(base64Image);
        }
        catch (Exception e){
            response = RemoveBgResponse.builder().statusCode(HttpStatus.INTERNAL_SERVER_ERROR).success(false).data("Something went wrong !!").build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
