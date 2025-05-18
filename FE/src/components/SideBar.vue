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
      <div class="sidebar-body" @scroll="onSidebarScroll" ref="sidebarRef">
        <template v-if="viewState === 'country'">
          <ul class="list-group">
            <li
              class="list-group-item d-flex justify-content-between align-items-start"
              v-for="country in sidebarCountries"
              :key="country.id"
              @click="loadSidebarCitySummary(country.id, country.name)"
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

        <template v-else-if="viewState === 'city'">
          <div class="d-flex align-items-center justify-content-center position-relative mb-2">
            <button
              class="btn position-absolute start-0"
              @click="backToList('country')"
            >
              <font-awesome-icon :icon="['fas', 'chevron-left']" />
            </button>

            <h6 class="fw-bold mb-0 text-center"> City of {{ selectedCountry.name }}</h6>
          </div>
          <ul class="list-group">
            <li
              class="list-group-item d-flex justify-content-between align-items-start"
              v-for="city in sidebarCities"
              :key="city.id"
              @click="loadSidebarReportSummary(selectedCountry.id, city.id, city.name)"
            >
              <div class="ms-2 me-auto">{{ city.name }}</div>
              <span class="badge text-bg-primary rounded-pill">{{ city.scamCnt }}</span>
            </li>
          </ul>
        </template>

        <template v-else-if="viewState === 'report'">
          <div class="d-flex align-items-center justify-content-center position-relative mb-2">
            <button
              class="btn position-absolute start-0"
              @click="backToList('city')"
            >
              <font-awesome-icon :icon="['fas', 'chevron-left']" />
            </button>

            <h6 class="fw-bold mb-0 text-center"> Scam of {{ selectedCity.name }}</h6>
          </div>
          <ul class="list-group">
            <li
              class="list-group-item d-flex justify-content-between align-items-start"
              v-for="report in sidebarReports"
              :key="report.reportId"
              @click="openReportDetailModal(report.reportId)"
              data-bs-toggle="modal"
              data-bs-target="#reportDetailModal"
            >
                <div class="ms-2 me-auto">{{ report.title }}</div>
                <span class="badge text-bg-primary rounded-pill">{{ report.scam }}</span>
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
  <ReportDetailModal :report="selectedReport"/>

</template>

<script setup>
import { ref, onMounted, reactive } from 'vue';
import axios from 'axios'
import ReportDetailModal from './ReportDetailModal.vue'

const isOpen = ref(false);
const searchText = ref('');

const viewState = ref('country') // 'country' 또는 'city' 또는 'report'

const sidebarCountries = ref([]);
const sidebarCities = ref([]);
const sidebarReports = ref([]);

const selectedCountry = reactive({
  id: null,
  name: ''
});

const selectedCity = reactive({
  id: null,
  name: ''
});

const selectedReport = reactive({
  title: '',
  scam: '',
  address: '',
  description: '',
  advice: '',
  createdAt: ''
})

const size = 10;

const countryPage = ref(0);
const isLastCountryPage = ref(false);
const isLoadingCountry = ref(false);

const cityPage = ref(0);
const isLastCityPage = ref(false);
const isLoadingCity = ref(false);

const reportPage = ref(0);
const isLastReportPage = ref(false);
const isLoadingReport = ref(false);

const sidebarRef = ref(null);
const serverURL = import.meta.env.VITE_API_URL;

const openReportDetailModal = (reportId) => {
  loadReportDetialInfo(reportId);
};

const loadReportDetialInfo = async (reportId) => {
  try {
    const response = await axios.get(`${serverURL}/reports/${reportId}`);

    const result = response.data.result;
    selectedReport.title = result.title;
    selectedReport.scam = result.scam;
    selectedReport.address = result.address;
    selectedReport.description = result.description;
    selectedReport.advice = result.advice;
    selectedReport.createdAt = result.createdAt;

  } catch (e) {
    console.error('API 요청 실패:', e);
  }
}

const loadSidebarCountrySummary = async (mode = 'click') => {

  if (mode === 'scroll' && (isLoadingCountry.value || isLastCountryPage.value)) return;

  isLoadingCountry.value = true;
  try {
    const response = await axios.get(`${serverURL}/reports/sidebar-summary/counties`, {
      params: {
        page: countryPage.value,
        size: size
      }
    });

    const content = response.data.result.content;
    const last = response.data.result.last;

    sidebarCountries.value.push(...content);
    isLastCountryPage.value = last;
    countryPage.value += 1;
  } catch (e) {
    console.error('API 요청 실패:', e);
  } finally {
    isLoadingCountry.value = false;
  }
}

const loadSidebarCitySummary = async (countryId, countryName, mode = 'click') => {
  if (mode === 'scroll' && (isLoadingCity.value || isLastCityPage.value)) return;

  isLoadingCity.value = true;

  selectedCountry.id = countryId;
  selectedCountry.name = countryName;

  try {
    const response = await axios.get(`${serverURL}/reports/sidebar-summary/cities`, {
      params: {
        countryId: countryId,
        page: cityPage.value,
        size: size
      }
    });
    const content = response.data.result.content;
    const last = response.data.result.last;

    sidebarCities.value.push(...content);
    isLastCityPage.value = last;
    cityPage.value += 1;

    viewState.value = 'city';
  } catch (e) {
    console.error('API 요청 실패:', e);
  } finally {
    isLoadingCity.value = false;
  }
}

const loadSidebarReportSummary = async (countryId, cityId, cityName, mode = 'click') => {
  if (mode === 'scroll' && (isLoadingReport.value || isLastReportPage.value)) return;

  isLoadingReport.value = true;

  selectedCity.id = cityId;
  selectedCity.name = cityName;

  try {
    const response = await axios.get(`${serverURL}/reports/sidebar-summary/reports`, {
      params: {
        countryId: countryId,
        cityId: cityId,
        page: reportPage.value,
        size: size
      }
    });
    const content = response.data.result.content;
    const last = response.data.result.last;

    sidebarReports.value.push(...content);
    isLastReportPage.value = last;
    reportPage.value += 1;

    viewState.value = 'report';
  } catch (e) {
    console.error('API 요청 실패:', e);
  } finally {
    isLoadingReport.value = false;
  }
}


const backToList = (type) => {
  viewState.value = type;
}

const toggleSidebar = () => {
  isOpen.value = !isOpen.value;
};

// 스크롤 감지
const onSidebarScroll = () => {
  const sidebarEl = sidebarRef.value;
  if (!sidebarEl) return;

  const scrollBottom = sidebarEl.scrollTop + sidebarEl.clientHeight >= sidebarEl.scrollHeight - 100;

  if (!scrollBottom) return;
  
  if (viewState.value === 'country') {
    loadSidebarCountrySummary('scroll');
  } else if (viewState.value === 'city') {
    loadSidebarCitySummary(selectedCountry.id, selectedCountry.name, 'scroll');
  } else if (viewState.value === 'report') {
    loadSidebarReportSummary(selectedCountry.id, selectedCity.id, selectedCity.name, 'scroll');
  }
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
