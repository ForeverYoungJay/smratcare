const {
  getStaffVitalRecords,
  submitStaffVitalRecord
} = require('../../services/staff');

const dataTypeOptions = [
  { label: '血压 BP', value: 'BP', unit: 'mmHg', placeholder: '例如 120/80' },
  { label: '心率 HR', value: 'HR', unit: '次/分', placeholder: '例如 78' },
  { label: '体温 TEMP', value: 'TEMP', unit: '℃', placeholder: '例如 36.6' },
  { label: '血氧 SPO2', value: 'SPO2', unit: '%', placeholder: '例如 96' },
  { label: '血糖 GLUCOSE', value: 'GLUCOSE', unit: 'mmol/L', placeholder: '例如 6.2' },
  { label: '体重 WEIGHT', value: 'WEIGHT', unit: 'kg', placeholder: '例如 62.5' },
  { label: '其他 OTHER', value: 'OTHER', unit: '', placeholder: '填写观察结果' }
];

const statusFilters = [
  { label: '全部', value: '' },
  { label: '异常', value: '1' },
  { label: '正常', value: '0' }
];

function nowText() {
  const date = new Date();
  const pad = (value) => String(value).padStart(2, '0');
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`;
}

function splitDateTime(value) {
  const text = String(value || nowText());
  return {
    measuredDate: text.slice(0, 10),
    measuredTime: text.slice(11, 16)
  };
}

function normalizeMeasuredAt(value) {
  return String(value || '').replace(' ', 'T');
}

function typeMeta(type) {
  return dataTypeOptions.find((item) => item.value === type) || dataTypeOptions[0];
}

function validateDataValue(dataType, value) {
  const text = String(value || '').trim();
  if (!text) return '请填写体征数值';
  if (dataType === 'BP') {
    if (!/^\d{2,3}\/\d{2,3}$/.test(text)) return '血压请使用 120/80 格式';
    const parts = text.split('/').map((item) => Number(item));
    if (parts[0] < 50 || parts[0] > 260 || parts[1] < 30 || parts[1] > 180 || parts[0] <= parts[1]) {
      return '血压值超出合理范围';
    }
    return '';
  }
  if (dataType === 'OTHER') return '';
  const numeric = Number(text);
  if (!Number.isFinite(numeric)) return '该类型体征需填写数字';
  if (dataType === 'HR' && (numeric < 20 || numeric > 240)) return '心率超出合理范围';
  if (dataType === 'TEMP' && (numeric < 30 || numeric > 45)) return '体温超出合理范围';
  if (dataType === 'SPO2' && (numeric < 0 || numeric > 100)) return '血氧超出合理范围';
  if (dataType === 'GLUCOSE' && (numeric < 0 || numeric > 40)) return '血糖超出合理范围';
  if (dataType === 'WEIGHT' && (numeric <= 0 || numeric > 300)) return '体重超出合理范围';
  return '';
}

function recommendAbnormalFlag(dataType, value) {
  const text = String(value || '').trim();
  if (dataType === 'BP') {
    if (!/^\d{2,3}\/\d{2,3}$/.test(text)) return 0;
    const parts = text.split('/').map((item) => Number(item));
    return parts[0] >= 140 || parts[0] < 90 || parts[1] >= 90 || parts[1] < 60 ? 1 : 0;
  }
  const numeric = Number(text);
  if (!Number.isFinite(numeric)) return 0;
  if (dataType === 'HR') return numeric < 60 || numeric > 100 ? 1 : 0;
  if (dataType === 'TEMP') return numeric < 36 || numeric > 37.3 ? 1 : 0;
  if (dataType === 'SPO2') return numeric < 95 ? 1 : 0;
  if (dataType === 'GLUCOSE') return numeric < 3.9 || numeric > 10 ? 1 : 0;
  return 0;
}

function normalizeRecord(item) {
  const meta = typeMeta(item.dataType);
  return {
    ...item,
    typeLabel: meta.label,
    unit: meta.unit,
    measuredText: String(item.measuredAt || item.createTime || '').replace('T', ' '),
    statusText: Number(item.abnormalFlag || 0) === 1 ? '异常' : '正常',
    statusClass: Number(item.abnormalFlag || 0) === 1 ? 'danger' : 'done'
  };
}

Page({
  data: {
    loading: false,
    submitting: false,
    loadError: '',
    records: [],
    dataTypeOptions,
    statusFilters,
    dataTypeIndex: 0,
    activeAbnormal: '',
    keyword: '',
    elderName: '',
    dataValue: '',
    measuredAt: nowText(),
    measuredDate: nowText().slice(0, 10),
    measuredTime: nowText().slice(11, 16),
    remark: '',
    abnormalFlag: 0,
    unit: dataTypeOptions[0].unit,
    placeholder: dataTypeOptions[0].placeholder
  },
  onShow() {
    getApp().ensureLogin();
    this.loadData();
  },
  async loadData() {
    this.setData({ loading: true, loadError: '' });
    try {
      const records = await getStaffVitalRecords({
        keyword: this.data.keyword.trim(),
        abnormalFlag: this.data.activeAbnormal === '' ? '' : Number(this.data.activeAbnormal),
        pageSize: 20
      });
      this.setData({ records: (records || []).map(normalizeRecord) });
    } catch (error) {
      this.setData({ loadError: error.message || '体征记录加载失败' });
    } finally {
      this.setData({ loading: false });
    }
  },
  onInput(e) {
    const field = e.currentTarget.dataset.field;
    this.setData({ [field]: e.detail.value });
    if (field === 'dataValue') this.refreshAbnormalFlag(e.detail.value);
  },
  onTypeChange(e) {
    const dataTypeIndex = Number(e.detail.value || 0);
    const meta = this.data.dataTypeOptions[dataTypeIndex] || this.data.dataTypeOptions[0];
    this.setData({
      dataTypeIndex,
      unit: meta.unit,
      placeholder: meta.placeholder,
      dataValue: '',
      abnormalFlag: 0
    });
  },
  onDateChange(e) {
    const measuredDate = e.detail.value;
    const measuredTime = this.data.measuredTime || '00:00';
    this.setData({
      measuredDate,
      measuredAt: `${measuredDate} ${measuredTime}:00`
    });
  },
  onTimeChange(e) {
    const measuredTime = e.detail.value;
    const measuredDate = this.data.measuredDate || nowText().slice(0, 10);
    this.setData({
      measuredTime,
      measuredAt: `${measuredDate} ${measuredTime}:00`
    });
  },
  switchAbnormal(e) {
    this.setData({ activeAbnormal: e.currentTarget.dataset.value || '' });
    this.loadData();
  },
  searchRecords() {
    this.loadData();
  },
  refreshAbnormalFlag(value) {
    const dataType = this.data.dataTypeOptions[this.data.dataTypeIndex].value;
    if (validateDataValue(dataType, value)) return;
    this.setData({ abnormalFlag: recommendAbnormalFlag(dataType, value) });
  },
  toggleAbnormal() {
    this.setData({ abnormalFlag: this.data.abnormalFlag ? 0 : 1 });
  },
  validateForm() {
    if (!this.data.elderName.trim()) return '请填写老人姓名';
    const dataType = this.data.dataTypeOptions[this.data.dataTypeIndex].value;
    return validateDataValue(dataType, this.data.dataValue);
  },
  async submitRecord() {
    if (this.data.submitting) return;
    const validation = this.validateForm();
    if (validation) {
      wx.showToast({ title: validation, icon: 'none' });
      return;
    }
    const dataType = this.data.dataTypeOptions[this.data.dataTypeIndex].value;
    this.setData({ submitting: true });
    try {
      await submitStaffVitalRecord({
        elderName: this.data.elderName.trim(),
        dataType,
        dataValue: this.data.dataValue.trim(),
        measuredAt: normalizeMeasuredAt(this.data.measuredAt),
        abnormalFlag: this.data.abnormalFlag,
        remark: this.data.remark.trim(),
        source: 'staff-mobile'
      });
      wx.showToast({ title: '体征已补录', icon: 'success' });
      this.setData({
        dataValue: '',
        remark: '',
        measuredAt: nowText(),
        ...splitDateTime(nowText()),
        abnormalFlag: 0,
        keyword: this.data.elderName.trim()
      });
      this.loadData();
    } catch (error) {
      wx.showToast({ title: error.message || '补录失败', icon: 'none' });
    } finally {
      this.setData({ submitting: false });
    }
  }
});
