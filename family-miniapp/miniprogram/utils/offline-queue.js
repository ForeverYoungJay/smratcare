const QUEUE_STORAGE_KEY = 'staff_offline_queue';
const MAX_RETRY = 5;

let flushing = false;

function readQueue() {
  try {
    const raw = wx.getStorageSync(QUEUE_STORAGE_KEY);
    return Array.isArray(raw) ? raw : [];
  } catch (error) {
    return [];
  }
}

function writeQueue(items) {
  try {
    wx.setStorageSync(QUEUE_STORAGE_KEY, Array.isArray(items) ? items : []);
  } catch (error) {
    // Storage full or unavailable: keep in-memory only, nothing else we can do.
  }
}

function isNetworkError(error) {
  if (!error) return false;
  return error.code === 'NETWORK_ERROR' || error.code === 'TIMEOUT';
}

function enqueue(type, payload) {
  const item = {
    id: `offline-${Date.now()}-${Math.floor(Math.random() * 100000)}`,
    type,
    payload: payload || {},
    createdAt: new Date().toISOString().slice(0, 19),
    retryCount: 0
  };
  const queue = readQueue();
  queue.push(item);
  writeQueue(queue);
  return item;
}

function getQueueLength() {
  return readQueue().length;
}

async function submitItem(item) {
  // Lazy require to avoid module cycles at load time.
  const staff = require('../services/staff');
  const payload = item.payload || {};
  if (item.type === 'task-complete') {
    const kind = payload.kind || '';
    const data = payload.data || {};
    if (kind === 'care') return staff.submitCareExecutionReceipt(payload.taskId, data);
    if (kind === 'medication') return staff.submitMedicationReceipt(payload.taskId, data);
    if (kind === 'inspection') return staff.submitInspectionReceipt(payload.taskId, data);
    if (kind === 'repair') return staff.submitRepairReceipt(payload.taskId, data);
    if (kind === 'meal') return staff.submitMealDeliveryReceipt(payload.taskId, data);
    return staff.completeTask(payload.taskId, data);
  }
  if (item.type === 'incident') return staff.submitIncident(payload);
  if (item.type === 'handover') return staff.submitHandover(payload);
  if (item.type === 'vitals') return staff.submitStaffVitalRecord(payload);
  if (item.type === 'patrol-scan') return staff.submitPatrolScan(payload);
  // Unknown type: treat as done so it does not clog the queue forever.
  return null;
}

async function flush() {
  if (flushing) {
    return { submitted: 0, remaining: getQueueLength() };
  }
  const app = typeof getApp === 'function' ? getApp() : null;
  if (!app || !app.globalData || !app.globalData.token) {
    return { submitted: 0, remaining: getQueueLength() };
  }
  flushing = true;
  try {
    const queue = readQueue();
    if (!queue.length) {
      return { submitted: 0, remaining: 0 };
    }
    const remaining = [];
    let submitted = 0;
    for (const item of queue) {
      try {
        await submitItem(item);
        submitted += 1;
      } catch (error) {
        const retryCount = Number(item.retryCount || 0) + 1;
        if (retryCount < MAX_RETRY) {
          remaining.push({ ...item, retryCount });
        }
        // Reached MAX_RETRY: drop the item to avoid endless retries.
      }
    }
    writeQueue(remaining);
    if (submitted > 0) {
      wx.showToast({ title: `已自动补交 ${submitted} 条离线记录`, icon: 'none' });
    }
    return { submitted, remaining: remaining.length };
  } finally {
    flushing = false;
  }
}

module.exports = {
  enqueue,
  flush,
  isNetworkError,
  getQueueLength
};
