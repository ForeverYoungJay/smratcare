/**
 * 巡检/护理记录的体征串（“体温:36.5，血压:142/88，脉搏:-，呼吸:-…”）面向家属展示前清洗：
 * 去掉值为 “-” 的空项与空分段，保留有效数值与提醒文字，降低阅读噪音。
 */
function cleanVitalText(raw) {
  const text = String(raw || '').trim();
  if (!text) return text;
  const segments = text
    .split(/；|;/)
    .map((segment) => segment
      .split(/，|,/)
      .map((part) => part.trim())
      .filter((part) => part && !/^[^:：]+[:：]\s*[-—]\s*$/.test(part))
      .join('，'))
    .filter(Boolean);
  return segments.join('；') || text;
}

module.exports = { cleanVitalText };
