import { DEPARTMENT_EMPLOYEE_ROLES, DEPARTMENT_MINISTER_ROLES, SUPER_ROLES } from './roleAccess';
const TOP_SUPER_ROLES = [...SUPER_ROLES, 'DEAN'];
function toRoleSet(roleCodes) {
    return new Set((roleCodes || []).map((item) => String(item || '').trim().toUpperCase()).filter(Boolean));
}
export function hasAnyRoleCode(roleCodes, required) {
    const set = toRoleSet(roleCodes);
    return required.some((role) => set.has(String(role || '').toUpperCase()));
}
export function isDepartmentMinister(roleCodes) {
    return hasAnyRoleCode(roleCodes, DEPARTMENT_MINISTER_ROLES);
}
export function isDepartmentEmployee(roleCodes) {
    return hasAnyRoleCode(roleCodes, DEPARTMENT_EMPLOYEE_ROLES);
}
export function isSuperManager(roleCodes) {
    return hasAnyRoleCode(roleCodes, TOP_SUPER_ROLES);
}
export function supervisionLevel(roleCodes) {
    if (isSuperManager(roleCodes))
        return 'SUPER';
    if (isDepartmentMinister(roleCodes))
        return 'MINISTER';
    if (isDepartmentEmployee(roleCodes))
        return 'EMPLOYEE';
    return 'UNKNOWN';
}
export function canBeDirectLeader(target, candidate) {
    const targetId = String(target.id || '');
    const candidateId = String(candidate.id || '');
    if (!candidateId || targetId === candidateId)
        return false;
    const level = supervisionLevel(target.roleCodes);
    if (level === 'EMPLOYEE') {
        if (isDepartmentMinister(candidate.roleCodes) && String(candidate.departmentId || '') === String(target.departmentId || '')) {
            return true;
        }
        return false;
    }
    if (level === 'MINISTER') {
        return isSuperManager(candidate.roleCodes);
    }
    if (level === 'SUPER') {
        return isSuperManager(candidate.roleCodes);
    }
    if (isDepartmentMinister(candidate.roleCodes) && String(candidate.departmentId || '') === String(target.departmentId || '')) {
        return true;
    }
    return isSuperManager(candidate.roleCodes);
}
export function canBeIndirectLeader(target, candidate) {
    const targetId = String(target.id || '');
    const candidateId = String(candidate.id || '');
    if (!candidateId || targetId === candidateId)
        return false;
    const level = supervisionLevel(target.roleCodes);
    if (level === 'EMPLOYEE' || level === 'MINISTER') {
        return isSuperManager(candidate.roleCodes);
    }
    if (level === 'SUPER') {
        return isSuperManager(candidate.roleCodes);
    }
    return isSuperManager(candidate.roleCodes);
}
export function ensureSupervisorOrder(directLeaderId, indirectLeaderId) {
    const direct = String(directLeaderId || '');
    const indirect = String(indirectLeaderId || '');
    if (!direct || !indirect)
        return true;
    return direct !== indirect;
}
export function mergeRoleCodes(...items) {
    const set = new Set();
    items.forEach((item) => {
        ;
        (item?.roleCodes || []).forEach((code) => {
            const text = String(code || '').trim().toUpperCase();
            if (text)
                set.add(text);
        });
    });
    return Array.from(set);
}
