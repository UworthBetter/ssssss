import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/store/user'

export interface AppRouteMeta {
  title: string
  icon: string
  group: 'workbench' | 'subject' | 'event' | 'device' | 'ai' | 'report'
  groupTitle: string
  groupIcon: string
  navKey: string
}

export const appChildrenRoutes: RouteRecordRaw[] = [
  // ── 工作台 ──
  {
    path: 'workbench/overview',
    name: 'WorkbenchOverview',
    component: () => import('@/views/DashboardView.vue'),
    meta: {
      title: '平台总览',
      icon: 'DataBoard',
      group: 'workbench',
      groupTitle: '工作台',
      groupIcon: 'DataBoard',
      navKey: '/workbench/overview'
    } satisfies AppRouteMeta
  },
  {
    path: 'workbench/example',
    name: 'DashboardExample',
    component: () => import('@/views/DashboardExampleView.vue'),
    meta: {
      title: '处理链总览',
      icon: 'Promotion',
      group: 'workbench',
      groupTitle: '工作台',
      groupIcon: 'DataBoard',
      navKey: '/workbench/example'
    } satisfies AppRouteMeta
  },
  // ── 对象中心 ──
  {
    path: 'subject',
    name: 'SubjectCenter',
    component: () => import('@/views/HealthSubjectView.vue'),
    meta: {
      title: '对象列表',
      icon: 'UserFilled',
      group: 'subject',
      groupTitle: '对象中心',
      groupIcon: 'UserFilled',
      navKey: '/subject'
    } satisfies AppRouteMeta
  },
  {
    path: 'subject/health-records',
    name: 'HealthRecords',
    component: () => import('@/views/subject/HealthRecordsView.vue'),
    meta: {
      title: '健康档案',
      icon: 'Document',
      group: 'subject',
      groupTitle: '对象中心',
      groupIcon: 'UserFilled',
      navKey: '/subject/health-records'
    } satisfies AppRouteMeta
  },
  {
    path: 'subject/risk-profile',
    name: 'RiskProfile',
    component: () => import('@/views/subject/RiskProfileView.vue'),
    meta: {
      title: '风险画像',
      icon: 'PieChart',
      group: 'subject',
      groupTitle: '对象中心',
      groupIcon: 'UserFilled',
      navKey: '/subject/risk-profile'
    } satisfies AppRouteMeta
  },
  // ── 事件中心 ──
  {
    path: 'event',
    name: 'EventCenter',
    component: () => import('@/views/ExceptionAlertView.vue'),
    meta: {
      title: '事件列表',
      icon: 'WarningFilled',
      group: 'event',
      groupTitle: '事件中心',
      groupIcon: 'WarningFilled',
      navKey: '/event'
    } satisfies AppRouteMeta
  },
  {
    path: 'event/processing-chain',
    name: 'ProcessingChain',
    component: () => import('@/views/ProcessingChainView.vue'),
    meta: {
      title: '处理链追踪',
      icon: 'DataAnalysis',
      group: 'event',
      groupTitle: '事件中心',
      groupIcon: 'WarningFilled',
      navKey: '/event/processing-chain'
    } satisfies AppRouteMeta
  },
  // ── 设备中心 ──
  {
    path: 'device',
    name: 'DeviceCenter',
    component: () => import('@/views/DeviceInfoView.vue'),
    meta: {
      title: '设备列表',
      icon: 'Watch',
      group: 'device',
      groupTitle: '设备中心',
      groupIcon: 'Watch',
      navKey: '/device'
    } satisfies AppRouteMeta
  },
  {
    path: 'device/monitor',
    name: 'DeviceMonitor',
    component: () => import('@/views/device/DeviceMonitorView.vue'),
    meta: {
      title: '状态监控',
      icon: 'Monitor',
      group: 'device',
      groupTitle: '设备中心',
      groupIcon: 'Watch',
      navKey: '/device/monitor'
    } satisfies AppRouteMeta
  },
  {
    path: 'device/maintenance',
    name: 'DeviceMaintenance',
    component: () => import('@/views/device/DeviceMaintenanceView.vue'),
    meta: {
      title: '维护记录',
      icon: 'Tools',
      group: 'device',
      groupTitle: '设备中心',
      groupIcon: 'Watch',
      navKey: '/device/maintenance'
    } satisfies AppRouteMeta
  },
  // ── AI 中心 ──
  {
    path: 'ai/workbench',
    name: 'AiCenter',
    component: () => import('@/views/AiWorkbenchView.vue'),
    meta: {
      title: '分析工作台',
      icon: 'MagicStick',
      group: 'ai',
      groupTitle: 'AI 中心',
      groupIcon: 'MagicStick',
      navKey: '/ai/workbench'
    } satisfies AppRouteMeta
  },
  {
    path: 'ai/logs',
    name: 'AiLogs',
    component: () => import('@/views/ai/AiLogsView.vue'),
    meta: {
      title: '模型日志',
      icon: 'Memo',
      group: 'ai',
      groupTitle: 'AI 中心',
      groupIcon: 'MagicStick',
      navKey: '/ai/logs'
    } satisfies AppRouteMeta
  },
  {
    path: 'ai/rules',
    name: 'AiRules',
    component: () => import('@/views/ai/AiRulesView.vue'),
    meta: {
      title: '规则配置',
      icon: 'Setting',
      group: 'ai',
      groupTitle: 'AI 中心',
      groupIcon: 'MagicStick',
      navKey: '/ai/rules'
    } satisfies AppRouteMeta
  },
  // ── 报表中心 ──
  {
    path: 'report/operation',
    name: 'OperationReport',
    component: () => import('@/views/report/OperationReportView.vue'),
    meta: {
      title: '运营报表',
      icon: 'TrendCharts',
      group: 'report',
      groupTitle: '报表中心',
      groupIcon: 'Histogram',
      navKey: '/report/operation'
    } satisfies AppRouteMeta
  },
  {
    path: 'report/health',
    name: 'HealthReport',
    component: () => import('@/views/report/HealthReportView.vue'),
    meta: {
      title: '健康报告',
      icon: 'Notebook',
      group: 'report',
      groupTitle: '报表中心',
      groupIcon: 'Histogram',
      navKey: '/report/health'
    } satisfies AppRouteMeta
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
    redirect: '/workbench/overview',
    children: appChildrenRoutes
  },
  {
    path: '/dashboard',
    redirect: '/workbench/overview'
  },
  {
    path: '/exception',
    redirect: '/event'
  },
  {
    path: '/ai-workbench',
    redirect: '/ai/workbench'
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
