import { defineStore } from 'pinia'

interface AppState {
  sidebar: {
    opened: boolean
    withoutAnimation: boolean
  }
  device: 'mobile' | 'desktop'
  size: string
}

export const useAppStore = defineStore('app', {
  state: (): AppState => ({
    sidebar: {
      opened: true,
      withoutAnimation: false
    },
    device: 'desktop',
    size: 'default'
  }),

  actions: {
    toggleSidebar() {
      this.sidebar.opened = !this.sidebar.opened
    },

    closeSidebar() {
      this.sidebar.opened = false
    },

    openSidebar() {
      this.sidebar.opened = true
    },

    setDevice(device: 'mobile' | 'desktop') {
      this.device = device
    },

    setSize(size: string) {
      this.size = size
    }
  }
})
