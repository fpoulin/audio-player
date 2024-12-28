// This file is the entry point of the Vue.js application.
// It initializes the Vue instance and sets up the router.

import { createApp } from 'vue'
import App from './App.vue'
import router from './router'

// Create Vue app
const app = createApp(App)

// Enable debug mode
app.config.devtools = true
app.config.debug = true
app.config.performance = true

// Use router
app.use(router)

// Mount app
app.mount('#app')
