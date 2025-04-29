<template>
  <div class="mapview-container">
    <div style="height: 100vh; width: 100%">
      <l-map :useGlobalLeaflet="false" ref="map" v-model:zoom="zoom" :center="[centerOfSeoul.lat, centerOfSeoul.lng]" :min-zoom="3" :options="{zoomControl: false,  maxBoundsViscosity: 1.0}" :max-bounds="[[ -75, -1800 ], [ 85, 1800 ]]">
        <l-tile-layer
          url="https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}{r}.png"
          layer-type="base"
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> &copy; <a href="https://carto.com/">CARTO</a>'
          name="CartoDB Positron"
        ></l-tile-layer>
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
    <ReportModal id="reportModal"/>
    
    <button class="btn btn-primary position-fixed top-0 end-0 mt-4 me-4 shadow-sm login-btn">
      <font-awesome-icon :icon="['fas', 'user-large']" class="icon" />
      LOGIN
    </button>
  </div>
</template>

<script setup>
import {ref} from 'vue'
import "leaflet/dist/leaflet.css";
import { LMap, LTileLayer, LControlZoom } from "@vue-leaflet/vue-leaflet";

import ReportModal from './ReportModal.vue'

const zoom = ref(3);
const centerOfSeoul = ref({ "lat": 37.5665, "lng": 126.9780 });
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