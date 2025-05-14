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
    <button 
    type="button" 
    class="btn btn-danger position-fixed top-0 start-50 translate-middle-x mt-4 shadow-sm report-btn px-3" 
    data-bs-toggle="modal"
    data-bs-target="#reportModal">
      <font-awesome-icon :icon="['fas', 'pen']" class="icon" />
      제보하기
    </button>
    <ReportModal/>
    
    <button class="btn btn-primary position-fixed top-0 end-0 mt-4 me-4 shadow-sm login-btn">
      <font-awesome-icon :icon="['fas', 'user-large']" class="icon" />
      LOGIN
    </button>
  </div>
</template>

<script setup>
import {ref, onMounted, watch} from 'vue'
import "leaflet/dist/leaflet.css";
import { LMap, LTileLayer, LControlZoom, LCircleMarker, LTooltip, LMarker } from "@vue-leaflet/vue-leaflet";
import axios from 'axios'


import ReportModal from './ReportModal.vue'

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
    const response = await axios.get('http://localhost:8080/reports/map-summary', {
      params: {
        zoom: zoom.value
      }
    });

    markers.value = response.data.result.locationSummaryItems;

  } catch (e) {
    console.error('지도 요약 정보 로딩 실패:', e);
  }
};

onMounted(() => {
  loadMapSummary();
})
</script>

<style scoped lang="scss">
  .icon {
    font-size: 95%;
    margin-right: 5px;
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
</style>