<template>
  <div class="page-container page-shell">
    <section class="page-head card-elevated">
      <div class="page-head-main">
        <div class="page-title-wrap">
          <div class="page-kicker">智慧养老管理平台</div>
          <h1 class="page-title">{{ title }}</h1>
          <p class="page-subtitle" v-if="subTitle || subTitleAlias">{{ subTitle || subTitleAlias }}</p>
        </div>
        <div class="page-head-extra">
          <slot name="extra" />
        </div>
      </div>
      <div class="page-head-stats" v-if="$slots.stats">
        <slot name="stats" />
      </div>
    </section>
    <section class="page-body">
      <slot />
    </section>
  </div>
</template>

<script setup lang="ts">
const props = defineProps<{ title: string; subTitle?: string; subTitleAlias?: string }>()
const subTitleAlias = props.subTitleAlias
</script>

<style scoped>
.page-shell {
  gap: 18px;
}

.page-head {
  position: relative;
  overflow: hidden;
  padding: 22px 24px;
}

.page-head::before {
  content: '';
  position: absolute;
  inset: auto auto -80px -60px;
  width: 240px;
  height: 240px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(87, 215, 255, 0.14) 0%, rgba(87, 215, 255, 0) 70%);
  pointer-events: none;
}

.page-head::after {
  content: '';
  position: absolute;
  top: -80px;
  right: -50px;
  width: 240px;
  height: 240px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(121, 177, 255, 0.16) 0%, rgba(121, 177, 255, 0) 70%);
  pointer-events: none;
}

.page-head-main {
  position: relative;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.page-title-wrap {
  min-width: 0;
  display: grid;
  gap: 6px;
}

.page-kicker {
  color: #6d8aa3;
  font-size: 11px;
  text-transform: uppercase;
  letter-spacing: 0.18em;
  font-weight: 700;
}

.page-title {
  margin: 0;
  font-size: 28px;
  line-height: 1.15;
  letter-spacing: 0.02em;
  color: #173854;
}

.page-subtitle {
  margin: 0;
  max-width: 760px;
  font-size: 13px;
  color: #6d8aa3;
  line-height: 1.7;
}

.page-head-extra {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  flex-wrap: wrap;
  gap: 8px;
}

.page-head-stats {
  position: relative;
  margin-top: 18px;
}

.page-body {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

@media (max-width: 992px) {
  .page-head {
    padding: 18px;
  }

  .page-head-main {
    flex-direction: column;
  }

  .page-head-extra {
    justify-content: flex-start;
    width: 100%;
  }

  .page-title {
    font-size: 24px;
  }
}
</style>
