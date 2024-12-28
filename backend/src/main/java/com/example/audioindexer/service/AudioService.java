package com.example.audioindexer.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.example.audioindexer.model.AudioFile;
import com.example.audioindexer.config.UploadConfig;
import com.example.audioindexer.exception.AudioFileStorageException;

@Service
public class AudioService {
    private static final Logger logger = LoggerFactory.getLogger(AudioService.class);
    private final Map<String, AudioFile> audioFiles = new HashMap<>();
    private final String uploadDir;

    public AudioService(UploadConfig uploadConfig) {
        this.uploadDir = uploadConfig.getDir();
    }

    public List<AudioFile> getAllAudioFiles() {
        return new ArrayList<>(audioFiles.values());
    }

    public AudioFile initializeAudioFile(AudioFile audioFile) {
        String id = UUID.randomUUID().toString();
        audioFile.setId(id);
        
        try {
            Files.createDirectories(Paths.get(uploadDir));
        } catch (IOException e) {
            logger.error("Could not create upload directory: {}", uploadDir);
            throw new AudioFileStorageException("Failed to create upload directory", e);
        }
        
        audioFiles.put(id, audioFile);
        logger.debug("Initialized audio file: {}", audioFile.getFileName());
        return audioFile;
    }

    public AudioFile handleFileUpload(String id, MultipartFile file) {
        AudioFile audioFile = getAudioFileById(id);
        if (audioFile == null) {
            throw new NoSuchElementException("No initialized upload found for id: " + id);
        }

        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String uniqueFileName = id + "_" + file.getOriginalFilename();
            Path targetLocation = uploadPath.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            audioFile.setFilePath(targetLocation.toString());
            audioFiles.put(id, audioFile);

            logger.debug("Successfully uploaded file: {} to location: {}", file.getOriginalFilename(), targetLocation);
            return audioFile;

        } catch (IOException e) {
            logger.error("Failed to upload file: {}", file.getOriginalFilename(), e);
            throw new AudioFileStorageException("Failed to upload file", e);
        }
    }

    public AudioFile getAudioFileById(String id) {
        AudioFile audioFile = audioFiles.get(id);
        if (audioFile == null) {
            throw new NoSuchElementException("Audio file not found with id: " + id);
        }
        return audioFile;
    }

    public boolean deleteAudioFileById(String id) {
        AudioFile audioFile = audioFiles.get(id);
        if (audioFile != null && audioFile.getFilePath() != null) {
            try {
                // Delete the physical file if it exists
                Path filePath = Paths.get(audioFile.getFilePath());
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                logger.error("Failed to delete file: {}", audioFile.getFilePath(), e);
            }
        }
        
        AudioFile removedFile = audioFiles.remove(id);
        return removedFile != null;
    }
}