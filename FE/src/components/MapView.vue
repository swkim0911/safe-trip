<template>
  <div class="mapview-container">
    <div style="height: calc(100vh - 28px); width: 100%">
      <l-map :useGlobalLeaflet="false" ref="map" v-model:zoom="zoom" :center="[center.lat, center.lng]" :min-zoom="3" :options="{zoomControl: false,  maxBoundsViscosity: 1.0}" :max-bounds="[[ -75, -1800 ], [ 85, 1800 ]]" worldCopyJump>
        <l-tile-layer
          url="https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png"
          layer-type="base"
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> &copy; <a href="https://carto.com/">CARTO</a>'
          name="CartoDB Voyager"
        ></l-tile-layer>
        <l-circle-marker
          v-for="marker in markers"
          :key="marker.id"
          :lat-lng="[marker.lat, marker.lng]"
          :radius="getRadius(marker.scamCnt)"
          :color="getColor(marker.riskLevel)"
          :fill-opacity="0.5"
          :weight="1"
          @click="mapStore.selectMarker(marker, getGroupByZoom(zoom))"
        >
          <l-tooltip :options="{ direction: 'top', offset: [0, -5] }">
            <div class="tooltip-card">
              <div class="tooltip-name">{{ marker.name }}</div>
              <div class="tooltip-bottom">
                <strong>{{ marker.scamCnt }}</strong> reports
                <span class="risk-badge" :class="marker.riskLevel?.toLowerCase()">{{ marker.riskLevel }}</span>
              </div>
            </div>
          </l-tooltip>
        </l-circle-marker>
        <l-control-zoom position="bottomright"></l-control-zoom>
        <l-control position="bottomleft">
          <div class="map-legend">
            <div class="legend-title">Report Activity</div>
            <div class="legend-item"><span class="dot high"></span> Hotspot</div>
            <div class="legend-item"><span class="dot medium"></span> Moderate</div>
            <div class="legend-item"><span class="dot low"></span> Quiet</div>
          </div>
        </l-control>
      </l-map>
    </div>  
    <div class="top-actions position-fixed top-0 end-0 mt-4 me-4 d-flex gap-2" style="z-index: 1000;">
      <div v-if="!isLoggedIn">
        <button
          type="button"
          class="btn top-action-btn login-btn"
          @click="openAuthModal"
        >
          <font-awesome-icon :icon="['fas', 'user-large']" class="icon" />
          Log in
        </button>
      </div>
      <template v-else>
        <button
          type="button"
          class="btn top-action-btn report-btn"
          @click="openReportFormModal"
        >
          <font-awesome-icon :icon="['fas', 'pen']" class="icon" />
          Report
        </button>
        <div class="dropdown">
          <button
            class="btn top-action-btn dropdown-toggle dropdown-btn"
            type="button"
            data-bs-toggle="dropdown"
            aria-expanded="false"
          >
            <font-awesome-icon :icon="['fas', 'user-large']" class="icon" />
            {{ nickname }}
          </button>
          <ul class="dropdown-menu dropdown-menu-end account-menu">
            <li>
              <button class="dropdown-item" @click="openMyPageModal">
                <font-awesome-icon :icon="['fas', 'user-gear']" class="menu-icon"/>
                My Page
              </button>
            </li>
            <li v-if="isAdmin">
              <button class="dropdown-item" @click="openAdminModal">
                <font-awesome-icon :icon="['fas', 'shield-halved']" class="menu-icon"/>
                Admin
              </button>
            </li>
            <li><hr class="dropdown-divider"></li>
            <li>
              <button class="dropdown-item" @click="logout">
                <font-awesome-icon icon="fa-solid fa-arrow-right-from-bracket" class="menu-icon"/>
                Logout
              </button>
            </li>
          </ul>
        </div>
      </template>
    </div>
    <ReportFormModal/>
    <AuthFormModal/>
    <MyPageModal ref="myPageModalRef" @edit-report="handleEditReport"/>
    <ReportEditModal :report="editingReport" @updated="handleReportUpdated"/>
    <AdminModal ref="adminModalRef"/>
    <AppToast/>
    <div class="map-footer">
      Data sourced from Reddit via the official API. All content belongs to the respective authors.
      &nbsp;·&nbsp; © 2026 SafeTrip &nbsp;·&nbsp;
      <a href="mailto:safetripworld.contact@gmail.com">Contact</a>
    </div>
  </div>
</template>

<script setup>
import { useAuthStore } from '@/stores/auth';
import { useMapStore } from '@/stores/map';

