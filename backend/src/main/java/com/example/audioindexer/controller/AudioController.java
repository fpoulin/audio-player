package com.example.audioindexer.controller;

import com.example.audioindexer.model.AudioFile;
import com.example.audioindexer.service.AudioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/audio")
public class AudioController {

    private static final Logger logger = LoggerFactory.getLogger(AudioController.class);
    private final AudioService audioService;

    public AudioController(AudioService audioService) {
        this.audioService = audioService;
    }

    @GetMapping
    public ResponseEntity<List<AudioFile>> getAllAudioFiles() {
        List<AudioFile> audioFiles = audioService.getAllAudioFiles();
        return new ResponseEntity<>(audioFiles, HttpStatus.OK);
    }

    // Step 1: Initialize upload and get upload ID
    @PostMapping("/init")
    public ResponseEntity<AudioFile> initializeUpload(@RequestBody AudioFile audioFile) {
        logger.debug("Initializing upload for file: {}", audioFile.getFileName());
        try {
            AudioFile initialized = audioService.initializeAudioFile(audioFile);
            return new ResponseEntity<>(initialized, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error initializing upload: ", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Step 2: Handle the actual file upload
    @PostMapping(value = "/{id}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AudioFile> uploadFile(
            @PathVariable("id") String id,
            @RequestParam("file") MultipartFile file) {
        logger.debug("Receiving file upload for id: {}", id);
        
        try {
            AudioFile result = audioService.handleFileUpload(id, file);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Error processing file upload: ", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<AudioFile> getAudioFileById(@PathVariable("id") String id) {        
        try {
            AudioFile audioFile = audioService.getAudioFileById(id);
            return new ResponseEntity<>(audioFile, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAudioFileById(@PathVariable("id") String id) {
        boolean deleted = audioService.deleteAudioFileById(id);
        if (deleted) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{id}/stream")
    public ResponseEntity<Resource> streamAudio(@PathVariable String id) {
        try {
            AudioFile audioFile = audioService.getAudioFileById(id);
            Path path = Paths.get(audioFile.getFilePath());
            Resource resource = new FileSystemResource(path);

            if (resource.exists()) {
                return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("audio/mpeg"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + audioFile.getFileName() + "\"")
                    .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}