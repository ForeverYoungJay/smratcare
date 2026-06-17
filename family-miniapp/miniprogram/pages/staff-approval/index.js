const { getStaffApprovals, submitStaffApproval } = require('../../services/staff');

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
    submitting: false,
    loadError: '',
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
    this.loadData();
  },
  async loadData() {
    this.setData({ loading: true, loadError: '' });
    try {
      const approvals = await getStaffApprovals({ pageSize: 30 });
      this.setData({ approvals: (approvals || []).map(normalizeApproval) });
    } catch (error) {
      this.setData({ loadError: error.message || '审批记录加载失败' });
    } finally {
      this.setData({ loading: false });
    }
  },
  switchType(e) {
    this.setData({ activeType: e.currentTarget.dataset.type });
  },
  onLeaveTypeChange(e) {
    this.setData({ leaveTypeIndex: Number(e.detail.value) });
  },
  onStartDateChange(e) {
    this.setData({ startDate: e.detail.value });
  },
  onStartTimeChange(e) {
    this.setData({ startTime: e.detail.value });
  },
  onEndDateChange(e) {
    this.setData({ endDate: e.detail.value });
  },
  onEndTimeChange(e) {
    this.setData({ endTime: e.detail.value });
  },
  onTargetShiftDateChange(e) {
    this.setData({ targetShiftDate: e.detail.value });
  },
  onTargetShiftTimeChange(e) {
    this.setData({ targetShiftTime: e.detail.value });
  },
  onTargetStaffInput(e) {
    this.setData({ targetStaffName: e.detail.value });
  },
  onReasonInput(e) {
    this.setData({ reason: e.detail.value });
  },
  onPolicyChange(e) {
    this.setData({ policyAcknowledged: e.detail.value.includes('yes') });
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
    this.setData({ submitting: true });
    try {
      const item = await submitStaffApproval(payload);
      this.setData({
        approvals: [normalizeApproval(item)].concat(this.data.approvals),
        reason: '',
        policyAcknowledged: false
      });
      wx.showToast({ title: '已提交审批', icon: 'success' });
    } catch (error) {
      wx.showToast({ title: error.message || '提交失败', icon: 'none' });
    } finally {
      this.setData({ submitting: false });
    }
  }
});
