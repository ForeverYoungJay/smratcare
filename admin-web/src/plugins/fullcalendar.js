import FullCalendar from '@fullcalendar/vue3';
import dayGridPlugin from '@fullcalendar/daygrid';
import interactionPlugin from '@fullcalendar/interaction';
export const calendarPlugins = [dayGridPlugin, interactionPlugin];
export function setupFullCalendar(app) {
    app.component('FullCalendar', FullCalendar);
}
