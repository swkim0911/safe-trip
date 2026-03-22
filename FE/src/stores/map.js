import { defineStore } from 'pinia'

export const useMapStore = defineStore('map', {
  state: () => ({
    selectedMarker: null,
    flyToTarget: null,
  }),
  actions: {
    selectMarker(marker, groupBy) {
      this.selectedMarker = { ...marker, groupBy };
    },
    clearMarker() {
      this.selectedMarker = null;
    },
    setFlyTo(lat, lng, zoom = 6) {
      this.flyToTarget = { lat, lng, zoom };
    },
  }
})
