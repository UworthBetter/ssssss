import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, __dirname, '')

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
        '/dev-api': {
          target: 'http://localhost:8098',
          changeOrigin: true
        },
        // Local dev uses same-origin proxy to Lighthouse backend to avoid browser CORS issues.
        '/prod-api': {
          target: env.VITE_PROXY_TARGET || 'https://api.cecm.site',
          changeOrigin: true,
          secure: false
        }
      }
    }
  }
})
