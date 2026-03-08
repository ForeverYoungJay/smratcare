<template>
  <BillCenter
    title="入住账单缴费"
    subTitle="新入住首期账单查询、生成与收款登记"
    defaultScene="ADMISSION"
    :lockScene="true"
    :defaultCurrentMonth="true"
  />
</template>

<script setup lang="ts">
import BillCenter from './BillCenter.vue'
</script>
