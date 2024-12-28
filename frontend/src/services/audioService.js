import axios from 'axios'

const API_URL = '/api/audio/'

export default {
  async getAllAudioFiles () {
    return axios.get(API_URL)
  },

  async getAudioFile (id) {
    return axios.get(API_URL + id)
  },

  async createAudioFile (audioFile) {
    return axios.post(API_URL, audioFile)
  },

  async deleteAudioFile (id) {
    return axios.delete(API_URL + id)
  }
}
