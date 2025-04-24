<template>
  <div class="mapview-container">
    <div style="height: 100vh; width: 100%">
    <l-map :useGlobalLeaflet="false" ref="map" v-model:zoom="zoom" :center="[centerOfSeoul.lat, centerOfSeoul.lng]" :min-zoom="3" :options="{zoomControl: false,  maxBoundsViscosity: 1.0}" :max-bounds="[[ -75, -1800 ], [ 85, 1800 ]]">
      <l-tile-layer
        url="https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}{r}.png"
        layer-type="base"
        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors &copy; <a href="https://carto.com/">CARTO</a>'
        name="CartoDB Positron"
      ></l-tile-layer>
      <l-control-zoom position="bottomright"></l-control-zoom>
    </l-map>
  </div>
    <button class="btn btn-primary shadow-sm login-btn">
      <font-awesome-icon :icon="['fas', 'user-large']" class="login-icon" />
      LOGIN
    </button>
  </div>
</template>

<script setup>
import {ref} from 'vue'
import "leaflet/dist/leaflet.css";
import { LMap, LTileLayer, LControlZoom } from "@vue-leaflet/vue-leaflet";

const zoom = ref(3);
const centerOfSeoul = ref({ "lat": 37.5665, "lng": 126.9780 });

</script>

<style scoped lang="scss">
  .login-icon {
    font-size: 95%;
    margin-right: 5px;
  }
  .login-btn {
    position: fixed;
    top: 20px;
    right: 20px;
    z-index: 1000; /* 다른 요소보다 위에 뜨도록 */
    border: none;
    padding: 10px 14px;
    border-radius: 10px;
    font-size: 18px;
  }
</style>