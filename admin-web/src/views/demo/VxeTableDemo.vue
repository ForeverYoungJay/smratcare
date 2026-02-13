<template>
  <PageContainer title="VXE-Table Demo" subTitle="高级表格 CRUD 示例">
    <a-space style="margin-bottom: 12px;">
      <a-button type="primary" @click="addRow">新增</a-button>
      <a-button @click="save">保存</a-button>
    </a-space>
    <vxe-table border height="400" :data="tableData" class="vxe-scope">
      <vxe-column type="seq" width="60" />
      <vxe-column field="name" title="姓名" />
      <vxe-column field="age" title="年龄" />
      <vxe-column field="dept" title="部门" />
      <vxe-column title="操作" width="140">
        <template #default="{ row }">
          <a-space>
            <a @click="editRow(row)">编辑</a>
            <a @click="removeRow(row)">删除</a>
          </a-space>
        </template>
      </vxe-column>
    </vxe-table>

    <a-modal v-model:open="editOpen" title="编辑" @ok="confirmEdit">
      <a-form layout="vertical">
        <a-form-item label="姓名"><a-input v-model:value="editForm.name" /></a-form-item>
        <a-form-item label="年龄"><a-input-number v-model:value="editForm.age" style="width:100%" /></a-form-item>
        <a-form-item label="部门"><a-input v-model:value="editForm.dept" /></a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'

const tableData = ref([
  { id: 1, name: '王婷', age: 30, dept: '护理部' },
  { id: 2, name: '李晨', age: 34, dept: '医疗部' }
])

const editOpen = ref(false)
const editForm = reactive<any>({})
let editingRow: any = null

function addRow() {
  tableData.value.push({ id: Date.now(), name: '新员工', age: 20, dept: '部门' })
}

function editRow(row: any) {
  editingRow = row
  Object.assign(editForm, row)
  editOpen.value = true
}

function confirmEdit() {
  Object.assign(editingRow, editForm)
  editOpen.value = false
}

function removeRow(row: any) {
  tableData.value = tableData.value.filter((r) => r.id !== row.id)
}

function save() {
  message.success('保存成功')
}
</script>
