<template>
  <div class="home">
    <h1>Audio Files</h1>
    <div v-if="loading">Loading...</div>
    <div v-else-if="error">{{ error }}</div>
    <div v-else>
      <div v-if="audioFiles.length === 0">No audio files found</div>
      <div v-else class="audio-list">
        <div v-for="file in audioFiles" :key="file.id" class="audio-item">
          <AudioPlayer 
            v-if="selectedFile && selectedFile.id === file.id"
            :audioFile="selectedFile"
          />
          <div v-else class="audio-info" @click="playAudio(file)">
            <h3>{{ file.fileName }}</h3>
            <p>{{ file.filePath }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import AudioPlayer from '@/components/AudioPlayer.vue'
import audioService from '@/services/audioService'

export default {
  name: 'HomeView',
  components: {
    AudioPlayer
  },
  setup() {
    const audioFiles = ref([])
    const selectedFile = ref(null)
    const loading = ref(true)
    const error = ref(null)

    async function loadAudioFiles() {
      try {
        loading.value = true
        error.value = null
        const response = await audioService.getAllAudioFiles()
        audioFiles.value = response.data
        console.log('Loaded audio files:', audioFiles.value)
      } catch (err) {
        console.error('Failed to load audio files:', err)
        error.value = 'Failed to load audio files'
      } finally {
        loading.value = false
      }
    }

    function playAudio(file) {
      console.log('Playing audio file:', file)
      selectedFile.value = file
    }

    onMounted(() => {
      loadAudioFiles()
    })

    return {
      audioFiles,
      selectedFile,
      loading,
      error,
      playAudio
    }
  }
}
</script>

<style scoped>
.home {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.audio-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.audio-item {
  cursor: pointer;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.audio-item:hover {
  background-color: #f5f5f5;
}

.audio-info h3 {
  margin: 0 0 5px 0;
}

.audio-info p {
  margin: 0;
  color: #666;
  font-size: 0.9em;
}
</style>
