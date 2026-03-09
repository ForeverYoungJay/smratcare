import dayjs from 'dayjs'

export interface TaskBatchFailure {
  at: string
  action: string
  itemId: string | number
  reason: string
  code: string
  path: string
  retryable: boolean
}

export interface TaskBatchReceipt {
  action: string
  startAt: string
  finishAt: string
  total: number
  success: number
  failed: number
}

export interface TaskBatchErrorDetail {
  reason: string
  code: string
  path: string
  retryable: boolean
}

export interface RunTaskBatchOptions<T> {
  action: string
  rows: T[]
  getItemId: (row: T) => string | number
  execute: (row: T) => Promise<void>
  parseErrorDetail: (error: unknown) => TaskBatchErrorDetail
  onStep?: (ok: boolean, row: T) => void
  now?: () => string
}

export interface RunTaskBatchResult {
  successIds: Array<string | number>
  failedIds: Array<string | number>
  failures: TaskBatchFailure[]
  receipt: TaskBatchReceipt
}

function defaultNow() {
  return dayjs().format('YYYY-MM-DD HH:mm:ss')
}

export async function runTaskBatch<T>(options: RunTaskBatchOptions<T>): Promise<RunTaskBatchResult> {
  const now = options.now || defaultNow
  const startAt = now()
  const successIds: Array<string | number> = []
  const failedIds: Array<string | number> = []
  const failures: TaskBatchFailure[] = []

  for (const row of options.rows) {
    const itemId = options.getItemId(row)
    try {
      await options.execute(row)
      successIds.push(itemId)
      options.onStep?.(true, row)
    } catch (error) {
      failedIds.push(itemId)
      options.onStep?.(false, row)
      const detail = options.parseErrorDetail(error)
      failures.push({
        at: now(),
        action: options.action,
        itemId,
        reason: detail.reason,
        code: detail.code,
        path: detail.path,
        retryable: detail.retryable
      })
    }
  }

  return {
    successIds,
    failedIds,
    failures,
    receipt: {
      action: options.action,
      startAt,
      finishAt: now(),
      total: options.rows.length,
      success: successIds.length,
      failed: failedIds.length
    }
  }
}