import { ref, onMounted, watch, computed } from 'vue';
import { LMap, LTileLayer, LControlZoom, LCircleMarker, LTooltip, LControl } from "@vue-leaflet/vue-leaflet";
import { useBootstrapModal } from '@/composables/useBootstrapModal';
import ReportFormModal from './ReportFormModal.vue';
import AuthFormModal from './AuthFormModal.vue';
import MyPageModal from './MyPageModal.vue';
import ReportEditModal from './ReportEditModal.vue';
import AdminModal from './AdminModal.vue';
import AppToast from './AppToast.vue';
import apiClient from '@/api/apiClient';
import "leaflet/dist/leaflet.css";

const { show: openAuthModal } = useBootstrapModal('#authFormModal');
const { show: openReportFormModal } = useBootstrapModal('#reportFormModal');
const { show: openMyPageModal } = useBootstrapModal('#myPageModal');
const { show: openReportEditModal } = useBootstrapModal('#reportEditModal');
const { show: showAdminModal } = useBootstrapModal('#adminModal');

const myPageModalRef = ref(null);
const adminModalRef = ref(null);
const editingReport = ref(null);

const openAdminModal = () => {
  adminModalRef.value?.load();
  showAdminModal();
};

const handleEditReport = (report) => {
  editingReport.value = report;
  openReportEditModal();
};

const handleReportUpdated = (reportId) => {
  mapStore.requestOpenUserReportFromEdit(reportId);
};

const authStore = useAuthStore();
const mapStore = useMapStore();

watch(() => mapStore.pendingReturnToMyPageTab, (tab) => {
  if (!tab) return;
  myPageModalRef.value?.openOnTab(tab);
  openMyPageModal();
  mapStore.clearPendingReturnToMyPage();
});
const isLoggedIn = computed(() => !!authStore.accessToken); // 로그인 여부
const isAdmin = computed(() => authStore.user?.role === 'ADMIN');
const nickname = computed(() => authStore.user?.nickname || 'user');

const map = ref(null);
const zoom = ref(3);
const center = ref({ "lat": 42.8333, "lng": 12.8333 });

const markers = ref([]);

// maxCnt / minCnt를 computed로 미리 구해두기 (반경 계산에만 사용)
const maxCnt = computed(() => Math.max(...markers.value.map(m => m.scamCnt)))
const minCnt = computed(() => Math.min(...markers.value.map(m => m.scamCnt)))

function getGroupByZoom(zoom) {
  if (zoom >= 9) {
    return 'city';
  }else if (zoom >= 6) {
    return 'state';
  } else {
    return 'country';
  }
}

watch(() => mapStore.flyToTarget, (target) => {
  if (!target || !map.value?.leafletObject) return;
  map.value.leafletObject.flyTo([target.lat, target.lng], target.zoom);
});

watch(zoom, (newZoom, oldZoom) => {
  const prevGroup = getGroupByZoom(oldZoom);
  const currGroup = getGroupByZoom(newZoom);

  if (prevGroup !== currGroup) {
    loadMapSummary();
  }
})

const getRadius = (scamCnt) => {
  const minSize = 4
  const maxSize = 40

  if (!maxCnt.value || maxCnt.value === minCnt.value) return minSize

  const normalized = (Math.sqrt(scamCnt) - Math.sqrt(minCnt.value)) /
                     (Math.sqrt(maxCnt.value) - Math.sqrt(minCnt.value))

  const zoomFactor = 1 + (zoom.value - 4) * 0.2
  let radius = minSize + normalized * (maxSize - minSize) * zoomFactor

  if (zoom.value >= 9 && scamCnt <= 1) {
    radius = Math.max(radius, 10)
  }

  return radius
}

const getColor = (riskLevel) => {
  if (riskLevel === 'HIGH') return '#D97757'
  if (riskLevel === 'MEDIUM') return '#E9B872'
  if (riskLevel === 'LOW') return '#66AFA3'
  return '#A8A29E'
}

const loadMapSummary = async () => {
  try {
    const response = await apiClient.get('/map/overview', {
      params: {
        zoom: zoom.value
      }
    });
    markers.value = response.data.result.items;

  } catch (e) {
    console.error('Failed to load map summary information:', e);
  }
};

const logout = async () => {
  try {
    await apiClient.post('/auth/logout', {}, { withCredentials: true }); // 쿠키로 refresh token 전달

    authStore.clearAccessToken();
    authStore.clearUser();

  } catch (error) {
    console.error('Logout failed', error);
  }
}
let refreshPromise = null;

