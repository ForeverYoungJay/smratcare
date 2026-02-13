# Plugins 使用说明

本目录封装所有补充型能力库，统一以插件/组件方式在全局注册，便于复用与维护。

## 1) Ant Design X（智能助手）
- 入口：`src/plugins/antdx.ts`
- 全局组件：`AdxChat`
- 用法：
  - 在任意页面模板中使用 `<AdxChat />`
  - 通过 `v-model` 控制消息列表（`messages`）

## 2) vxe-table（高级表格）
- 入口：`src/plugins/vxeTable.ts`
- 全局组件：`vxe-table` / `vxe-column` / `vxe-toolbar` 等
- 用法：
  - 在页面内直接使用 `<vxe-table />`
  - 示例页：`/demo/vxe-table`

## 3) FullCalendar（排班日历）
- 入口：`src/plugins/fullcalendar.ts`
- 全局组件：`FullCalendar`
- 用法：
  - `<FullCalendar :options="options" />`
  - 样式已在插件中引入

## 4) ECharts（报表图表）
- 入口：`src/plugins/echarts.ts`
- Hooks：`useECharts(elRef, options)`
- 用法：
  - 传入 DOM 容器 ref 与 options，自动初始化/销毁
  - 示例页：`/demo/charts`

## 5) 富文本编辑器（WangEditor）
- 入口：`src/plugins/editor.ts`
- 全局组件：`RichTextEditor`
- 用法：
  - `<RichTextEditor v-model="content" />`
  - 支持双向绑定

## 6) 流程图（LogicFlow）
- 入口：`src/plugins/flow.ts`
- 全局组件：`FlowDesigner`
- 用法：
  - `<FlowDesigner :data="graphData" @ready="onReady" />`
  - 支持 resize 自适应

## Demo 路由
所有 Demo 已加入菜单：
- /demo/vxe-table
- /demo/calendar
- /demo/charts
- /demo/editor
- /demo/flow
- /demo/antdx-chat

