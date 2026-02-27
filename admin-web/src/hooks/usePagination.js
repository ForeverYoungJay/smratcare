import { reactive } from 'vue';
export function usePagination(pageSize = 10) {
    const pagination = reactive({
        current: 1,
        pageSize,
        total: 0,
        showSizeChanger: true,
        showTotal: (total) => `共 ${total} 条`
    });
    const toParams = () => ({
        pageNo: pagination.current,
        pageSize: pagination.pageSize
    });
    return { pagination, toParams };
}
