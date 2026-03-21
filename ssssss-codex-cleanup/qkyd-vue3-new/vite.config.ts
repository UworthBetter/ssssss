import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')

  return {
    plugins: [vue()],
    resolve: {
      alias: {
        '@': path.resolve(__dirname, 'src')
      }
    },
    server: {
      port: 8080,
      proxy: {
        '/prod-api': {
          target: env.VITE_PROXY_TARGET || 'https://api.cecm.site',
          changeOrigin: true,
          secure: false
        },
        '/ai': {
          target: 'http://localhost:8011',
          changeOrigin: true
        }
      }
    }
  }
})
