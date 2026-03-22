import { ElMessage, ElMessageBox } from 'element-plus'
import type { Router } from 'vue-router'
import { searchPlatformEntities, type PlatformSearchResult } from '@/api/platformSearch'
import type {
  PlatformActionDispatchOptions,
  PlatformCenter,
  PlatformEntityKind,
  PlatformEntityRef,
  PlatformNotificationRecord,
  PlatformNotificationTarget,
  PlatformSearchPresentation
} from './platformTypes'

const centerLabelMap: Record<PlatformCenter, string> = {
  workbench: '工作台',
  subject: '对象中心',
  event: '事件中心',
  device: '设备中心',
  ai: 'AI 中心'
}

export const getPlatformSearchPresentation = (center: PlatformCenter): PlatformSearchPresentation => ({
  label: '全局搜索',
  placeholder:
    center === 'device'
      ? '搜索设备名称、IMEI、绑定对象 ID'
      : center === 'event'
        ? '搜索异常类型、用户 ID、设备 ID'
        : center === 'subject'
          ? '搜索对象姓名、手机号、对象账号'
          : '搜索对象、设备、事件、手机号或 IMEI',
  hint: 'Ctrl K'
})

const buildSearchResultMarkup = (results: PlatformSearchResult[]) =>
  results
    .map(
      (item, index) =>
        `<div style="margin-bottom:12px;"><strong>${index + 1}. ${item.title}</strong><br/><span style="color:#667085;">${item.subtitle}</span><br/><span style="color:#98a2b3;">${item.description}</span></div>`
    )
    .join('')

const selectSearchResult = async (results: PlatformSearchResult[]) => {
  if (results.length === 1) return results[0]

  const options = results.slice(0, 5)
  const { value } = await ElMessageBox.prompt(
    `<div style="margin-bottom:8px;">共找到 ${results.length} 条结果，请输入要打开的序号。</div>${buildSearchResultMarkup(options)}`,
    '选择搜索结果',
    {
      dangerouslyUseHTMLString: true,
      confirmButtonText: '打开',
      cancelButtonText: '取消',
      inputPlaceholder: `输入 1 - ${options.length}`,
      inputPattern: new RegExp(`^[1-${options.length}]$`),
      inputErrorMessage: `请输入 1 到 ${options.length} 之间的序号`
    }
  )

  return options[Number(value) - 1]
}

export const openPlatformSearch = async (router: Router, center: PlatformCenter) => {
  try {
    const { value } = await ElMessageBox.prompt(
      '输入对象姓名、设备 IMEI、异常类型、手机号或用户 ID。',
      `平台搜索 · ${centerLabelMap[center]}`,
      {
        confirmButtonText: '搜索',
        cancelButtonText: '取消',
        inputPlaceholder: getPlatformSearchPresentation(center).placeholder
      }
    )

    const keyword = String(value || '').trim()
    if (!keyword) {
      ElMessage.info('请输入搜索关键词')
      return
    }

    const { results } = await searchPlatformEntities(keyword, center)
    if (!results.length) {
      ElMessage.warning(`未找到与“${keyword}”相关的对象、设备或事件`)
      return
    }

    const selected = await selectSearchResult(results)
    if (!selected) return

    await router.push(selected.target)
    ElMessage.success(`已打开${selected.subtitle}`)
  } catch {
    return
  }
}

