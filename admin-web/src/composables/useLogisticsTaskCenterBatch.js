import dayjs from 'dayjs';
function defaultNow() {
    return dayjs().format('YYYY-MM-DD HH:mm:ss');
}
export async function runTaskBatch(options) {
    const now = options.now || defaultNow;
    const startAt = now();
    const successIds = [];
    const failedIds = [];
    const failures = [];
    for (const row of options.rows) {
        const itemId = options.getItemId(row);
        try {
            await options.execute(row);
            successIds.push(itemId);
            options.onStep?.(true, row);
        }
        catch (error) {
            failedIds.push(itemId);
            options.onStep?.(false, row);
            const detail = options.parseErrorDetail(error);
            failures.push({
                at: now(),
                action: options.action,
                itemId,
                reason: detail.reason,
                code: detail.code,
                path: detail.path,
                retryable: detail.retryable
            });
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
    };
}
