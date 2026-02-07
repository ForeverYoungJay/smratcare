<template>
  <div class="app">
    <header class="header">
      <div class="brand">Ant Design X Demo</div>
    </header>

    <main class="main">
      <div class="chat-card">
        <div class="chat-list">
          <a-empty v-if="messages.length === 0" description="暂无消息" />
          <div v-else class="bubble-list">
            <Bubble
              v-for="msg in messages"
              :key="msg.id"
              :content="msg.content"
              :placement="msg.role === 'user' ? 'end' : 'start'"
              :variant="msg.role === 'user' ? 'filled' : 'outlined'"
              :type="msg.role === 'user' ? 'primary' : 'default'"
              class="bubble-item"
            />
          </div>
        </div>

        <div class="composer">
          <a-input
            v-model:value="draft"
            placeholder="输入消息..."
            @keyup.enter="send"
          />
          <a-button type="primary" :disabled="!draft.trim()" @click="send">
            发送
          </a-button>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { Bubble } from 'ant-design-x-vue';

interface ChatMessage {
  id: number;
  role: 'user' | 'assistant';
  content: string;
}

const draft = ref('');
const messages = ref<ChatMessage[]>([]);
let seq = 1;

const send = () => {
  const text = draft.value.trim();
  if (!text) return;

  messages.value.push({
    id: seq++,
    role: 'user',
    content: text
  });

  draft.value = '';

  setTimeout(() => {
    messages.value.push({
      id: seq++,
      role: 'assistant',
      content: `收到：${text}`
    });
  }, 1000);
};
</script>
