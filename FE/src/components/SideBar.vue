<template>
  <div class="sidebar-container">
    <!-- 사이드바 -->
    <div :class="['sidebar', {'open': isOpen}]">
      <div class="sidebar-header form-group">
        <input
        type="text"
        v-model="searchText"
        placeholder="국가, 도시 검색"
        class="form-control form-control-lg mb-3"
        />
      
      </div>
      <div class="sidebar-body">
        <template v-if="viewState === 'country'">
          <ul class="list-group">
            <li
              class="list-group-item d-flex justify-content-between align-items-start"
              v-for="country in sidebarCountries"
              :key="country.id"
              @click="loadSidbarCitySummary(country.id, country.name)"
            >
              <div class="ms-2 me-auto">
                <div class="fw-bold">{{ country.name }}</div>
              </div>
              <span class="badge text-bg-primary rounded-pill">
                {{ country.scamCnt }}
              </span>
            </li>
          </ul>
        </template>

        <!-- 도시 리스트 -->
        <template v-else-if="viewState === 'city'">
          <div class="d-flex align-items-center justify-content-center position-relative mb-2">
            <button
              class="btn position-absolute start-0"
              @click="backToCountryList"
            >
              <font-awesome-icon :icon="['fas', 'arrow-left']" />
            </button>

            <h6 class="fw-bold mb-0 text-center"> City of {{ selectedCountry }}</h6>
          </div>
          <ul class="list-group">
            <li
              class="list-group-item d-flex justify-content-between align-items-start"
              v-for="city in sidebarCities"
              :key="city.id"
            >
              <div class="ms-2 me-auto">{{ city.name }}</div>
              <span class="badge text-bg-primary rounded-pill">{{ city.scamCnt }}</span>
            </li>
          </ul>
        </template>
      </div>
    </div>

    <!-- 토글 버튼 (사이드바 열기) -->
    <button @click="toggleSidebar" class="toggle-btn">
      <span v-if="!isOpen">▶</span>
      <span v-else>◀</span>
    </button>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios'

const isOpen = ref(false);
const searchText = ref('');

const viewState = ref('country') // 'country' 또는 'city' 또는 'scam'



const sidebarCountries = ref([]);
const sidebarCities = ref([]);
const selectedCountry = ref('');


const loadSidebarCountrySummary = async () => {
  try {
    const response = await axios.get('http://localhost:8080/reports/sidebar-summary/counties', {
      params: {
        page: 0,
        size: 20
      }
    });
    sidebarCountries.value = response.data.result.content;
  } catch (e) {
    console.error('API 요청 실패:', e);
  }
}

const loadSidbarCitySummary = async (countryId, countryName) => {
  selectedCountry.value = countryName;
  try {
    const response = await axios.get(`http://localhost:8080/reports/sidebar-summary/cities`, {
      params: { countryId: countryId }
    });
    sidebarCities.value = response.data.result.content;
    viewState.value = 'city';

  } catch (e) {
    console.error('API 요청 실패:', e);
  }
}

const backToCountryList = () => {
  viewState.value = 'country'
}

const toggleSidebar = () => {
  isOpen.value = !isOpen.value;
};

onMounted(() => {
  loadSidebarCountrySummary();
})

</script>

<style scoped lang="scss">

/* 사이드바 컨테이너 */
.sidebar-container {
  position: fixed;
  display: flex;
  height: 100vh;
  z-index: 1000;
}

/* 사이드바 */
.sidebar {
  position: fixed;
  display: flex;
  top: 0;
  left: -400px; /* 기본적으로 숨김 */
  width: 400px;
  height: 100vh;
  background-color: #e0e0e0bd;
  transition: left 0.3s ease;
  padding: 20px;
  flex-direction: column;
}

/* 사이드바가 열릴 때 */
.sidebar.open {
  left: 0;
}

/* 사이드바 헤더 */
.sidebar-header {
  align-items: center;
  font-size: 18px;
  margin-bottom: 20px;
}

.sidebar-body {
  padding-left: 4px;
  padding-right: 4px;
  overflow-y: auto;
  overscroll-behavior: contain; /* 스크롤 범위를 벗어나면 움직이지 않게 */
}

/* 사이드바가 열렸을 때 토글 버튼 위치 조정 */
.sidebar.open + .toggle-btn {
  left: 400px;
}

.list-group-item {
  background-color: #f8f9fa; /* 배경색 */
  color: black;
  border-radius: 8px; /* 모서리를 둥글게 */
  padding: 12px; /* 내부 여백 */
  margin-bottom: 12px; /* 아래 여백 */
  margin-top: 12px;
  border: 1px solid #ddd; /* 테두리 */
  box-shadow: 2px 2px 5px rgba(0, 0, 0, 0.1); /* 그림자 효과 */
}

/* 토글 버튼 (사이드바 여닫기 버튼) */
.toggle-btn {
  position: fixed;
  top: 50%;
  left: 0;
  transform: translateY(-50%);
  background-color: #0d6efd;
  color: white;
  border: none;
  padding: 10px 15px;
  font-size: 18px;
  cursor: pointer;
  transition: all 0.3s;
}
</style>
