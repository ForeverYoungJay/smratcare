import { describe, expect, it, vi } from 'vitest'
import { permission } from './permission'

let mockRoles: string[] = []

vi.mock('../utils/auth', () => ({
  getRoles: () => mockRoles
}))

describe('permission directive', () => {
  it('removes admin-only elements for minister roles without explicit admin', () => {
    mockRoles = ['HR_MINISTER']
    const parent = document.createElement('div')
    const el = document.createElement('button')
    parent.appendChild(el)

    permission.mounted?.(el, { value: ['ADMIN'] } as any)

    expect(parent.contains(el)).toBe(false)
  })

  it('keeps admin-only elements for admin users', () => {
    mockRoles = ['ADMIN']
    const parent = document.createElement('div')
    const el = document.createElement('button')
    parent.appendChild(el)

    permission.mounted?.(el, { value: ['ADMIN'] } as any)

    expect(parent.contains(el)).toBe(true)
  })
})
