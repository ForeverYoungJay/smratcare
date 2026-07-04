const DRAFT_KEY_PREFIX = 'staff_form_draft_';

function draftKey(page, id) {
  return `${DRAFT_KEY_PREFIX}${page}_${id || 'default'}`;
}

function saveDraft(page, id, data) {
  try {
    wx.setStorageSync(draftKey(page, id), {
      data: data || {},
      savedAt: Date.now()
    });
  } catch (error) {
    // Storage unavailable: draft is best-effort only.
  }
}

function loadDraft(page, id) {
  try {
    const raw = wx.getStorageSync(draftKey(page, id));
    if (raw && raw.data && typeof raw.data === 'object') {
      return raw.data;
    }
  } catch (error) {
    // Ignore and treat as no draft.
  }
  return null;
}

function clearDraft(page, id) {
  try {
    wx.removeStorageSync(draftKey(page, id));
  } catch (error) {
    // Ignore.
  }
}

module.exports = {
  saveDraft,
  loadDraft,
  clearDraft
};
