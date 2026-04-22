import { defineStore } from 'pinia';
import { getPagePermissions, getPermissions, getRoles, getStaffInfo, getToken, setPagePermissions, setPermissions, setRoles, setStaffInfo, setToken, clearPagePermissions, clearPermissions, clearRoles, clearStaffInfo, clearToken, normalizeRoles } from '../utils/auth';
import { getRecommendedPagePermissions, normalizePagePermissions } from '../utils/pageAccess';
export const useUserStore = defineStore('user', {
    state: () => ({
        token: getToken(),
        roles: getRoles(),
        permissions: getPermissions(),
        pagePermissions: getPagePermissions(),
        staffInfo: getStaffInfo()
    }),
    actions: {
        setStaffProfile(staffInfo) {
            this.staffInfo = staffInfo;
            if (staffInfo) {
                setStaffInfo(staffInfo);
            }
            else {
                clearStaffInfo();
            }
        },
        setAuth(payload) {
            const roles = normalizeRoles(payload.roles || []);
            const inheritedPagePermissions = roles.flatMap((role) => getRecommendedPagePermissions(role));
            const pagePermissions = normalizePagePermissions([
                ...(payload.pagePermissions || []),
                ...inheritedPagePermissions
            ]);
            this.token = payload.token;
            this.roles = roles;
            this.permissions = payload.permissions || [];
            this.pagePermissions = pagePermissions;
            this.setStaffProfile(payload.staffInfo);
            setToken(payload.token);
            setRoles(roles);
            setPermissions(payload.permissions || []);
            setPagePermissions(pagePermissions);
        },
        clear() {
            this.token = '';
            this.roles = [];
            this.permissions = [];
            this.pagePermissions = [];
            this.setStaffProfile(null);
            clearToken();
            clearRoles();
            clearPermissions();
            clearPagePermissions();
        }
    }
});
