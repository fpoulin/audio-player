package com.example.audioindexer;

import com.example.audioindexer.model.AudioFile;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.core.ParameterizedTypeReference;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AudioIndexerApplicationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String getBaseUrl() {
        return "http://localhost:" + port + "/api/audio/";
    }

    @Test
    void contextLoads() {
        // This test verifies that the Spring application context loads successfully
    }

    @Test
    void testPostAudioFile() {
        // Test POST - Create new audio file
        AudioFile newAudioFile = new AudioFile("test.mp3", "/path/to/test.mp3", "test metadata");
        ResponseEntity<AudioFile> response = restTemplate.postForEntity(
            getBaseUrl(), newAudioFile, AudioFile.class);
        
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
        assertEquals("test.mp3", response.getBody().getFileName());
    }

    @Test
    void testGetAudioFileById() {
        // First create a file to get
        AudioFile newAudioFile = new AudioFile("test-get.mp3", "/path/test-get.mp3", "test metadata");
        AudioFile createdFile = restTemplate.postForEntity(
            getBaseUrl(), newAudioFile, AudioFile.class).getBody();
        
        // Test GET by ID
        ResponseEntity<AudioFile> response = restTemplate.getForEntity(
            getBaseUrl() + createdFile.getId(), AudioFile.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(createdFile.getId(), response.getBody().getId());
    }

    @Test
    void testGetNonExistentAudioFile() {
        // Test GET with non-existent ID
        ResponseEntity<AudioFile> response = restTemplate.getForEntity(
            getBaseUrl() + "non-existent-id", AudioFile.class);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testGetAllAudioFiles() {
        // Create a test file first
        AudioFile newAudioFile = new AudioFile("test-getall.mp3", "/path/test-getall.mp3", "test metadata");
        restTemplate.postForEntity(getBaseUrl(), newAudioFile, AudioFile.class);
        
        // Test GET all
        ResponseEntity<List<AudioFile>> response = restTemplate.exchange(
            getBaseUrl(),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<AudioFile>>() {});
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().size() > 0);
    }

    @Test
    void testDeleteAudioFile() {
        // First create a file to delete
        AudioFile newAudioFile = new AudioFile("test-delete.mp3", "/path/test-delete.mp3", "test metadata");
        AudioFile createdFile = restTemplate.postForEntity(
            getBaseUrl(), newAudioFile, AudioFile.class).getBody();
        
        // Test DELETE
        restTemplate.delete(getBaseUrl() + createdFile.getId());
        
        // Verify deletion
        ResponseEntity<AudioFile> response = restTemplate.getForEntity(
            getBaseUrl() + createdFile.getId(), AudioFile.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}