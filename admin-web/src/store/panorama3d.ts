import { defineStore } from 'pinia'
import { ref } from 'vue'

export const usePanoramaStore = defineStore('panorama3d', () => {
  const currentLevel = ref<'PARK' | 'BUILDING' | 'FLOOR' | 'ROOM'>('PARK')
  const selectedBuilding = ref('')
  const selectedFloor = ref('')
  const selectedRoom = ref('')

  function setLevel(level: 'PARK' | 'BUILDING' | 'FLOOR' | 'ROOM') {
    currentLevel.value = level
  }

  function goUpLevel() {
    if (currentLevel.value === 'ROOM') {
      currentLevel.value = 'FLOOR'
      selectedRoom.value = ''
    } else if (currentLevel.value === 'FLOOR') {
      currentLevel.value = 'BUILDING'
      selectedFloor.value = ''
    } else if (currentLevel.value === 'BUILDING') {
      currentLevel.value = 'PARK'
      selectedBuilding.value = ''
    }
  }

  function resetCamera() {
    currentLevel.value = 'PARK'
    selectedBuilding.value = ''
    selectedFloor.value = ''
    selectedRoom.value = ''
  }

  return {
    currentLevel,
    selectedBuilding,
    selectedFloor,
    selectedRoom,
    setLevel,
    goUpLevel,
    resetCamera
  }
})
