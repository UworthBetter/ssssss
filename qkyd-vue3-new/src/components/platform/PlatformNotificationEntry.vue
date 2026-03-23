<template>
  <div class="notification-entry">
    <div class="notification-head">
      <div class="notification-title">
        <div class="icon-box">
          <el-icon><Bell /></el-icon>
        </div>
        <span>{{ title }}</span>
      </div>
      <div class="notification-count" v-if="unreadCount > 0">{{ unreadCount }}</div>
    </div>

    <div v-if="previewItems.length" class="notification-body">
      <div
        v-for="item in previewItems"
        :key="item.id"
        class="notification-item"
        @click="$emit('click-item', item)"
      >
        <div class="item-status">
          <span class="dot" :class="`dot-${item.level}`"></span>
        </div>
        <div class="notification-copy">
          <div class="copy-header">
            <strong class="item-title">{{ item.title }}</strong>
            <span class="notification-time">{{ item.time }}</span>
          </div>
          <span class="item-desc">{{ item.description }}</span>
        </div>
      </div>
    </div>

    <div v-else class="notification-empty">
      <el-icon class="empty-icon"><InfoFilled /></el-icon>
      <p>暂无新通知</p>
      <span>当系统有最新动态时会在此提醒</span>
    </div>

    <button class="notification-footer" type="button" @click="$emit('click-all')">
      进入通知中心
      <el-icon class="ml-1"><ArrowRight /></el-icon>
    </button>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Bell, InfoFilled, ArrowRight } from '@element-plus/icons-vue'

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
  display: flex;
  flex-direction: column;
  background: #ffffff;
  border: 1px solid rgba(226, 232, 240, 0.8);
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03);
  overflow: hidden;
}

.notification-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 18px 20px;
  border-bottom: 1px solid #f1f5f9;
}

.notification-title {
  display: flex;
  align-items: center;
  gap: 12px;
  
  .icon-box {
    width: 32px;
    height: 32px;
    background: #eff6ff;
    color: #3b82f6;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 16px;
  }
  
  span {
    font-size: 15px;
    font-weight: 700;
    color: #0f172a;
    letter-spacing: -0.01em;
  }
}

.notification-count {
  height: 20px;
  padding: 0 8px;
  border-radius: 10px;
  background: #ef4444;
  color: #fff;
  font-size: 11px;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 4px rgba(239, 68, 68, 0.2);
}

.notification-body {
  display: flex;
  flex-direction: column;
}

.notification-item {
  display: flex;
  gap: 14px;
  padding: 16px 20px;
  cursor: pointer;
  border-bottom: 1px solid #f8fafc;
  transition: all 0.2s ease;

  &:last-child {
    border-bottom: none;
  }

  &:hover {
    background: #f8fafc;
    .item-title { color: #3b82f6; }
  }
}

.item-status {
  padding-top: 6px;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  display: block;
  background: #94a3b8;
  box-shadow: 0 0 0 2px #fff;
}

.dot-info { background: #3b82f6; box-shadow: 0 0 0 2px #eff6ff, 0 0 8px rgba(59, 130, 246, 0.4); }
.dot-warning { background: #f59e0b; box-shadow: 0 0 0 2px #fffbeb, 0 0 8px rgba(245, 158, 11, 0.4); }
.dot-danger { background: #ef4444; box-shadow: 0 0 0 2px #fef2f2, 0 0 8px rgba(239, 68, 68, 0.4); }

.notification-copy {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.copy-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.item-title {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
  transition: color 0.2s;
}

.notification-time {
  font-size: 11px;
  font-weight: 600;
  color: #94a3b8;
  white-space: nowrap;
  letter-spacing: 0.02em;
}

.item-desc {
  font-size: 13px;
  color: #64748b;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.notification-footer {
  border: none;
  border-top: 1px solid #f1f5f9;
  background: #ffffff;
  color: #3b82f6;
  font-size: 13px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 14px;
  cursor: pointer;
  transition: all 0.2s ease;
  
  &:hover {
    background-color: #f8fafc;
    color: #2563eb;
    .ml-1 { transform: translateX(2px); transition: transform 0.2s; }
  }
}

.notification-empty {
  padding: 48px 24px;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  
  .empty-icon {
    font-size: 32px;
    color: #e2e8f0;
    margin-bottom: 12px;
  }
  
  p {
    margin: 0;
    font-size: 14px;
    font-weight: 600;
    color: #64748b;
  }
  
  span {
    font-size: 12px;
    color: #94a3b8;
    margin-top: 4px;
  }
}

.ml-1 { margin-left: 4px; }
</style>

