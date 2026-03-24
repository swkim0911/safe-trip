import { defineStore } from 'pinia'

export const useMapStore = defineStore('map', {
  state: () => ({
    selectedMarker: null,
    flyToTarget: null,
    pendingOpenExternalReportId: null,
    returnToMyFeedback: false,
    pendingReturnToMyFeedback: false,
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
    requestOpenExternalReport(id) {
      this.pendingOpenExternalReportId = id;
      this.returnToMyFeedback = true;
    },
    clearOpenExternalReport() {
      this.pendingOpenExternalReportId = null;
    },
    triggerReturnToMyFeedback() {
      this.pendingReturnToMyFeedback = true;
      this.returnToMyFeedback = false;
    },
    clearPendingReturnToMyFeedback() {
      this.pendingReturnToMyFeedback = false;
    },
  }
})
