package com.vansh.removeBackground.service.impl;

import com.vansh.removeBackground.client.ClipDropClient;
import com.vansh.removeBackground.service.RemoveBackgroundService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class RemoveBackgroundServiceImpl implements RemoveBackgroundService {

    @Value("${clipdrop.apiKey}")
    private String apiKey;

    private final ClipDropClient clipDropClient;

    @Override
    public byte[] removeBackground(MultipartFile file) {
        return clipDropClient.removeBackground(file, apiKey);
    }
}
