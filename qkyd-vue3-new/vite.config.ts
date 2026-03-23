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
    build: {
      rollupOptions: {
        output: {
          manualChunks(id) {
            if (!id.includes('node_modules')) {
              return
            }

            if (id.includes('echarts') || id.includes('@amap/amap-jsapi-loader') || id.includes('@jiaminghi/data-view')) {
              return 'vendor-visual'
            }

            if (id.includes('element-plus')) {
              return 'vendor-element'
            }

            if (id.includes('vue-cropper') || id.includes('@vueup/vue-quill')) {
              return 'vendor-editor'
            }

            if (id.includes('vue') || id.includes('pinia') || id.includes('vue-router') || id.includes('axios')) {
              return 'vendor-core'
            }
          }
        }
      }
    },
    server: {
      port: 8080,
      proxy: {
        '/dev-api': {
          target: 'http://localhost:8098',
          changeOrigin: true
        },
        '/ws': {
          target: env.VITE_PROXY_TARGET || 'http://localhost:8098',
          changeOrigin: true,
          ws: true,
          secure: false
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
