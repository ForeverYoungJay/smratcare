const { getStaffApprovals, submitStaffApproval } = require('../../services/staff');
const { requestSubscribe } = require('../../utils/subscribe');

const today = new Date().toISOString().slice(0, 10);

function toDateTime(date, time) {
  if (!date || !time) return '';
  return `${date}T${time}:00`;
}

function typeText(type) {
  if (type === 'LEAVE') return '请假';
  if (type === 'SHIFT_CHANGE') return '调班';
  return type || '审批';
}

function statusText(status) {
  if (status === 'APPROVED') return '已通过';
  if (status === 'REJECTED') return '已驳回';
  return '待审批';
}

function normalizeApproval(item) {
  return {
    ...item,
    typeText: typeText(item.approvalType),
    statusText: statusText(item.status),
    timeText: `${String(item.startTime || '').replace('T', ' ')} 至 ${String(item.endTime || '').replace('T', ' ')}`,
    createText: String(item.createTime || '').replace('T', ' ')
  };
}

Page({
  data: {
    activeType: 'LEAVE',
    approvals: [],
    loading: false,
    loadingMore: false,
    submitting: false,
    loadError: '',
    submitError: '',
    submitSuccess: '',
    lastApprovalSummary: '',
    pageNo: 1,
    pageSize: 20,
    hasMore: false,
    leaveTypes: ['年假', '病假', '事假', '调休', '其他'],
    leaveTypeIndex: 1,
    startDate: today,
    startTime: '08:00',
    endDate: today,
    endTime: '17:30',
    targetShiftDate: today,
    targetShiftTime: '15:30',
    targetStaffName: '',
    reason: '',
    policyAcknowledged: false
  },
  onShow() {
    getApp().ensureLogin();
    this.loadData({ reset: true });
  },
  async loadData(options = {}) {
    const reset = options.reset !== false;
    const nextPageNo = reset ? 1 : Number(this.data.pageNo || 1) + 1;
    this.setData(reset ? { loading: true, loadError: '' } : { loadingMore: true, loadError: '' });
    try {
      const approvals = await getStaffApprovals({ pageNo: nextPageNo, pageSize: this.data.pageSize });
      const normalized = (approvals || []).map(normalizeApproval);
      this.setData({
        approvals: reset ? normalized : this.data.approvals.concat(normalized),
        pageNo: nextPageNo,
        hasMore: normalized.length >= this.data.pageSize
      });
    } catch (error) {
      this.setData({ loadError: error.message || (reset ? '审批记录加载失败' : '更多审批记录加载失败，请稍后重试') });
    } finally {
      this.setData({ loading: false, loadingMore: false });
    }
  },
  switchType(e) {
    this.setData({
      activeType: e.currentTarget.dataset.type,
      submitError: '',
      submitSuccess: ''
    });
  },
  loadMore() {
    if (this.data.loading || this.data.loadingMore || !this.data.hasMore) return;
    this.loadData({ reset: false });
  },
  onReachBottom() {
    this.loadMore();
  },
  onLeaveTypeChange(e) {
    this.setData({ leaveTypeIndex: Number(e.detail.value), submitError: '', submitSuccess: '' });
  },
  onStartDateChange(e) {
    this.setData({ startDate: e.detail.value, submitError: '', submitSuccess: '' });
  },
  onStartTimeChange(e) {
    this.setData({ startTime: e.detail.value, submitError: '', submitSuccess: '' });
  },
  onEndDateChange(e) {
    this.setData({ endDate: e.detail.value, submitError: '', submitSuccess: '' });
  },
  onEndTimeChange(e) {
    this.setData({ endTime: e.detail.value, submitError: '', submitSuccess: '' });
  },
  onTargetShiftDateChange(e) {
    this.setData({ targetShiftDate: e.detail.value, submitError: '', submitSuccess: '' });
  },
  onTargetShiftTimeChange(e) {
    this.setData({ targetShiftTime: e.detail.value, submitError: '', submitSuccess: '' });
  },
  onTargetStaffInput(e) {
    this.setData({ targetStaffName: e.detail.value, submitError: '', submitSuccess: '' });
  },
  onReasonInput(e) {
    this.setData({ reason: e.detail.value, submitError: '', submitSuccess: '' });
  },
  onPolicyChange(e) {
    this.setData({
      policyAcknowledged: e.detail.value.includes('yes'),
      submitError: '',
      submitSuccess: ''
    });
  },
  buildPayload() {
    const isLeave = this.data.activeType === 'LEAVE';
    const startTime = toDateTime(this.data.startDate, this.data.startTime);
    const endTime = toDateTime(this.data.endDate, this.data.endTime);
    const reason = this.data.reason.trim();
    const form = isLeave
      ? {
          leaveType: this.data.leaveTypes[this.data.leaveTypeIndex],
          reason,
          policyAcknowledged: this.data.policyAcknowledged,
          source: 'staff-mobile'
        }
      : {
          scene: 'shift-change',
          reason,
          targetStaffName: this.data.targetStaffName.trim(),
          targetDutyDate: this.data.targetShiftDate,
          targetShiftTime: this.data.targetShiftTime,
          source: 'staff-mobile'
        };
    return {
      approvalType: isLeave ? 'LEAVE' : 'SHIFT_CHANGE',
      title: `${isLeave ? '请假' : '调班'}申请：移动端提交`,
      startTime,
      endTime,
      formData: JSON.stringify(form),
      status: 'PENDING',
      remark: reason
    };
  },
  validatePayload(payload) {
    if (!payload.startTime || !payload.endTime) return '请选择完整开始和结束时间';
    if (payload.startTime > payload.endTime) return '开始时间不能晚于结束时间';
    if (!this.data.reason.trim()) return '请填写申请原因';
    if (this.data.activeType === 'LEAVE' && !this.data.policyAcknowledged) {
      return '请先确认已阅读请假制度';
    }
    if (this.data.activeType === 'SHIFT_CHANGE' && !this.data.targetStaffName.trim()) {
      return '请填写调班对象或说明';
    }
    return '';
  },
  async submitApproval() {
    if (this.data.submitting) return;
    const payload = this.buildPayload();
    const validation = this.validatePayload(payload);
    if (validation) {
      wx.showToast({ title: validation, icon: 'none' });
      return;
    }
    this.setData({ submitting: true, submitError: '', submitSuccess: '' });
    // 提交前在点击手势内请求“审批结果”订阅授权；模板未配置或用户拒绝都静默跳过，不阻塞提交。
    await requestSubscribe('approvalResult');
    try {
      const item = await submitStaffApproval(payload);
      const summary = this.data.activeType === 'LEAVE'
        ? `${this.data.leaveTypes[this.data.leaveTypeIndex]} · ${this.data.startDate} ${this.data.startTime}`
        : `调班 · ${this.data.targetStaffName.trim() || '未填写对象'} · ${this.data.targetShiftDate}`;
      this.setData({
        approvals: [normalizeApproval(item)].concat(this.data.approvals),
        reason: '',
        policyAcknowledged: false,
        submitSuccess: '审批申请已提交，可在下方继续跟踪审批状态。',
        lastApprovalSummary: summary
      });
      wx.showToast({ title: '已提交审批', icon: 'success' });
    } catch (error) {
      this.setData({ submitError: error.message || '提交失败，请稍后重试' });
      wx.showToast({ title: error.message || '提交失败', icon: 'none' });
    } finally {
      this.setData({ submitting: false });
    }
  }
});
