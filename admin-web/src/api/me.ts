import request from '../utils/request'

/** 我的办公 · 本人：一律由服务端登录身份确定员工，不传员工 ID */
export interface MyProfile {
  staffId: string
  staffNo?: string
  username?: string
  realName?: string
  phone?: string
  email?: string
  departmentName?: string
  onJob?: boolean
  lastLoginTime?: string
  jobTitle?: string
  employmentType?: string
  hireDate?: string
  qualificationLevel?: string
  contractType?: string
  contractStatus?: string
  contractStartDate?: string
  contractEndDate?: string
  emergencyContactName?: string
  emergencyContactPhone?: string
  certificates: { name?: string; certificateNo?: string; validUntil?: string; status?: string }[]
}
export interface MyLogin { time: string; type?: string; result?: string; failReason?: string; ip?: string; userAgent?: string }

export const getMyProfile = () => request.get<MyProfile>('/api/me/profile')
export const getMyLogins = () => request.get<MyLogin[]>('/api/me/logins')
export const changeMyPassword = (data: { oldPassword: string; newPassword: string }) => request.post<void>('/api/me/password', data)

/** 本年假期余额：额度由人事维护，已用天数来自已批准的请假审批 */
export const getLeaveBalance = () =>
  request.get<{ leaveType: string; quotaDays: number; usedDays: number; remainingDays: number }[]>('/api/me/leave-balance')
/** 我的积分：余额、全院排名、积分兑现申请记录 */
export const getPointsSummary = () =>
  request.get<{ balance: number; rank?: number | string | null; rankTotal?: number | string; exchanges: { title: string; amount?: number; status: string; time: string }[] }>('/api/me/points-summary')
