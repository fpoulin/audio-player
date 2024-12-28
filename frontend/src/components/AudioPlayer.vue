// /audio-indexer/audio-indexer/frontend/src/components/AudioPlayer.vue

<template>
  <div class="audio-player">
    <!-- Debug Panel - Always visible -->
    <div class="debug-panel">
      <h4>Debug Information</h4>
      <ul>
        <li>Audio ID: {{ audioFile.id }}</li>
        <li>File Path: {{ audioFile.filePath }}</li>
        <li>Audio Source URL: {{ audioSource }}</li>
        <li>Player State: {{ playerState }}</li>
        <li>Is Loaded: {{ isLoaded }}</li>
        <li>Is Playing: {{ isPlaying }}</li>
        <li>Has Error: {{ error }}</li>
        <li v-if="error">Error Message: {{ errorMessage }}</li>
      </ul>
    </div>
    
    <div class="audio-info">
      <h3>{{ audioFile.fileName }}</h3>
      <p class="metadata">{{ formatMetadata(audioFile.metadata) }}</p>
    </div>

    <div class="player-controls">
      <audio 
        ref="audioPlayer" 
        :src="audioSource" 
        @loadedmetadata="onAudioLoaded"
        @error="onAudioError"
        @play="onPlay"
        @pause="onPause"
        @ended="onEnded"
        @loadstart="onLoadStart"
        @waiting="onWaiting"
        @canplay="onCanPlay"
        controls
        preload="metadata"
      ></audio>
      <div class="controls">
        <button @click="togglePlay" :disabled="!isLoaded">
          {{ isPlaying ? 'Pause' : 'Play' }}
        </button>
        <span v-if="error" class="error">{{ errorMessage }}</span>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'

export default {
  name: 'AudioPlayer',
  props: {
    audioFile: {
      type: Object,
      required: true,
      validator: (value) => {
        return value && value.id && value.fileName;
      },
      default: () => ({
        id: '',
        fileName: '',
        filePath: '',
        metadata: '{}'
      })
    }
  },
  setup(props) {
    const isPlaying = ref(false)
    const isLoaded = ref(false)
    const error = ref(false)
    const errorMessage = ref('')
    const playerState = ref('initializing')
    const audioPlayer = ref(null)

    const audioSource = computed(() => {
      if (!props.audioFile?.id) {
        logDebug('No audio file ID available')
        return ''
      }
      const baseUrl = window.location.origin
      const url = `${baseUrl}/api/audio/${props.audioFile.id}/stream`
      logDebug('Audio URL:', url)
      return url
    })

    function logDebug(...args) {
      console.log('[AudioPlayer]', new Date().toISOString(), ...args)
    }

    async function checkAudioAvailability() {
      if (!audioSource.value) {
        logDebug('No audio source available')
        return false
      }
      try {
        logDebug('Checking audio availability...')
        const response = await axios.head(audioSource.value)
        logDebug('Audio stream check response:', response.status)
        return response.status === 200
      } catch (err) {
        logDebug('Audio stream check failed:', err.message)
        logDebug('Full error:', err)
        return false
      }
    }

    async function togglePlay() {
      if (!props.audioFile?.id) {
        logDebug('No audio file available')
        error.value = true
        errorMessage.value = 'No audio file available'
        return
      }

      logDebug('Toggle play called, current state:', {
        isPlaying: isPlaying.value,
        isLoaded: isLoaded.value,
        playerState: playerState.value,
        audioFile: props.audioFile
      })

      if (!audioPlayer.value) {
        logDebug('Audio player reference not found')
        return
      }
      
      try {
        if (isPlaying.value) {
          logDebug('Attempting to pause')
          audioPlayer.value.pause()
        } else {
          logDebug('Attempting to play')
          const available = await checkAudioAvailability()
          if (!available) {
            throw new Error('Audio stream not available')
          }
          logDebug('Stream available, starting playback')
          await audioPlayer.value.play()
        }
      } catch (err) {
        logDebug('Playback error:', err.message)
        error.value = true
        errorMessage.value = `Playback failed: ${err.message}`
      }
    }

    function onLoadStart() {
      logDebug('Load started')
      playerState.value = 'loading'
    }

    function onWaiting() {
      logDebug('Waiting for data')
      playerState.value = 'buffering'
    }

    function onCanPlay() {
      logDebug('Can play')
      playerState.value = 'ready'
      isLoaded.value = true
    }

    function onPlay() {
      logDebug('Playback started')
      isPlaying.value = true
      playerState.value = 'playing'
    }

    function onPause() {
      logDebug('Playback paused')
      isPlaying.value = false
      playerState.value = 'paused'
    }

    function onEnded() {
      logDebug('Playback ended')
      isPlaying.value = false
      playerState.value = 'ended'
    }

    function onAudioLoaded() {
      logDebug('Audio metadata loaded')
      isLoaded.value = true
      error.value = false
      playerState.value = 'loaded'
    }

    function onAudioError(e) {
      const err = e.target.error
      logDebug('Audio loading error:', err)
      error.value = true
      isLoaded.value = false
      playerState.value = 'error'
      errorMessage.value = `Failed to load audio: ${err ? err.message : 'Unknown error'}`
    }

    function formatMetadata(metadata) {
      if (!metadata) return 'No metadata available'
      try {
        const parsed = JSON.parse(metadata)
        return `Size: ${formatBytes(parsed.size)}, Created: ${new Date(parsed.created).toLocaleString()}`
      } catch (e) {
        return metadata || 'Invalid metadata'
      }
    }

    function formatBytes(bytes) {
      if (bytes === 0) return '0 Bytes'
      const k = 1024
      const sizes = ['Bytes', 'KB', 'MB', 'GB']
      const i = Math.floor(Math.log(bytes) / Math.log(k))
      return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
    }

    onMounted(async () => {
      logDebug('Component mounted, audio file:', props.audioFile)
      const available = await checkAudioAvailability()
      if (!available) {
        error.value = true
        errorMessage.value = 'Audio stream not available'
        playerState.value = 'error'
      }
    })

    return {
      isPlaying,
      isLoaded,
      error,
      errorMessage,
      playerState,
      audioPlayer,
      audioSource,
      togglePlay,
      onLoadStart,
      onWaiting,
      onCanPlay,
      onPlay,
      onPause,
      onEnded,
      onAudioLoaded,
      onAudioError,
      formatMetadata
    }
  }
}
</script>

<style scoped>
.audio-player {
  padding: 15px;
  border: 1px solid #ddd;
  border-radius: 4px;
  margin: 10px 0;
  background: #f9f9f9;
}

.audio-info {
  margin-bottom: 15px;
}

.audio-info h3 {
  margin: 0 0 8px 0;
  color: #333;
}

.debug-panel {
  margin: 10px 0;
  padding: 10px;
  background: #e9ecef;
  border-radius: 4px;
  text-align: left;
}

.debug-panel h4 {
  margin: 0 0 10px 0;
  color: #0066cc;
}

.debug-panel ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.debug-panel li {
  font-family: monospace;
  margin: 5px 0;
  color: #666;
}

.player-controls {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.controls {
  display: flex;
  align-items: center;
  gap: 10px;
}

audio {
  width: 100%;
  margin-bottom: 10px;
}

button {
  padding: 8px 20px;
  border: none;
  border-radius: 4px;
  background-color: #4CAF50;
  color: white;
  cursor: pointer;
  transition: background-color 0.2s;
}

button:hover:not(:disabled) {
  background-color: #45a049;
}

button:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
}

.error {
  color: #ff4444;
  font-size: 0.9em;
  max-width: 300px;
}
</style>
