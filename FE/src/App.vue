<script setup>
import SideBar from './components/SideBar.vue';
import MapView from './components/MapView.vue';
import axios from 'axios'
import {ref,onMounted} from 'vue'

const sidebarCountries = ref([]);
const sidebarCities = ref([]);

const loadSidebarCountrySummary = async () => {
  try {
  const response = await axios.get('http://localhost:8080/reports/sidebar-summary/counties', {
    params: {
      page: 0,
      size: 10
    }
  });
  sidebarCountries.value = response.data.result.LocationSummaryItem;
  console.log(sidebarCountries.value);

} catch (e) {
  console.error('API 요청 실패:', e);
}
}

onMounted(() => {
  loadSidebarCountrySummary();
})

</script>


<template>
  <SideBar/>
  <MapView/>
</template>

<style scoped lang="scss">

</style>
