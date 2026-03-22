<template>
  <section :class="['platform-page-shell', { 'has-aside': hasAside }]">
    <div class="platform-page-card panel">
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
          <slot name="aside" />
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
}

.platform-page-card {
  display: flex;
  flex-direction: column;
  gap: 18px;
  min-height: calc(100vh - 100px);
  padding: 22px;
}

.platform-page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.platform-page-heading {
  min-width: 0;
}

.platform-page-eyebrow {
  margin: 0 0 8px;
  color: var(--brand);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.16em;
  text-transform: uppercase;
}

.platform-page-title {
  margin: 0;
  color: var(--text-main);
  font-size: 22px;
  line-height: 1.2;
  font-weight: 800;
}

.platform-page-subtitle {
  margin: 8px 0 0;
  color: var(--text-sub);
  font-size: 13px;
  line-height: 1.6;
}

.platform-page-header-extra {
  flex: 0 0 auto;
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.platform-page-toolbar {
  padding: 14px 16px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.52);
  border: 1px solid rgba(217, 225, 232, 0.88);
}

.platform-page-body {
  display: grid;
  grid-template-columns: minmax(0, 1fr);
  gap: 18px;
  align-items: start;
}

.platform-page-shell.has-aside .platform-page-body {
  grid-template-columns: minmax(0, 1fr) auto;
}

.platform-page-main {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.platform-page-aside {
  min-width: 0;
  padding: 16px;
  border-radius: 18px;
  border: 1px solid rgba(217, 225, 232, 0.88);
  background: rgba(255, 255, 255, 0.54);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  box-shadow: var(--shadow-soft);
}

.platform-page-aside-title {
  margin: 0 0 12px;
  color: var(--text-main);
  font-size: 14px;
  font-weight: 700;
}

@media (max-width: 1024px) {
  .platform-page-card {
    min-height: auto;
    padding: 18px;
  }

  .platform-page-header {
    flex-direction: column;
  }

  .platform-page-body,
  .platform-page-shell.has-aside .platform-page-body {
    grid-template-columns: minmax(0, 1fr);
  }

  .platform-page-aside {
    width: 100% !important;
  }
}
</style>