export const buildPlatformNotifications = (
  center: PlatformCenter,
  context: {
    subjectName?: string
    deviceName?: string
    eventType?: string
  } = {}
): PlatformNotificationRecord[] => {
  if (center === 'ai') {
    return [
      {
        id: 'ai-1',
        title: '2 条 AI 建议待确认',
        description: '建议将高风险结论转入事件中心继续处置。',
        time: '刚刚',
        level: 'danger',
        target: { path: '/event', query: { type: 'AI研判' } }
      },
      {
        id: 'ai-2',
        title: '重点关注对象待复核',
        description: `对象中心已可查看${context.subjectName || '重点关注对象'}。`,
        time: '8m',
        level: 'warning',
        target: { path: '/subject', query: { keyword: context.subjectName || '重点关注对象' } }
      },
      {
        id: 'ai-3',
        title: '设备策略建议待落地',
        description: '建议在设备中心调整采样频率。',
        time: '25m',
        level: 'info',
        target: { path: '/device', query: { name: context.deviceName || '夜间监测设备组' } }
      }
    ]
  }

  if (center === 'event') {
    return [
      {
        id: 'event-1',
        title: '新告警待研判',
        description: `事件中心有新的${context.eventType || '异常事件'}待处理。`,
        time: '2m',
        level: 'danger',
        target: { path: '/event', query: { type: context.eventType || '异常事件' } }
      },
      {
        id: 'event-2',
        title: '关联对象待跟进',
        description: `对象中心可查看${context.subjectName || '当前服务对象'}的最新状态。`,
        time: '14m',
        level: 'warning',
        target: { path: '/subject', query: { keyword: context.subjectName || '' } }
      },
      {
        id: 'event-3',
        title: '关联设备状态已刷新',
        description: '设备中心已同步当前事件关联设备的状态。',
        time: '32m',
        level: 'info',
        target: { path: '/device', query: { name: context.deviceName || '' } }
      }
    ]
  }

  return [
    {
      id: `${center}-1`,
      title: '平台协同提醒',
      description: `${centerLabelMap[center]}已接入统一通知能力。`,
      time: '刚刚',
      level: 'info',
      target: {
        path:
          center === 'device'
            ? '/device'
            : center === 'event'
              ? '/event'
              : center === 'ai'
                ? '/ai/workbench'
                : center === 'workbench'
                  ? '/workbench/overview'
                  : '/subject'
      }
    }
  ]
}

export const openPlatformNotification = async (router: Router, item: PlatformNotificationRecord) => {
  await router.push(item.target)
}

export const openAllPlatformNotifications = async (router: Router, center: PlatformCenter) => {
  const defaultTarget: Record<PlatformCenter, PlatformNotificationTarget> = {
    workbench: { path: '/workbench/overview' },
    subject: { path: '/subject' },
    event: { path: '/event' },
    device: { path: '/device' },
    ai: { path: '/ai/workbench' }
  }

  await router.push(defaultTarget[center])
  ElMessage.info(`已切换到${centerLabelMap[center]}，可继续处理通知相关任务`)
}

export const navigatePlatformEntity = async (router: Router, entity: PlatformEntityRef) => {
  const pathMap: Record<PlatformEntityKind, string> = {
    subject: '/subject',
    event: '/event',
    device: '/device'
  }

  await router.push({
    path: pathMap[entity.kind],
    query: entity.query || { keyword: entity.name }
  })
}

export const dispatchPlatformAction = async (
  router: Router,
  action: string,
  options: PlatformActionDispatchOptions = {}
) => {
  const { entities, onExportCsv, onExportReport, onUnhandled } = options

  if (action === '导出 Excel 名单') {
    onExportCsv?.()
    return true
  }

  if (action === '生成报告并下载') {
    onExportReport?.()
    return true
  }

  if (action === '创建事件' || action === '下发巡检工单' || action === '查看事件') {
    await navigatePlatformEntity(
      router,
      entities?.event || {
        kind: 'event',
        name: action === '下发巡检工单' ? '巡检工单' : 'AI研判',
        query: { type: action === '下发巡检工单' ? '巡检工单' : 'AI研判' }
      }
    )
    return true
  }

  if (action === '加入重点关注' || action === '查看对象') {
    await navigatePlatformEntity(
      router,
      entities?.subject || {
        kind: 'subject',
        name: '重点关注对象',
        query: { keyword: '重点关注对象' }
      }
    )
    return true
  }

  if (action === '调整设备监测频率' || action === '查看设备') {
    await navigatePlatformEntity(
      router,
      entities?.device || {
        kind: 'device',
        name: '夜间监测设备组',
        query: { name: '夜间监测设备组' }
      }
    )
    return true
  }

  if (action === '查看规则') {
    await router.push({ path: '/device', query: { keyword: '规则配置' } })
    ElMessage.info('已跳转到设备中心，规则配置将在后续阶段继续完善')
    return true
  }

  onUnhandled?.(action)
  return false
}
