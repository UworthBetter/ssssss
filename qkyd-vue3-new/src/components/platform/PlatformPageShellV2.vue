<template>
  <div class="platform-page-shell-v2" :class="{ 'has-aside': hasAside }">
    <header class="page-topbar">
      <div class="topbar-main">
        <div class="page-heading">
          <p v-if="eyebrow" class="page-eyebrow">{{ eyebrow }}</p>
          <h1 class="page-title">{{ title }}</h1>
          <p v-if="subtitle" class="page-subtitle">{{ subtitle }}</p>
        </div>
        <div class="topbar-actions" v-if="$slots.headerExtra">
          <slot name="headerExtra" />
        </div>
      </div>
      <div class="page-toolbar" v-if="$slots.toolbar">
        <slot name="toolbar" />
      </div>
    </header>

    <div class="page-body">
      <main class="page-main">
        <slot />
      </main>

      <aside v-if="$slots.aside" class="page-aside" :style="asideStyle">
        <div v-if="asideTitle" class="aside-title">{{ asideTitle }}</div>
        <slot name="aside" />
      </aside>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, useSlots } from 'vue'

interface Props {
  title: string
  subtitle?: string
  eyebrow?: string
  asideTitle?: string
  asideWidth?: string
}

const props = withDefaults(defineProps<Props>(), {
  subtitle: '',
  eyebrow: '',
  asideTitle: '',
  asideWidth: '360px'
})

const slots = useSlots()

const hasAside = computed(() => Boolean(slots.aside))
const asideStyle = computed(() => ({
  width: props.asideWidth
}))
</script>

<style scoped lang="scss">
.platform-page-shell-v2 {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%; /* Important: needs to be 100% of parent in App layout */
  min-height: 0;
  background: var(--el-bg-color);
  box-sizing: border-box;
}

.page-topbar {
  flex: 0 0 auto;
  display: flex;
  flex-direction: column;
  background: var(--el-bg-color);
  border-bottom: 1px solid var(--el-border-color-lighter);
  z-index: 10;
}

.topbar-main {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  padding: 16px 24px;
  gap: 16px;
}

.page-heading {
  display: flex;
  flex-direction: column;
  justify-content: center;
  min-width: 0;
}

.page-eyebrow {
  margin: 0 0 6px;
  color: var(--el-color-primary);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.page-title {
  margin: 0;
  color: var(--el-text-color-primary);
  font-size: 20px;
  font-weight: 700;
  line-height: 1.2;
}

.page-subtitle {
  margin: 6px 0 0;
  color: var(--el-text-color-secondary);
  font-size: 13px;
  line-height: 1.5;
}

.topbar-actions {
  flex: 0 0 auto;
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-toolbar {
  padding: 0 24px 16px;
}

.page-body {
  flex: 1 1 0;
  display: flex;
  min-height: 0;
  position: relative;
  background: var(--el-bg-color-page);
}

.page-main {
  flex: 1 1 0;
  min-width: 0;
  height: 100%;
  overflow-y: auto;
  overflow-x: hidden;
}

.page-aside {
  flex: 0 0 auto;
  height: 100%;
  overflow-y: auto;
  background-color: var(--el-bg-color);
  border-left: 1px solid var(--el-border-color-lighter);
  display: flex;
  flex-direction: column;
  box-shadow: -2px 0 8px rgba(0, 0, 0, 0.02);
}

.aside-title {
  padding: 16px 24px 0;
  margin: 0;
  color: var(--el-text-color-primary);
  font-size: 15px;
  font-weight: 600;
  flex: 0 0 auto;
}
</style>
