<template>
  <PageContainer title="流程设计" subTitle="LogicFlow 示例">
    <a-space style="margin-bottom: 12px;">
      <a-button type="primary" @click="addNode">新增节点</a-button>
      <a-button @click="clear">清空</a-button>
    </a-space>
    <a-card class="card-elevated" :bordered="false">
      <FlowDesigner v-model:data="graph" />
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import PageContainer from '../../components/PageContainer.vue'

const graph = ref({
  nodes: [
    { id: '1', type: 'rect', x: 120, y: 120, text: '开始' }
  ],
  edges: [] as any[]
})

function addNode() {
  const id = String(Date.now())
  graph.value.nodes.push({ id, type: 'rect', x: 120 + graph.value.nodes.length * 40, y: 200, text: '节点' })
}

function clear() {
  graph.value.nodes.splice(0, graph.value.nodes.length)
  graph.value.edges.splice(0, graph.value.edges.length)
}
</script>
