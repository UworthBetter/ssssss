<template>
  <button class="search-entry" type="button" @click="$emit('click')">
    <span class="search-icon-wrap">
      <el-icon><Search /></el-icon>
    </span>
    <span class="search-copy">
      <span class="search-label" v-if="label">{{ label }}</span>
      <span class="search-placeholder">{{ displayText }}</span>
    </span>
    <span class="search-hint">
       <span class="key-cmd">⌘</span> K
    </span>
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
  label: '',
  placeholder: '搜索档案、设备或事件...',
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
  min-width: 320px;
  height: 44px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 16px;
  border: 1px solid rgba(226, 232, 240, 0.8);
  border-radius: 12px;
  background: #ffffff;
  color: #0f172a;
  cursor: pointer;
  text-align: left;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
  outline: none;
  position: relative;

  &:hover {
    background: #f8fafc;
    border-color: #cbd5e1;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
    transform: translateY(-1px);
  }

  &:active {
    transform: translateY(0);
  }

  &:focus-visible {
    border-color: #3b82f6;
    box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.12);
  }
}

.search-icon-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
  flex: 0 0 auto;
  font-size: 18px;
}

.search-copy {
  display: flex;
  flex-direction: column;
  justify-content: center;
  min-width: 0;
  flex: 1 1 auto;
}

.search-label {
  font-size: 11px;
  font-weight: 700;
  color: #3b82f6;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin-bottom: -2px;
}

.search-placeholder {
  font-size: 14px;
  font-weight: 500;
  color: #64748b;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.search-hint {
  flex: 0 0 auto;
  padding: 4px 8px;
  border-radius: 6px;
  background: #f1f5f9;
  border: 1px solid #e2e8f0;
  color: #64748b;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.02em;
  display: flex;
  align-items: center;
  gap: 4px;
  
  .key-cmd {
    font-family: inherit;
    font-size: 12px;
  }
}

@media (max-width: 640px) {
  .search-entry {
    min-width: 200px;
  }
  .search-hint {
    display: none;
  }
}
</style>

