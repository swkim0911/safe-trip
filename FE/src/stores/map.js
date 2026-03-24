import { defineStore } from 'pinia'

export const useMapStore = defineStore('map', {
  state: () => ({
    selectedMarker: null,
    flyToTarget: null,
    pendingOpenExternalReportId: null,
    pendingOpenUserReportId: null,
    returnToMyPageTab: null,       // 'feedback' | 'reports' | null
    pendingReturnToMyPageTab: null,
    pendingReopenReportDetail: false,
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
      this.returnToMyPageTab = 'feedback';
    },
    clearOpenExternalReport() {
      this.pendingOpenExternalReportId = null;
    },
    requestOpenUserReport(id) {
      this.pendingOpenUserReportId = id;
    },
    requestOpenUserReportFromEdit(id) {
      this.pendingOpenUserReportId = id;
      this.returnToMyPageTab = 'reports';
    },
    clearOpenUserReport() {
      this.pendingOpenUserReportId = null;
    },
    triggerReturnToMyPage() {
      this.pendingReturnToMyPageTab = this.returnToMyPageTab;
      this.returnToMyPageTab = null;
    },
    clearPendingReturnToMyPage() {
      this.pendingReturnToMyPageTab = null;
    },
    requestReopenReportDetail() {
      this.pendingReopenReportDetail = true;
    },
    clearReopenReportDetail() {
      this.pendingReopenReportDetail = false;
    },
  }
})
