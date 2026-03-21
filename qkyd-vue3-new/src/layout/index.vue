<template>
  <el-container class="layout-container">
    <el-aside :width="sidebarWidth" class="sidebar panel">
      <div class="brand">
        <img class="brand-logo" src="/logo-qkyd-wide.png" alt="耆康云盾" />
        <div v-show="appStore.sidebar.opened" class="brand-text">
          <p class="brand-title">耆康云盾健康监测平台</p>
          <p class="brand-sub">智能监测管理端</p>
        </div>
      </div>
      <el-menu :default-active="activeMenu" :collapse="!appStore.sidebar.opened" router>
        <el-menu-item v-for="item in navItems" :key="item.path" :index="item.path">
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.title }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header panel">
        <div class="header-left">
          <el-button text @click="appStore.toggleSidebar()">
            <el-icon size="18"><Menu /></el-icon>
          </el-button>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item v-for="item in breadcrumbs" :key="item.path">
              {{ item.meta?.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <div class="date-chip">{{ nowText }}</div>
          <el-dropdown @command="onUserCommand">
            <div class="user-entry">
              <el-avatar :size="32" :src="userAvatar">{{ avatarText }}</el-avatar>
              <span>{{ displayName }}</span>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main">
        <transition name="fade-slide" mode="out-in">
          <router-view />
        </transition>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { Menu, DataBoard, UserFilled, Watch, WarningFilled, MagicStick } from '@element-plus/icons-vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore } from '@/store/app'
import { useUserStore } from '@/store/user'

interface NavItem {
  path: string
  title: string
  icon: typeof DataBoard
}

const router = useRouter()
const route = useRoute()
const appStore = useAppStore()
const userStore = useUserStore()
const displayName = '小康'
const userAvatar = '/avatar-qkyd-2.jpg'

const navItems: NavItem[] = [
  { path: '/dashboard', title: '健康总览', icon: DataBoard },
  { path: '/subject', title: '服务对象管理', icon: UserFilled },
  { path: '/device', title: '设备管理', icon: Watch },
  { path: '/exception', title: '异常告警中心', icon: WarningFilled },
  { path: '/ai-workbench', title: 'AI 工作台', icon: MagicStick }
]

const activeMenu = computed(() => route.path)
const breadcrumbs = computed(() => route.matched.filter((item) => item.meta?.title))
const sidebarWidth = computed(() => (appStore.sidebar.opened ? '248px' : '76px'))
const avatarText = computed(() => {
  const source = displayName || userStore.userInfo.nickName || userStore.userInfo.userName || 'U'
  return String(source).slice(0, 1).toUpperCase()
})

const nowText = ref('')
let timer: ReturnType<typeof setInterval> | null = null

const updateNowText = () => {
  nowText.value = new Date().toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const onUserCommand = async (command: string | number | object) => {
  if (command !== 'logout') {
    return
  }
  await userStore.handleLogout()
  await router.replace('/login')
}

onMounted(() => {
  updateNowText()
  timer = setInterval(updateNowText, 30000)
})

onBeforeUnmount(() => {
  if (timer) {
    clearInterval(timer)
  }
})
</script>

<style scoped lang="scss">
.layout-container {
  height: 100vh;
  padding: 8px;
  gap: 10px;
}

.sidebar {
  border-radius: 20px;
  overflow: hidden;
  padding: 12px 10px;
  transition: width 0.25s ease;

  :deep(.el-menu) {
    border-right: none;
    background: transparent;
  }

  :deep(.el-menu-item) {
    border-radius: 12px;
    margin-bottom: 6px;
  }
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 8px 16px;
}

.brand-logo {
  width: 44px;
  height: 44px;
  border-radius: 6px;
  object-fit: contain;
  object-position: center;
  background: rgba(255, 255, 255, 0.95);
  padding: 2px;
  flex: 0 0 auto;
}

.brand-title {
  margin: 0;
  font-size: 14px;
  font-weight: 700;
}

.brand-sub {
  margin: 0;
  font-size: 12px;
  color: var(--text-sub);
}

.header {
  height: 64px;
  border-radius: 18px;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
}

.header-left,
.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.date-chip {
  font-size: 12px;
  color: var(--text-sub);
  padding: 6px 10px;
  border-radius: 10px;
  background: #e7f8f6;
}

.user-entry {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.main {
  padding: 0;
  overflow: auto;
  background: transparent;
}

@media (max-width: 960px) {
  .layout-container {
    padding: 4px;
  }

  .header {
    padding: 0 10px;
  }

  .date-chip {
    display: none;
  }
}
</style>
