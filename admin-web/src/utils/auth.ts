const TOKEN_KEY = 'zhiyangyun_token'
const ROLES_KEY = 'zhiyangyun_roles'

export function getToken(): string {
  return localStorage.getItem(TOKEN_KEY) || ''
}

export function setToken(token: string) {
  localStorage.setItem(TOKEN_KEY, token)
}

export function clearToken() {
  localStorage.removeItem(TOKEN_KEY)
}

export function getRoles(): string[] {
  const raw = localStorage.getItem(ROLES_KEY)
  return raw ? JSON.parse(raw) : []
}

export function setRoles(roles: string[]) {
  localStorage.setItem(ROLES_KEY, JSON.stringify(roles || []))
}

export function clearRoles() {
  localStorage.removeItem(ROLES_KEY)
}
