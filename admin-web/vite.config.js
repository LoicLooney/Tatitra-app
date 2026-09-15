import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// host: true → admin joignable depuis le téléphone de démo (même Wi-Fi).
export default defineConfig({
  plugins: [react()],
  server: {
    host: true,
    port: 5173,
  },
  preview: {
    host: true,
    port: 4173,
  },
})
