import { describe, expect, it } from 'vitest'
import { createMemoryHistory, createRouter } from 'vue-router'
import { routes } from '../router/routes'
import { DEPARTMENT_HOME_CONFIG, formatDepartmentHomeMetric } from './departmentHome'
import { DEPARTMENT_ROLE_MAP } from '../access/policy'
import { getMenuTree, type MenuItem } from '../layouts/menu'
import { getRecommendedPagePermissions } from '../utils/pageAccess'

function leafPaths(items: MenuItem[]): string[] {
  return items.flatMap((item) => [item.path || '', ...leafPaths(item.children || [])]).filter(Boolean)
}

describe('department home configuration', () => {
  const router = createRouter({ history: createMemoryHistory(), routes })
  const configs = Object.values(DEPARTMENT_HOME_CONFIG)

  it('defines one complete home and five-step flow for every department', () => {
    expect(configs).toHaveLength(6)
    configs.forEach((config) => {
      expect(config.homePath).toMatch(/^\//)
      expect(config.moduleRoots.length).toBeGreaterThan(0)
      expect(config.steps).toHaveLength(5)
      expect(new Set(config.steps.map((step) => step.path.split('?')[0])).size).toBe(5)
    })
  })

  it('only references routes that exist', () => {
    const paths = configs.flatMap((config) => [
      config.homePath,
      ...config.moduleRoots,
      ...config.steps.map((step) => step.path),
      ...(config.extraGroups || []).flatMap((group) => group.paths)
    ])
    paths.forEach((path) => {
      const resolved = router.resolve(path)
      expect(resolved.matched.length, path).toBeGreaterThan(0)
      expect(resolved.name, path).not.toBe('NotFound')
    })
  })

  it('does not duplicate configured feature paths inside a department', () => {
    configs.forEach((config) => {
      const paths = (config.extraGroups || []).flatMap((group) => group.paths.map((path) => path.split('?')[0]))
      expect(new Set(paths).size, config.department).toBe(paths.length)
    })
  })

  it('distinguishes explicit zero values from missing data', () => {
    expect(formatDepartmentHomeMetric({ value: 0 })).toBe('0')
    expect(formatDepartmentHomeMetric({ value: 0, suffix: '%' })).toBe('0%')
    expect(formatDepartmentHomeMetric({ value: undefined })).toBe('--')
    expect(formatDepartmentHomeMetric({ value: null })).toBe('--')
  })

  it('exposes each department module through employee and minister permissions', () => {
    configs.forEach((config) => {
      const roles = DEPARTMENT_ROLE_MAP[config.department]
      const employeeMenu = getMenuTree([roles.employee], getRecommendedPagePermissions(roles.employee), { focused: false })
      const ministerMenu = getMenuTree([roles.minister], getRecommendedPagePermissions(roles.minister), { focused: false })
      config.moduleRoots.forEach((root) => {
        const employeeCount = leafPaths(employeeMenu).filter((path) => path === root || path.startsWith(`${root}/`)).length
        const ministerCount = leafPaths(ministerMenu).filter((path) => path === root || path.startsWith(`${root}/`)).length
        expect(employeeCount, `${config.department} employee ${root}`).toBeGreaterThan(0)
        expect(ministerCount, `${config.department} minister ${root}`).toBeGreaterThanOrEqual(employeeCount)
      })
    })
  })
})
