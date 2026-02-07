<template>
  <aside class="sidenav">
    <div class="brand">
      <div class="logo">智</div>
      <div>
        <div class="title">智养云</div>
        <div class="subtitle">Care Cloud Admin</div>
      </div>
    </div>

    <nav class="menu">
      <div class="menu-group">运营概览</div>
      <a class="menu-item active">总览</a>
      <a class="menu-item">任务中心</a>
      <a class="menu-item">老人档案</a>
      <a class="menu-item">商城与库存</a>
      <a class="menu-item">风险规则</a>

      <div class="menu-group">机构设置</div>
      <a class="menu-item">组织部门</a>
      <a class="menu-item">账号权限</a>
      <a class="menu-item">计费配置</a>
    </nav>

    <div class="footer">
      <div class="badge success">ONLINE</div>
      <div class="footer-text">数据隔离 · 多机构</div>
    </div>
  </aside>
</template>

<style scoped>
.sidenav {
  background: linear-gradient(180deg, #1f3b40 0%, #1b2f33 100%);
  color: #f8f5f0;
  padding: 28px 24px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.brand {
  display: flex;
  gap: 12px;
  align-items: center;
}

.logo {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  background: linear-gradient(140deg, #d0a569, #f2d59a);
  color: #1b2f33;
  display: grid;
  place-items: center;
  font-weight: 700;
  font-size: 20px;
}

.title {
  font-weight: 700;
  font-size: 18px;
}

.subtitle {
  font-size: 12px;
  color: rgba(248, 245, 240, 0.7);
}

.menu {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.menu-group {
  font-size: 11px;
  letter-spacing: 1.2px;
  text-transform: uppercase;
  color: rgba(248, 245, 240, 0.5);
  margin-top: 12px;
}

.menu-item {
  padding: 10px 12px;
  border-radius: 12px;
  color: rgba(248, 245, 240, 0.85);
  background: transparent;
  transition: all 0.2s ease;
}

.menu-item:hover {
  background: rgba(255, 255, 255, 0.08);
}

.menu-item.active {
  background: rgba(242, 213, 154, 0.2);
  color: #f2d59a;
  font-weight: 600;
}

.footer {
  margin-top: auto;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.footer-text {
  font-size: 12px;
  color: rgba(248, 245, 240, 0.6);
}
</style>
