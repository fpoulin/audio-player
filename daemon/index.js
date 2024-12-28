const chokidar = require('chokidar');
const axios = require('axios');
const path = require('path');
const fs = require('fs');
const fsPromises = require('fs').promises;
const FormData = require('form-data');

// Get configuration from environment variables with defaults
const API_URL = process.env.API_URL || 'http://localhost:8080';
const API_ENDPOINT = `${API_URL}/api/audio`;
const ROOT_FOLDER = process.env.WATCH_DIR || process.env.HOME + '/Music';
const MAX_RETRIES = 3;
const CHUNK_SIZE = 5 * 1024 * 1024; // 5MB chunks

console.log(`Using API endpoint: ${API_ENDPOINT}`);
console.log(`Watching directory: ${ROOT_FOLDER}`);

function hasEblParent(filePath) {
    const parentDir = path.dirname(filePath);
    const parentFolderName = path.basename(parentDir);
    return parentFolderName === 'ebl';
}

async function initializeUpload(audioFile) {
    try {
        const response = await axios.post(`${API_ENDPOINT}/init`, audioFile, {
            headers: { 'Content-Type': 'application/json' }
        });
        return response.data;
    } catch (error) {
        console.error('Error initializing upload:', error.message);
        throw error;
    }
}

async function uploadFile(id, filePath, retryCount = 0) {
    try {
        const formData = new FormData();
        const fileStream = fs.createReadStream(filePath);
        formData.append('file', fileStream);

        const response = await axios.post(`${API_ENDPOINT}/${id}/upload`, formData, {
            headers: {
                ...formData.getHeaders(),
                'Content-Type': 'multipart/form-data'
            },
            maxContentLength: Infinity,
            maxBodyLength: Infinity,
            timeout: 300000 // 5 minutes timeout
        });

        console.log(`Successfully uploaded file with ID: ${id}`);
        return response.data;
    } catch (error) {
        console.error(`Upload error (attempt ${retryCount + 1}/${MAX_RETRIES}):`, error.message);
        
        if (retryCount < MAX_RETRIES - 1) {
            console.log(`Retrying upload in 5 seconds...`);
            await new Promise(resolve => setTimeout(resolve, 5000));
            return uploadFile(id, filePath, retryCount + 1);
        }
        throw error;
    }
}

async function indexAudioFile(filePath) {
    try {
        const stats = await fsPromises.stat(filePath);
        const fileName = path.basename(filePath);
        
        // Step 1: Initialize the upload
        const audioFileData = {
            fileName: fileName,
            filePath: filePath,
            metadata: JSON.stringify({
                size: stats.size,
                created: stats.birthtime,
                modified: stats.mtime
            })
        };
        
        const initializedFile = await initializeUpload(audioFileData);
        console.log(`Upload initialized for: ${fileName}`);
        
        // Step 2: Upload the file
        const result = await uploadFile(initializedFile.id, filePath);
        console.log(`Completed upload process for: ${fileName}`);
        
        return result;
    } catch (error) {
        console.error('Upload process failed:', error.message);
        if (error.response) {
            console.error('Response error details:', {
                status: error.response.status,
                data: error.response.data
            });
        }
        throw error;
    }
}

const watcher = chokidar.watch(ROOT_FOLDER, {
    persistent: true,
    ignoreInitial: false,
    ignored: /(^|[\/\\])\../,
    awaitWriteFinish: {
        stabilityThreshold: 2000,
        pollInterval: 100
    }
});

watcher
    .on('add', async (filePath) => {
        const ext = path.extname(filePath).toLowerCase();
        if ((ext === '.wav' || ext === '.mp3') && hasEblParent(filePath)) {
            console.log(`New audio file detected: ${filePath}`);
            try {
                await indexAudioFile(filePath);
            } catch (error) {
                console.error(`Failed to process file ${filePath}:`, error.message);
            }
        }
    })
    .on('error', error => console.error(`Watcher error: ${error}`));

// Initial scan
console.log(`Starting to watch ${ROOT_FOLDER} for audio files in 'ebl' folders...`); 