import VChart from 'vue-echarts';
import { use } from 'echarts/core';
import { CanvasRenderer } from 'echarts/renderers';
import { LineChart, PieChart, BarChart } from 'echarts/charts';
import { GridComponent, TooltipComponent, LegendComponent, TitleComponent } from 'echarts/components';
import { onBeforeUnmount, onMounted, ref } from 'vue';
import * as echarts from 'echarts';
export function setupEcharts(app) {
    use([CanvasRenderer, LineChart, PieChart, BarChart, GridComponent, TooltipComponent, LegendComponent, TitleComponent]);
    app.component('VChart', VChart);
}
export function useECharts() {
    const chartRef = ref(null);
    let instance = null;
    const setOption = (option) => {
        if (!chartRef.value)
            return;
        if (!instance) {
            instance = echarts.init(chartRef.value);
        }
        instance.setOption(option);
    };
    const resize = () => {
        if (instance)
            instance.resize();
    };
    onMounted(() => {
        window.addEventListener('resize', resize);
    });
    onBeforeUnmount(() => {
        window.removeEventListener('resize', resize);
        if (instance) {
            instance.dispose();
            instance = null;
        }
    });
    return { chartRef, setOption };
}
