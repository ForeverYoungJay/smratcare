<template>
  <PageContainer title="合同与票据" subTitle="合同/收据/发票/评估报告/身份证明统一归档，支持上传扫描件、版本与权限控制">
    <a-row :gutter="16">
      <a-col :xs="24" :lg="14">
        <a-card title="附件中心" class="card-elevated" :bordered="false">
          <a-space wrap style="margin-bottom: 12px">
            <a-button type="primary" @click="openFilePicker('contract')">上传合同</a-button>
            <a-button @click="openFilePicker('invoice')">上传收据/发票</a-button>
            <a-button @click="downloadBundle">下载打包</a-button>
            <a-button @click="downloadTemplate('contract')">下载空白合同</a-button>
            <a-button @click="downloadTemplate('invoice')">下载空白发票</a-button>
            <a-button @click="downloadTemplate('assessment')">下载空白评估报告</a-button>
          </a-space>
          <input
            ref="fileInputRef"
            type="file"
            accept=".pdf,.jpg,.jpeg,.png,.doc,.docx"
            style="display: none"
            @change="onFileSelected"
          />
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
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import PageContainer from '../../../components/PageContainer.vue'

const router = useRouter()
const fileInputRef = ref<HTMLInputElement | null>(null)
const uploadType = ref<'contract' | 'invoice'>('contract')

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

function openFilePicker(type: 'contract' | 'invoice') {
  uploadType.value = type
  fileInputRef.value?.click()
}

function onFileSelected(event: Event) {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return
  const typeText = uploadType.value === 'contract' ? '合同' : '收据/发票'
  message.success(`${typeText}文件已选择：${file.name}（演示环境）`)
  target.value = ''
}

function downloadBundle() {
  const content = rows
    .map((item) => `${item.type},${item.name},${item.version},${item.operator},${item.time}`)
    .join('\n')
  const csv = `类型,文件名,版本,上传人,上传时间\n${content}`
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = '合同与票据-附件清单.csv'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
  message.success('附件清单已下载')
}

function downloadTemplate(type: 'contract' | 'invoice' | 'assessment') {
  const templates = {
    contract: {
      name: '空白合同模板.txt',
      content: `【长者入住合同（空白模板）】
合同编号：
甲方（机构）：
乙方（长者/监护人）：
入住日期：
护理等级：
服务套餐：
费用条款：
押金：
付款周期：
双方签字：
日期：`
    },
    invoice: {
      name: '空白发票模板.txt',
      content: `【发票信息登记模板】
发票抬头：
税号：
开票金额：
项目名称：
合同编号：
开票日期：
备注：`
    },
    assessment: {
      name: '空白评估报告模板.txt',
      content: `【入住评估报告（空白模板）】
评估日期：
评估人：
自理能力：
认知能力：
风险评估：
护理需求：
评估结论：
建议护理等级：
备注：`
    }
  } as const
  const item = templates[type]
  const blob = new Blob([item.content], { type: 'text/plain;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = item.name
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
  message.success('模板已下载')
}
</script>
