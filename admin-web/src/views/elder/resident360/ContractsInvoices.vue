<template>
  <PageContainer title="合同与票据" subTitle="合同/收据/发票/评估报告/身份证明统一归档，支持上传扫描件、版本与权限控制">
    <a-row :gutter="16">
      <a-col :xs="24" :lg="14">
        <a-card title="附件中心" class="card-elevated" :bordered="false">
          <a-space wrap style="margin-bottom: 12px">
            <a-button type="primary">上传合同</a-button>
            <a-button>上传收据/发票</a-button>
            <a-button>手机拍照上传</a-button>
            <a-button>下载打包</a-button>
          </a-space>
          <a-table :columns="columns" :data-source="rows" row-key="id" :pagination="false" />
        </a-card>
      </a-col>

      <a-col :xs="24" :lg="10">
        <a-card title="结构化字段" class="card-elevated" :bordered="false" style="margin-bottom: 16px">
          <a-descriptions :column="1" size="small" bordered>
            <a-descriptions-item label="合同号">HT-2026-0012</a-descriptions-item>
            <a-descriptions-item label="起止日期">2026-01-01 ~ 2026-12-31</a-descriptions-item>
            <a-descriptions-item label="费用项">床位费、护理费、膳食费、增值服务</a-descriptions-item>
            <a-descriptions-item label="押金">6000</a-descriptions-item>
            <a-descriptions-item label="付款周期">月付</a-descriptions-item>
            <a-descriptions-item label="签署状态">已签署</a-descriptions-item>
          </a-descriptions>
        </a-card>

        <a-card title="发票夹联动" class="card-elevated" :bordered="false">
          <a-typography-paragraph>
            从费用管理发票夹选择发票并挂接到当前长者/合同，支持权限分级与版本追溯。
          </a-typography-paragraph>
          <a-space direction="vertical" style="width: 100%">
            <a-button block type="primary" @click="go('/marketing/contract-management')">查看合同详情</a-button>
            <a-button block @click="go('/finance/bill')">前往费用账单</a-button>
            <a-button block @click="go('/elder/admission-processing')">返回入住办理</a-button>
          </a-space>
        </a-card>
      </a-col>
    </a-row>
  </PageContainer>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import PageContainer from '../../../components/PageContainer.vue'

const router = useRouter()

const columns = [
  { title: '类型', dataIndex: 'type', key: 'type', width: 120 },
  { title: '文件名', dataIndex: 'name', key: 'name' },
  { title: '版本', dataIndex: 'version', key: 'version', width: 90 },
  { title: '上传人', dataIndex: 'operator', key: 'operator', width: 120 },
  { title: '上传时间', dataIndex: 'time', key: 'time', width: 180 }
]

const rows = [
  { id: 1, type: '合同', name: '入住合同-v2.pdf', version: 'v2', operator: '前台A', time: '2026-02-24 11:20' },
  { id: 2, type: '发票', name: '2026-02发票.pdf', version: 'v1', operator: '财务B', time: '2026-02-25 09:10' },
  { id: 3, type: '评估报告', name: '入院评估报告.pdf', version: 'v1', operator: '护理C', time: '2026-02-25 15:08' }
]

function go(path: string) {
  router.push(path)
}
</script>
