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
  background-color: #f8fafc;
}

.platform-page-card {
  display: flex;
  flex-direction: column;
  gap: 20px;
  min-height: calc(100vh - 80px); /* Clean canvas, more usable space */
  padding: 24px;
}

.platform-page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding-bottom: 20px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}

.platform-page-heading {
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.platform-page-eyebrow {
  margin: 0 0 4px;
  color: var(--brand);
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  opacity: 0.8;
}

.platform-page-title {
  margin: 0;
  color: #0f172a;
  font-size: 24px;
  line-height: 1.25;
  font-weight: 600; /* Crisper, semibold vs black weight */
  letter-spacing: -0.01em;
}

.platform-page-subtitle {
  margin: 6px 0 0;
  color: #64748b;
  font-size: 14px;
  line-height: 1.5;
}

.platform-page-header-extra {
  flex: 0 0 auto;
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.platform-page-toolbar {
  padding: 12px 0;
  margin-bottom: 4px; /* Flatter approach, integrated rather than boxed */
}

.platform-page-body {
  display: grid;
  grid-template-columns: minmax(0, 1fr);
  gap: 24px;
  align-items: start;
}

.platform-page-shell.has-aside .platform-page-body {
  grid-template-columns: minmax(0, 1fr) auto;
}

.platform-page-main {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.platform-page-aside {
  min-width: 0;
  padding: 20px;
  border-radius: 12px;
  border: 1px solid rgba(0, 0, 0, 0.08);
  background: #ffffff;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.platform-page-aside-title {
  margin: 0 0 16px;
  color: #0f172a;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: -0.01em;
}

@media (max-width: 1024px) {
  .platform-page-card {
    min-height: auto;
    padding: 16px;
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
