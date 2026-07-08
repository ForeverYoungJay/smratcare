/**
 * 通用表单校验工具。
 *
 * 手机号 / 身份证等规则此前散落在 ContractSigning、elder/Create、BasicLayout 等多处内联，
 * 集中到此处以保证规则一致，并方便复用到 Ant Design Vue 的 rules 配置。
 */

/** 中国大陆手机号：1 开头共 11 位。 */
export const MOBILE_PHONE_REGEX = /^1\d{10}$/
/** 身份证号：15 位或 18 位（末位可为 X/x）。 */
export const ID_CARD_REGEX = /^(\d{15}|\d{17}[\dXx])$/

export function isMobilePhone(value?: string | null): boolean {
  return MOBILE_PHONE_REGEX.test(String(value ?? '').trim())
}

export function isChineseIdCard(value?: string | null): boolean {
  return ID_CARD_REGEX.test(String(value ?? '').trim())
}

/**
 * 生成 Ant Design Vue 兼容的校验器：非空 + 格式校验。
 * @param options.requiredMessage 为空时的提示；传入后即视为必填
 * @param options.formatMessage 格式不符时的提示
 */
function makeValidator(
  test: (text: string) => boolean,
  options: { requiredMessage?: string; formatMessage: string }
) {
  return (_rule: unknown, value?: string) => {
    const text = String(value ?? '').trim()
    if (!text) {
      return options.requiredMessage
        ? Promise.reject(new Error(options.requiredMessage))
        : Promise.resolve()
    }
    return test(text) ? Promise.resolve() : Promise.reject(new Error(options.formatMessage))
  }
}

export function mobilePhoneValidator(requiredMessage?: string, formatMessage = '手机号格式不正确') {
  return makeValidator(isMobilePhone, { requiredMessage, formatMessage })
}

export function idCardValidator(requiredMessage?: string, formatMessage = '身份证号格式不正确') {
  return makeValidator(isChineseIdCard, { requiredMessage, formatMessage })
}
