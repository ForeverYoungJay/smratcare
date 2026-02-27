export function toPageResult(list, pageNo = 1, pageSize = 10) {
    return { list, total: list.length, pageNo, pageSize };
}
