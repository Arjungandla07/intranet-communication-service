package com.factory.intranet_communication.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {
    private final Path uploadDir;

    public FileStorageService(@Value("${file.upload-dir}") String uploadDir) throws IOException {
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(this.uploadDir);
    }

    public String storeFile(MultipartFile file) {

        if (file.isEmpty()) {
            throw new IllegalStateException("Cannot store empty file");
        }

        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path target = uploadDir.resolve(filename);

        try {
            Files.copy(file.getInputStream(), target);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to store file", e);
        }

        return target.toString(); // stored as mediaPath
    }
}
