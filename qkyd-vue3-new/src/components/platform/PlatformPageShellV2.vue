<template>
  <div class="platform-page-shell-v2" :class="{ 'has-aside': hasAside }">
    <header class="page-topbar">
      <div class="topbar-main">
        <div class="page-heading">
          <p v-if="eyebrow" class="page-eyebrow">{{ eyebrow }}</p>
          <h1 class="page-title">{{ title }}</h1>
          <p v-if="subtitle" class="page-subtitle">{{ subtitle }}</p>
          <el-tag
            v-if="statusNote"
            class="page-status-note"
            :type="statusTone"
            effect="light"
            round
          >
            {{ statusNote }}
          </el-tag>
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
        <div class="page-content-wrapper">
          <slot />
        </div>
      </main>

      <aside v-if="$slots.aside" class="page-aside" :style="asideStyle">
        <div v-if="asideTitle" class="aside-title">{{ asideTitle }}</div>
        <div class="aside-content">
          <slot name="aside" />
        </div>
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
  statusNote?: string
  statusTone?: '' | 'success' | 'warning' | 'danger' | 'info'
  asideTitle?: string
  asideWidth?: string
}

const props = withDefaults(defineProps<Props>(), {
  subtitle: '',
  eyebrow: '',
  statusNote: '',
  statusTone: 'info',
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
  height: 100%;
  min-height: 0;
  background: #f8fafc;
  box-sizing: border-box;
}

.page-topbar {
  flex: 0 0 auto;
  display: flex;
  flex-direction: column;
  background: #ffffff;
  border-bottom: 1px solid rgba(226, 232, 240, 0.8);
  z-index: 10;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.02);
}

.topbar-main {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  padding: 24px 32px;
  gap: 24px;
}

.page-heading {
  display: flex;
  flex-direction: column;
  justify-content: center;
  min-width: 0;
}

.page-eyebrow {
  margin: 0 0 8px;
  color: #3b82f6;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.15em;
  text-transform: uppercase;
  line-height: 1;
}

.page-title {
  margin: 0;
  color: #0f172a;
  font-size: 28px;
  font-weight: 800;
  line-height: 1.1;
  letter-spacing: -0.02em;
}

.page-subtitle {
  margin: 10px 0 0;
  color: #64748b;
  font-size: 14px;
  line-height: 1.6;
  max-width: 800px;
}

.page-status-note {
  margin-top: 12px;
  width: fit-content;
}

.topbar-actions {
  flex: 0 0 auto;
  display: flex;
  align-items: center;
  gap: 16px;
  padding-top: 4px;
}

.page-toolbar {
  padding: 0 32px 20px;
}

.page-body {
  flex: 1 1 0;
  display: flex;
  min-height: 0;
  position: relative;
  background: #f8fafc;
}

.page-main {
  flex: 1 1 0;
  min-width: 0;
  height: 100%;
  overflow-y: auto;
  overflow-x: hidden;
  display: flex;
  flex-direction: column;
}

.page-content-wrapper {
  padding: 24px 32px 40px;
  width: 100%;
  max-width: 1600px; /* Optional: cap width for large screens */
  margin: 0 auto;
  box-sizing: border-box;
}

.page-aside {
  flex: 0 0 auto;
  height: 100%;
  overflow-y: auto;
  background-color: #ffffff;
  border-left: 1px solid rgba(226, 232, 240, 0.8);
  display: flex;
  flex-direction: column;
  box-shadow: -4px 0 12px rgba(0, 0, 0, 0.03);
}

.aside-title {
  padding: 24px 24px 16px;
  margin: 0;
  color: #0f172a;
  font-size: 16px;
  font-weight: 700;
  border-bottom: 1px solid #f1f5f9;
  flex: 0 0 auto;
}

.aside-content {
  flex: 1;
  padding: 20px 24px;
}

@media (max-width: 1200px) {
  .topbar-main { padding: 20px 24px; }
  .page-toolbar { padding: 0 24px 16px; }
  .page-content-wrapper { padding: 20px 24px 32px; }
}
</style>
