import type { App } from 'vue'
import FullCalendar from '@fullcalendar/vue3'
import dayGridPlugin from '@fullcalendar/daygrid'
import interactionPlugin from '@fullcalendar/interaction'

export const calendarPlugins = [dayGridPlugin, interactionPlugin]

export function setupFullCalendar(app: App) {
  app.component('FullCalendar', FullCalendar)
}