const restoreSession = async () => {
  if (!authStore.accessToken) {
    if (!refreshPromise) {
      refreshPromise = (async () => {
        try {
          const response = await apiClient.post('/auth/refresh', {}, { withCredentials: true });
          if (response.status !== 204) {
            authStore.setAccessToken(response.data.result.accessToken);
            const meResponse = await apiClient.get('/users/me');
            if (meResponse) authStore.setUser(meResponse.data.result);
          }
        } catch (err) {
          console.error("restoreSession failed:", err);
        } finally {
          refreshPromise = null; // 끝나면 초기화
        }
      })();
    }
    return refreshPromise; // 다른 호출은 같은 Promise 반환
  }
};

onMounted(() => {
  loadMapSummary();
  restoreSession();
})
</script>

<style scoped lang="scss">
  .map-footer {
    height: 28px;
    padding: 4px 12px;
    font-size: 11px;
    color: #888;
    background: #f8f9fa;
    text-align: center;
    border-top: 1px solid #e9ecef;
    a {
      color: #888;
      text-decoration: none;
      &:hover { color: #444; }
    }
  }

  .icon {
    font-size: 95%;
    margin-right: 1px;
  }

  .top-actions {
    align-items: center;
  }

  .top-action-btn {
    background: var(--safetrip-primary);
    border-color: var(--safetrip-primary);
    border: none;
    padding: 9px 17px;
    border-radius: 999px;
    color: #fff;
    font-size: 0.95rem;
    font-weight: 800;
    letter-spacing: 0.01em;
    box-shadow: 0 8px 22px rgba(42, 157, 143, 0.24);
    transition: transform 0.15s ease, box-shadow 0.15s ease;

    &:hover {
      color: #fff;
      background: var(--safetrip-primary-hover);
      border-color: var(--safetrip-primary-hover);
      transform: translateY(-1px);
      box-shadow: 0 10px 26px rgba(42, 157, 143, 0.3) !important;
    }

    &:active {
      color: #fff;
      background: var(--safetrip-primary-active);
      border-color: var(--safetrip-primary-active);
      transform: translateY(0);
    }
  }

  .report-btn {
    background: #fffdf8;
    color: var(--safetrip-primary);
    border: 1px solid #b9e4dc;

    &:hover,
    &:active {
      color: #fff;
    }
  }

  .dropdown-menu.account-menu {
    min-width: 190px;
    border: 1px solid var(--safetrip-border);
    border-radius: 14px;
    background: #fffdf8;
    box-shadow: 0 16px 34px rgba(36, 49, 58, 0.14);
    overflow: hidden;
    padding: 8px;
  }

  .dropdown-item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 10px 12px;
    color: var(--safetrip-text);
    font-size: 0.95rem;
    font-weight: 800;
    border-radius: 10px;
    transition: background-color 0.15s ease, color 0.15s ease;
  }

.dropdown-item:hover {
  background-color: var(--safetrip-primary-soft);
  color: var(--safetrip-primary);
}

.menu-icon {
  width: 18px;
  color: var(--safetrip-muted);
}

.dropdown-item:hover .menu-icon {
  color: var(--safetrip-primary);
}

.dropdown-divider {
  border-top-color: var(--safetrip-border);
  margin: 6px 0;
}

.tooltip-card {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 100px;
}

.tooltip-name {
  font-weight: 700;
  font-size: 13px;
}

.tooltip-bottom {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
}

.map-legend {
  background: rgba(255, 255, 255, 0.94);
  padding: 8px 12px;
  border-radius: 8px;
  border: 1px solid var(--safetrip-border);
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.1);
  font-size: 12px;
  line-height: 1.6;
}

.legend-title {
  font-weight: 700;
  margin-bottom: 4px;
  font-size: 11px;
  text-transform: uppercase;
  color: var(--safetrip-muted);
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.dot {
  display: inline-block;
  width: 10px;
  height: 10px;
  border-radius: 50%;

  &.high   { background-color: var(--safetrip-activity-many); }
  &.medium { background-color: var(--safetrip-activity-some); }
  &.low    { background-color: var(--safetrip-activity-few); }
}

.risk-badge {
  font-size: 11px;
  font-weight: 600;
  padding: 1px 5px;
  border-radius: 4px;
  color: white;

  &.high   { background-color: var(--safetrip-activity-many); }
  &.medium { background-color: var(--safetrip-activity-some); }
  &.low    { background-color: var(--safetrip-activity-few); }
}
</style>
