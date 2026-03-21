import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/store/user'

export const appChildrenRoutes: RouteRecordRaw[] = [
  {
    path: 'dashboard',
    name: 'Dashboard',
    component: () => import('@/views/DashboardView.vue'),
    meta: { title: '健康总览', icon: 'DataBoard' }
  },
  {
    path: 'subject',
    name: 'HealthSubject',
    component: () => import('@/views/HealthSubjectView.vue'),
    meta: { title: '服务对象管理', icon: 'UserFilled' }
  },
  {
    path: 'device',
    name: 'DeviceInfo',
    component: () => import('@/views/DeviceInfoView.vue'),
    meta: { title: '设备管理', icon: 'Watch' }
  },
  {
    path: 'exception',
    name: 'ExceptionAlert',
    component: () => import('@/views/ExceptionAlertView.vue'),
    meta: { title: '异常告警中心', icon: 'WarningFilled' }
  },
  {
    path: 'ai-workbench',
    name: 'AiWorkbench',
    component: () => import('@/views/AiWorkbenchView.vue'),
    meta: { title: 'AI 工作台', icon: 'MagicStick' }
  }
]

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/LoginView.vue')
  },
  {
    path: '/',
    component: () => import('@/layout/index.vue'),
    redirect: '/dashboard',
    children: appChildrenRoutes
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to, _from, next) => {
  const userStore = useUserStore()
  const token = userStore.token

  if (to.path === '/login') {
    if (token) {
      next({ path: '/' })
      return
    }
    next()
    return
  }

  if (!token) {
    next({ path: '/login' })
    return
  }

  try {
    if (!userStore.userInfo?.userId) {
      await userStore.fetchUserInfo()
    }
    next()
  } catch (error) {
    userStore.clearToken()
    next({ path: '/login' })
  }
})

export default router
