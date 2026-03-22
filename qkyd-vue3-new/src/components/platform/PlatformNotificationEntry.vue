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
  padding: 14px;
  border-radius: 18px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.92), rgba(247, 249, 251, 0.82));
}

.notification-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.notification-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 700;
  color: var(--text-main);
}

.notification-count {
  min-width: 28px;
  height: 28px;
  border-radius: 999px;
  padding: 0 8px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: rgba(197, 127, 134, 0.14);
  color: var(--danger);
  font-size: 12px;
  font-weight: 800;
}

.notification-body {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.notification-item {
  display: grid;
  grid-template-columns: 10px minmax(0, 1fr) auto;
  gap: 10px;
  align-items: start;
  padding: 10px 10px 10px 8px;
  border-radius: 14px;
  cursor: pointer;
  transition: background-color 0.2s ease, transform 0.2s ease;

  &:hover {
    background: rgba(111, 155, 160, 0.08);
    transform: translateY(-1px);
  }
}

.dot {
  width: 8px;
  height: 8px;
  margin-top: 5px;
  border-radius: 999px;
  background: var(--text-sub);
}

.dot-info {
  background: var(--brand);
}

.dot-warning {
  background: var(--warn);
}

.dot-danger {
  background: var(--danger);
}

.notification-copy {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;

  strong {
    font-size: 13px;
    color: var(--text-main);
  }

  span {
    font-size: 12px;
    color: var(--text-sub);
    line-height: 1.4;
  }
}

.notification-time {
  font-size: 12px;
  color: var(--text-sub);
  white-space: nowrap;
}

.notification-footer {
  border: none;
  background: transparent;
  color: var(--brand);
  font-size: 13px;
  font-weight: 700;
  text-align: left;
  padding: 2px 4px 0;
  cursor: pointer;
}

.notification-empty {
  padding: 12px;
  border-radius: 14px;
  border: 1px dashed rgba(221, 227, 233, 0.88);
  background: rgba(255, 255, 255, 0.56);
  font-size: 12px;
  line-height: 1.6;
  color: var(--text-sub);
}
</style>
