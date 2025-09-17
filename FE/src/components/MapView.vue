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
          :radius="getRadius(marker.scamCnt, zoom)"
          color="#ff9500"
          :fill-opacity="0.5"
          :weight="1"
        >
          <l-tooltip :options="{ permanent: false, direction: 'auto'}">
            {{ marker.scamCnt }}
          </l-tooltip>
        </l-circle-marker>
        <l-control-zoom position="bottomright"></l-control-zoom>
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
import { LMap, LTileLayer, LControlZoom, LCircleMarker, LTooltip, LMarker } from "@vue-leaflet/vue-leaflet";
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

watch(zoom, (newZoom, oldZoom) => {
  const prevGroup = oldZoom >= 7 ? 'city' : 'country';
  const currGroup = newZoom >= 7 ? 'city' : 'country';

  if (prevGroup !== currGroup) {
    loadMapSummary();
  }
})


const getRadius = (scamCnt, zoom) => {
  if (zoom <= 4) return Math.sqrt(scamCnt) * 15
  if (zoom <= 6) return Math.sqrt(scamCnt) * 20
  if (zoom <= 8) return Math.sqrt(scamCnt) * 25
  return scamCnt * 30
}

const loadMapSummary = async () => {
  try {
    const response = await apiClient.get('/reports/map-summary', {
      params: {
        zoom: zoom.value
      }
    });

    markers.value = response.data.result.locationSummaryItems;

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

const restoreSession = async () => {
  if (!authStore.accessToken) {
    try {
      const response = await apiClient.post('/auth/refresh', {}, { withCredentials: true });

      // 서버가 204 No Content를 반환한 경우 비로그인 상태 유지
      if (response.status === 204) return;

      authStore.setAccessToken(response.result.accessToken);

      // accessToken 얻었으니 사용자 정보 요청
      const { data: meResponse } = await apiClient.get('/me');
      authStore.setUser(meResponse.result);
    } catch {
      // refreshToken 없거나 만료된 상태 -> 아무것도 하지 않음.
    }
  }
}

onMounted(() => {
  loadMapSummary(),
  restoreSession()
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
</style>