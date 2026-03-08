<template>
  <BillCenter
    title="在住账单缴费"
    subTitle="在住周期账单对账与收款登记"
    defaultScene="RESIDENT"
    :lockScene="true"
    :defaultCurrentMonth="true"
  />
</template>

<script setup lang="ts">
import BillCenter from './BillCenter.vue'
</script>
