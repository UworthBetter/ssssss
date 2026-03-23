<template>
  <section :class="['platform-page-shell', { 'has-aside': hasAside }]">
    <div class="platform-page-card">
      <header class="platform-page-header">
        <div class="platform-page-heading">
          <p v-if="eyebrow" class="platform-page-eyebrow">{{ eyebrow }}</p>
          <h1 class="platform-page-title">{{ title }}</h1>
          <p v-if="subtitle" class="platform-page-subtitle">{{ subtitle }}</p>
        </div>

        <div v-if="$slots.headerExtra" class="platform-page-header-extra">
          <slot name="headerExtra" />
        </div>
      </header>

      <div v-if="$slots.toolbar" class="platform-page-toolbar">
        <slot name="toolbar" />
      </div>

      <div class="platform-page-body">
        <main class="platform-page-main">
          <slot />
        </main>

        <aside v-if="$slots.aside" class="platform-page-aside" :style="asideStyle">
          <div v-if="asideTitle" class="platform-page-aside-title">{{ asideTitle }}</div>
          <div class="aside-inner">
            <slot name="aside" />
          </div>
        </aside>
      </div>
    </div>
  </section>
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
  asideWidth: '320px'
})

const slots = useSlots()

const hasAside = computed(() => Boolean(slots.aside))
const asideStyle = computed(() => ({
  width: props.asideWidth
}))
</script>

<style scoped lang="scss">
.platform-page-shell {
  width: 100%;
  min-height: 100%;
  background-color: #f8fafc;
  padding: 24px;
  box-sizing: border-box;
}

.platform-page-card {
  display: flex;
  flex-direction: column;
  gap: 24px;
  background: #ffffff;
  border-radius: 20px;
  padding: 32px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(226, 232, 240, 0.6);
  min-height: calc(100vh - 88px);
}

.platform-page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid #f1f5f9;
}

.platform-page-heading {
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.platform-page-eyebrow {
  margin: 0 0 8px;
  color: #3b82f6;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.15em;
  text-transform: uppercase;
}

.platform-page-title {
  margin: 0;
  color: #0f172a;
  font-size: 28px;
  line-height: 1.2;
  font-weight: 800;
  letter-spacing: -0.02em;
}

.platform-page-subtitle {
  margin: 10px 0 0;
  color: #64748b;
  font-size: 14px;
  line-height: 1.6;
}

.platform-page-header-extra {
  flex: 0 0 auto;
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.platform-page-toolbar {
  padding: 0;
}

.platform-page-body {
  display: grid;
  grid-template-columns: minmax(0, 1fr);
  gap: 32px;
  align-items: start;
  flex: 1;
}

.platform-page-shell.has-aside .platform-page-body {
  grid-template-columns: minmax(0, 1fr) auto;
}

.platform-page-main {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 24px;
  height: 100%;
}

.platform-page-aside {
  min-width: 0;
  border-radius: 16px;
  border: 1px solid rgba(226, 232, 240, 0.8);
  background: #f8fafc;
  display: flex;
  flex-direction: column;
  box-shadow: inset 0 2px 4px rgba(0,0,0,0.01);
}

.platform-page-aside-title {
  padding: 20px 24px 12px;
  margin: 0;
  color: #0f172a;
  font-size: 16px;
  font-weight: 700;
  letter-spacing: -0.01em;
}

.aside-inner {
  padding: 0 24px 24px;
}

@media (max-width: 1024px) {
  .platform-page-shell { padding: 16px; }
  .platform-page-card { padding: 20px; border-radius: 16px; gap: 20px; }
  .platform-page-header { flex-direction: column; gap: 16px; }
  
  .platform-page-body,
  .platform-page-shell.has-aside .platform-page-body {
    grid-template-columns: minmax(0, 1fr);
  }

  .platform-page-aside {
    width: 100% !important;
  }
}
</style>

