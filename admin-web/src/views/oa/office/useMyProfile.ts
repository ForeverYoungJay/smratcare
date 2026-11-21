import { ref } from 'vue'
import { getMyProfile, type MyProfile } from '../../../api/me'

// 同一次会话里多个页面共用一份本人档案，避免每个页面重复请求
let pending: Promise<MyProfile | undefined> | null = null
const profile = ref<MyProfile>()

export function useMyProfile() {
  if (!pending) {
    pending = getMyProfile().then((p) => (profile.value = p)).catch(() => { pending = null; return undefined })
  }
  return profile
}
