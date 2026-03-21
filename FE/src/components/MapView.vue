<template>
  <div class="mapview-container">
    <div style="height: 100vh; width: 100%">
      <l-map :useGlobalLeaflet="false" ref="map" v-model:zoom="zoom" :center="[center.lat, center.lng]" :min-zoom="3" :options="{zoomControl: false,  maxBoundsViscosity: 1.0}" :max-bounds="[[ -75, -1800 ], [ 85, 1800 ]]" worldCopyJump>
        <l-tile-layer
          url="https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}{r}.png"
          layer-type="base"
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> &copy; <a href="https://carto.com/">CARTO</a>'
          name="CartoDB Positron"
        ></l-tile-layer>
        <l-circle-marker
          v-for="marker in markers"
          :key="marker.id"
          :lat-lng="[marker.lat, marker.lng]"
          :radius="getRadius(marker.scamCnt)"
          :color="getColor(marker.riskLevel)"
          :fill-opacity="0.5"
          :weight="1"
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
            <div class="legend-title">Scam Activity</div>
            <div class="legend-item"><span class="dot high"></span> HIGH</div>
            <div class="legend-item"><span class="dot medium"></span> MEDIUM</div>
            <div class="legend-item"><span class="dot low"></span> LOW</div>
          </div>
        </l-control>
      </l-map>
    </div>  
    <div v-if="!isLoggedIn" >
      <button 
        type="button" 
        class="btn btn-primary position-fixed top-0 end-0 mt-4 me-4 shadow-sm login-btn"
        @click="openAuthModal"
      >
        <font-awesome-icon :icon="['fas', 'user-large']" class="icon" />
        LOGIN
      </button>
    </div>
    <div v-else class="dropdown">
      <button 
        type="button" 
        class="btn btn-danger position-fixed top-0 start-50 translate-middle-x mt-4 shadow-sm report-btn px-3" 
        @click="openReportFormModal"
        >
          <font-awesome-icon :icon="['fas', 'pen']" class="icon" />
          Report
      </button>
      <ReportFormModal/>
      <button
        class="btn btn-primary dropdown-toggle position-fixed top-0 end-0 mt-4 me-4 shadow-sm dropdown-btn"
        type="button"
        data-bs-toggle="dropdown"
        aria-expanded="false"
      >
        <font-awesome-icon :icon="['fas', 'user-large']" class="icon" />  
        {{ nickname }}
      </button>
      <ul class="dropdown-menu">
        <li>
          <button class="dropdown-item" @click="logout">
            <font-awesome-icon icon="fa-solid fa-arrow-right-from-bracket" class="icon"/>
            Logout
          </button>
        </li>
      </ul>   
    </div>
    <AuthFormModal/>
  </div>
</template>

<script setup>
import { useAuthStore } from '@/stores/auth';

import { ref, onMounted, watch, computed } from 'vue';
import { LMap, LTileLayer, LControlZoom, LCircleMarker, LTooltip, LMarker, LControl } from "@vue-leaflet/vue-leaflet";
import { useBootstrapModal } from '@/composables/useBootstrapModal';
import ReportFormModal from './ReportFormModal.vue';
import AuthFormModal from './AuthFormModal.vue';
import apiClient from '@/api/apiClient';
import "leaflet/dist/leaflet.css";

const { show: openAuthModal } = useBootstrapModal('#authFormModal');
const { show: openReportFormModal } = useBootstrapModal('#reportFormModal');

const authStore = useAuthStore();
const isLoggedIn = computed(() => !!authStore.accessToken); // 로그인 여부
const nickname = computed(() => authStore.user?.nickname || 'user');

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

watch(zoom, (newZoom, oldZoom) => {
  const prevGroup = getGroupByZoom(oldZoom);
  const currGroup = getGroupByZoom(newZoom);

  console.log(zoom.value);

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
  if (riskLevel === 'HIGH') return '#e74c3c'
  if (riskLevel === 'MEDIUM') return '#f39c12'
  if (riskLevel === 'LOW') return '#2ecc71'
  return '#95a5a6'
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
  .icon {
    font-size: 95%;
    margin-right: 1px;
  }

  .login-btn {
    z-index: 1000; /* 다른 요소보다 위에 뜨도록 */
    border: none;
    padding: 10px 14px;
    border-radius: 10px;
    font-size: 18px;
  }

  .report-btn{
    z-index: 1000; /* 다른 요소보다 위에 뜨도록 */
    border: none;
    padding: 10px 14px;
    border-radius: 10px;
    font-size: 19px;
  }

  .dropdown-btn{
    z-index: 1000; /* 다른 요소보다 위에 뜨도록 */
    border: none;
    padding: 10px 14px;
    border-radius: 10px;
    font-size: 19px;
  }

  .dropdown-menu {
  background-color: white;
}
  .dropdown-item {
    padding: 10px 16px;
    font-size: 16px;
    color: black;
    font-weight: 500;
    border-bottom: 1px solid black;
    transition: background-color 0.15s ease-in-out;
  }

.dropdown-item:last-child {
  border-bottom: none;
}

.dropdown-item:hover {
  background-color: #f1f3f5;
  color: #000;
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
  background: white;
  padding: 8px 12px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
  font-size: 12px;
  line-height: 1.6;
}

.legend-title {
  font-weight: 700;
  margin-bottom: 4px;
  font-size: 11px;
  text-transform: uppercase;
  color: #555;
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

  &.high   { background-color: #e74c3c; }
  &.medium { background-color: #f39c12; }
  &.low    { background-color: #2ecc71; }
}

.risk-badge {
  font-size: 11px;
  font-weight: 600;
  padding: 1px 5px;
  border-radius: 4px;
  color: white;

  &.high   { background-color: #e74c3c; }
  &.medium { background-color: #f39c12; }
  &.low    { background-color: #2ecc71; }
}
</style>