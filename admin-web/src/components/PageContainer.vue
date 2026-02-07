<template>
  <div class="page-container">
    <a-page-header :title="title" :sub-title="subTitle">
      <template #extra>
        <slot name="extra" />
      </template>
    </a-page-header>
    <slot />
  </div>
</template>

<script setup lang="ts">
defineProps<{ title: string; subTitle?: string }>()
</script>
