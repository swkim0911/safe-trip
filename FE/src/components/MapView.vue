<template>
  <div class="mapview-container">
    <div style="height: 100vh; width: 100%">
    <l-map :useGlobalLeaflet="false" ref="map" v-model:zoom="zoom" :center="[47.41322, -1.219482]" :min-zoom="3" :options="{zoomControl: false}">
      <l-tile-layer
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        layer-type="base"
        name="OpenStreetMap"
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