<template>
  <PageContainer title="富文本编辑" subTitle="WangEditor 示例">
    <a-card class="card-elevated" :bordered="false">
      <RichTextEditor v-model="content" />
      <a-divider />
      <div v-html="content" />
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import PageContainer from '../../components/PageContainer.vue'

const content = ref('<p>欢迎使用富文本编辑器</p>')
</script>
