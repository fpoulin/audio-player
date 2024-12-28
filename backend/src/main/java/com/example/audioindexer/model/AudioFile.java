package com.example.audioindexer.model;

public class AudioFile {
    private String id;
    private String fileName;
    private String filePath;
    private String metadata;

    public AudioFile() { 
        
    }

    public AudioFile(String fileName, String filePath, String metadata) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.metadata = metadata;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }
}