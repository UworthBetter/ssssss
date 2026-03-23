<template>
  <button class="search-entry panel" type="button" @click="$emit('click')">
    <span class="search-icon-wrap">
      <el-icon><Search /></el-icon>
    </span>
    <span class="search-copy">
      <span class="search-label">{{ label }}</span>
      <span class="search-placeholder">{{ displayText }}</span>
    </span>
    <span class="search-hint">{{ hint }}</span>
  </button>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Search } from '@element-plus/icons-vue'

interface Props {
  label?: string
  placeholder?: string
  hint?: string
}

const props = withDefaults(defineProps<Props>(), {
  label: '全局搜索',
  placeholder: '搜索对象、设备、事件、手机号或 IMEI',
  hint: 'Ctrl K'
})

defineEmits<{
  click: []
}>()

const displayText = computed(() => props.placeholder)
</script>

<style scoped lang="scss">
.search-entry {
  width: 100%;
  min-width: 280px;
  min-height: 44px; /* Touch friendly >= 44px */
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 12px;
  border: 1px solid rgba(0, 0, 0, 0.12);
  border-radius: 8px; /* Sharper corners */
  background: #ffffff;
  color: #334155;
  cursor: pointer;
  text-align: left;
  transition: all 0.15s ease;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.02);
  outline: none;

  &:hover {
    border-color: rgba(0, 0, 0, 0.2);
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.04);
  }

  &:focus-visible {
    border-color: var(--brand, #0ea5e9);
    box-shadow: 0 0 0 3px rgba(14, 165, 233, 0.2);
  }
}

.search-icon-wrap {
  display: grid;
  place-items: center;
  color: #94a3b8;
  flex: 0 0 auto;
  font-size: 16px;
}

.search-copy {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
  flex: 1 1 auto;
}

.search-label {
  font-size: 13px;
  font-weight: 500;
  color: #64748b;
  display: none; /* Hide label in normal command bar, only show placeholder */
}

.search-placeholder {
  font-size: 14px;
  font-weight: 400;
  color: #64748b;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.search-hint {
  flex: 0 0 auto;
  padding: 4px 6px;
  border-radius: 4px;
  background: #f1f5f9;
  border: 1px solid #e2e8f0;
  color: #64748b;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.05em;
}
</style>
