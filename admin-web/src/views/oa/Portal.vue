<template>
  <PageContainer title="OA 门户" subTitle="公告与待办一览">
    <a-row :gutter="16">
      <a-col :span="14">
        <a-card title="最新公告" :bordered="false" class="card-elevated">
          <a-list :data-source="summary.notices" :render-item="renderNotice" />
        </a-card>
      </a-col>
      <a-col :span="10">
        <a-card title="我的待办" :bordered="false" class="card-elevated">
          <a-list :data-source="summary.todos" :render-item="renderTodo" />
        </a-card>
      </a-col>
    </a-row>
  </PageContainer>
</template>

<script setup lang="ts">
import { reactive, h } from 'vue'
import PageContainer from '../../components/PageContainer.vue'
import { getPortalSummary } from '../../api/oa'
import type { OaPortalSummary, OaNotice, OaTodo } from '../../types'

const summary = reactive<OaPortalSummary>({ notices: [], todos: [] })

function renderNotice(item: OaNotice) {
  return h('div', { class: 'list-item' }, [
    h('div', { class: 'title' }, item.title),
    h('div', { class: 'meta' }, `${item.publisherName || ''} ${item.publishTime || ''}`)
  ])
}

function renderTodo(item: OaTodo) {
  return h('div', { class: 'list-item' }, [
    h('div', { class: 'title' }, item.title),
    h('div', { class: 'meta' }, item.dueTime || '')
  ])
}

async function load() {
  const res = await getPortalSummary()
  summary.notices = res.notices || []
  summary.todos = res.todos || []
}

load()
</script>

<style scoped>
.list-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 8px 0;
}
.title {
  font-weight: 600;
}
.meta {
  color: #888;
  font-size: 12px;
}
</style>
