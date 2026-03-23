<template>
  <div class="notification-entry panel">
    <div class="notification-head">
      <div class="notification-title">
        <el-icon><Bell /></el-icon>
        <span>{{ title }}</span>
      </div>
      <div class="notification-count">{{ unreadCount }}</div>
    </div>

    <div v-if="previewItems.length" class="notification-body">
      <div
        v-for="item in previewItems"
        :key="item.id"
        class="notification-item"
        @click="$emit('click-item', item)"
      >
        <span class="dot" :class="`dot-${item.level}`"></span>
        <div class="notification-copy">
          <strong>{{ item.title }}</strong>
          <span>{{ item.description }}</span>
        </div>
        <span class="notification-time">{{ item.time }}</span>
      </div>
    </div>

    <div v-else class="notification-empty">
      当前暂无通知，后端数据恢复后会在这里显示最新提醒。
    </div>

    <button class="notification-footer" type="button" @click="$emit('click-all')">
      查看全部通知
    </button>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Bell } from '@element-plus/icons-vue'

type NotificationLevel = 'info' | 'warning' | 'danger'

export interface PlatformNotificationItem {
  id: string | number
  title: string
  description: string
  time: string
  level?: NotificationLevel
}

interface Props {
  title?: string
  unreadCount?: number
  items?: PlatformNotificationItem[]
}

const props = withDefaults(defineProps<Props>(), {
  title: '通知中心',
  unreadCount: 0,
  items: () => []
})

defineEmits<{
  'click-item': [item: PlatformNotificationItem]
  'click-all': []
}>()

const previewItems = computed(() => props.items.slice(0, 3))
</script>

<style scoped lang="scss">
.notification-entry {
  width: 100%;
  padding: 0;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  background: #ffffff;
  border: 1px solid rgba(0, 0, 0, 0.08);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.notification-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  border-bottom: 1px solid #f1f5f9;
}

.notification-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #0f172a;
}

.notification-count {
  min-width: 24px;
  height: 24px;
  border-radius: 12px;
  padding: 0 6px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: #fee2e2;
  color: #ef4444;
  font-size: 12px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
}

.notification-body {
  display: flex;
  flex-direction: column;
}

.notification-item {
  display: grid;
  grid-template-columns: 8px minmax(0, 1fr) auto;
  gap: 12px;
  align-items: start;
  padding: 12px 16px;
  cursor: pointer;
  border-bottom: 1px solid #f8fafc;
  transition: background-color 0.15s ease;

  &:last-child {
    border-bottom: none;
  }

  &:hover {
    background: #f8fafc;
  }
  
  &:focus-visible {
    outline: 2px solid var(--brand, #0ea5e9);
    outline-offset: -2px;
  }
}

.dot {
  width: 6px;
  height: 6px;
  margin-top: 6px;
  border-radius: 50%;
  background: #94a3b8;
}

.dot-info {
  background: #3b82f6;
}

.dot-warning {
  background: #f59e0b;
}

.dot-danger {
  background: #ef4444;
}

.notification-copy {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;

  strong {
    font-size: 13px;
    font-weight: 600;
    color: #1e293b;
  }

  span {
    font-size: 12px;
    color: #64748b;
    line-height: 1.5;
  }
}

.notification-time {
  font-size: 11px;
  font-weight: 500;
  color: #94a3b8;
  white-space: nowrap;
  font-variant-numeric: tabular-nums;
}

.notification-footer {
  border: none;
  border-top: 1px solid #f1f5f9;
  background: transparent;
  color: var(--brand, #0ea5e9);
  font-size: 13px;
  font-weight: 600;
  text-align: center;
  padding: 12px;
  cursor: pointer;
  transition: background-color 0.15s ease;
  border-bottom-left-radius: 12px;
  border-bottom-right-radius: 12px;
  
  &:hover {
    background-color: #f8fafc;
  }

  &:focus-visible {
    outline: 2px solid var(--brand, #0ea5e9);
    outline-offset: -2px;
  }
}

.notification-empty {
  padding: 24px 16px;
  text-align: center;
  font-size: 13px;
  line-height: 1.6;
  color: #94a3b8;
}
</style>
