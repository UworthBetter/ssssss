import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  server: {
    port: 8080,
    proxy: {
      '/dev-api': {
        target: 'http://localhost:8098',
        changeOrigin: true
      },
      '/ai': {
        target: 'http://localhost:8011',
        changeOrigin: true
      }
    }
  }
})
