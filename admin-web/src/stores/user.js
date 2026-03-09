import { defineStore } from 'pinia';
import { getPermissions, getRoles, getStaffInfo, getToken, setPermissions, setRoles, setStaffInfo, setToken, clearPermissions, clearRoles, clearStaffInfo, clearToken, normalizeRoles } from '../utils/auth';
export const useUserStore = defineStore('user', {
    state: () => ({
        token: getToken(),
        roles: getRoles(),
        permissions: getPermissions(),
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
            this.token = payload.token;
            this.roles = roles;
            this.permissions = payload.permissions || [];
            this.setStaffProfile(payload.staffInfo);
            setToken(payload.token);
            setRoles(roles);
            setPermissions(payload.permissions || []);
        },
        clear() {
            this.token = '';
            this.roles = [];
            this.permissions = [];
            this.setStaffProfile(null);
            clearToken();
            clearRoles();
            clearPermissions();
        }
    }
});
