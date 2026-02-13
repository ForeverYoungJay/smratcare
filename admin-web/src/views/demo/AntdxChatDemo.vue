<template>
  <PageContainer title="Ant Design X" subTitle="智能助手示例">
    <a-card class="card-elevated" :bordered="false">
      <AdxChat />
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import PageContainer from '../../components/PageContainer.vue'
</script>

<style scoped>
.adx-chat {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.adx-messages {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: 300px;
  overflow: auto;
}

.adx-input {
  display: flex;
  gap: 8px;
}

.adx-input input {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}

.adx-input button {
  padding: 8px 12px;
  border: none;
  border-radius: 8px;
  background: #1677ff;
  color: #fff;
}
</style>
