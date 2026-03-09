import { onMounted, onUnmounted } from 'vue';
import { subscribeLiveSync } from '../utils/liveSync';
export function useLiveSyncRefresh(options) {
    const debounceMs = options.debounceMs ?? 500;
    let timer;
    const triggerRefresh = () => {
        if (timer)
            window.clearTimeout(timer);
        timer = window.setTimeout(() => {
            Promise.resolve(options.refresh()).catch(() => { });
        }, debounceMs);
    };
    let unsubscribe = () => { };
    onMounted(() => {
        unsubscribe = subscribeLiveSync((payload) => {
            const hit = payload.topics.some((topic) => options.topics.includes(topic));
            if (hit)
                triggerRefresh();
        });
    });
    onUnmounted(() => {
        unsubscribe();
        if (timer)
            window.clearTimeout(timer);
    });
}
