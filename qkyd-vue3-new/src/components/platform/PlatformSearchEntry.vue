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
  min-height: 60px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  border: 1px solid rgba(219, 228, 235, 0.95);
  border-radius: 18px;
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.92), rgba(244, 248, 250, 0.78));
  box-shadow: var(--shadow-soft);
  color: var(--text-main);
  cursor: pointer;
  text-align: left;
  transition:
    transform 0.2s ease,
    box-shadow 0.2s ease,
    border-color 0.2s ease;

  &:hover {
    transform: translateY(-1px);
    border-color: rgba(111, 155, 160, 0.35);
    box-shadow: 0 16px 34px rgba(65, 86, 102, 0.12);
  }
}

.search-icon-wrap {
  width: 34px;
  height: 34px;
  border-radius: 12px;
  display: grid;
  place-items: center;
  background: var(--brand-soft);
  color: var(--brand);
  flex: 0 0 auto;
}

.search-copy {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
  flex: 1 1 auto;
}

.search-label {
  font-size: 12px;
  font-weight: 700;
  color: var(--text-sub);
  letter-spacing: 0.04em;
  text-transform: uppercase;
}

.search-placeholder {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-main);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.search-hint {
  flex: 0 0 auto;
  padding: 5px 9px;
  border-radius: 999px;
  background: rgba(111, 155, 160, 0.12);
  color: var(--brand);
  font-size: 12px;
  font-weight: 700;
}
</style>
