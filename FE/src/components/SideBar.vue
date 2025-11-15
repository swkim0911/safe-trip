<template>
  <div class="sidebar-container">
    <div :class="['sidebar', {'open': isOpen}]">
      <!-- 검색창 -->
      <div class="sidebar-header form-group">
        <input
        type="text"
        v-model="searchText"
        placeholder="Country or State or City"
        class="form-control form-control-lg mb-3"
        />
      </div>

      <div class="sidebar-body" @scroll="onSidebarScroll" ref="sidebarRef">
        <template v-if="viewType === 'country'">
          <ul class="list-group">
            <li
              class="list-group-item d-flex justify-content-between align-items-start"
              v-for="country in sidebarCountries"
              :key="country.id"
              @click="loadCountryDetail(country.id, country.name, country.scamCnt)"
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

        <template v-else-if="viewType === 'state'">
          <div class="d-flex align-items-center justify-content-center position-relative mb-3">
            <button
              class="btn position-absolute start-0"
              @click="backToList('country')"
            >
              <font-awesome-icon :icon="['fas', 'chevron-left']" />
            </button>
            <h6 class="fw-bold mb-0 text-center">{{ selectedCountry.name }}</h6>
          </div>

          <!-- 상단: 국가의 모든 리포트 보기 버튼 -->
          <div class="mb-3">
            <button 
              class="btn btn-primary w-100 d-flex align-items-center justify-content-between p-3"
              @click="loadCountryAllReports(selectedCountry.id, selectedCountry.name)"
              style="border-radius: 8px;"
            >
              <span class="d-flex align-items-center">
                View All Scam Reports
              </span>
              <span class="badge bg-light text-dark">
                {{ selectedCountry.scamCnt }} reports
              </span>
            </button>
          </div>

          <!-- 하단: State 목록 -->
          <div>
            <h6 class="fw-bold mb-2">States</h6>
            <ul class="list-group">
              <li
                class="list-group-item d-flex justify-content-between align-items-start"
                v-for="state in sidebarStates"
                :key="state.id"
                @click="loadSidebarCityStatistics(state.id, state.name)"
              >
                <div class="ms-2 me-auto">
                  <div class="fw-bold">{{ state.name }}</div>
                </div>
                <span class="badge text-bg-primary rounded-pill">{{ state.scamCnt }}</span>
              </li>
            </ul>
          </div>
        </template>


        <template v-else-if="viewType === 'city'">
          <div class="d-flex align-items-center justify-content-center position-relative mb-3">
            <button
              class="btn position-absolute start-0"
              @click="backToList('state')"
            >
              <font-awesome-icon :icon="['fas', 'chevron-left']" />
            </button>
            <h6 class="fw-bold mb-0 text-center">City of {{ selectedState.name }}</h6>
          </div>

          <!-- 상단: 국가의 모든 리포트 보기 버튼 -->
          <div class="mb-3">
            <button 
              class="btn btn-primary w-100 d-flex align-items-center justify-content-between p-3"
              @click="loadStateAllReports(selectedState.id, selectedState.name)"
              style="border-radius: 8px;"
            >
              <span class="d-flex align-items-center">
                View All Reports in {{ selectedState.name }}
              </span>
              <span class="badge bg-light text-dark">
                {{ selectedState.scamCnt || 0 }} reports
              </span>
            </button>
          </div>

          <ul class="list-group">
            <li
              class="list-group-item d-flex justify-content-between align-items-start"
              v-for="city in sidebarCities"
              :key="city.id"
              @click="loadSidebarReportSummary(city.id, city.name)"
            >
              <div class="ms-2 me-auto">
                <div class="fw-bold">
                  {{ city.name }}
                </div>
              </div>
              <span class="badge text-bg-primary rounded-pill">{{ city.scamCnt }}</span>
            </li>
          </ul>
        </template>

        <template v-else-if="viewType === 'report'">
          <div class="d-flex align-items-center justify-content-center position-relative mb-2">
            <button
              class="btn position-absolute start-0"
              @click="backToPreviousView"
            >
              <font-awesome-icon :icon="['fas', 'chevron-left']" />
            </button>

            <h6 class="fw-bold mb-0 text-center">
              <span v-if="selectedCity.id">Scam of {{ selectedCity.name }}</span>
              <span v-else-if="selectedState.id">Scam of {{ selectedState.name }}</span>
              <span v-else>Scam of {{ selectedCountry.name }}</span>
            </h6>
          </div>
          <ul class="list-group">
            <li
              class="list-group-item position-relative"
              v-for="report in sidebarReports"
              :key="report.reportId"
              @click="openReportDetailModal(report.source, report.reportId)"
            >
              <span class="badge bg-primary position-absolute top-0 end-0 translate-middle-y me-2">
                {{ report.source === 'SAFETRIP' ? 'SAFETRIP' : '🤖 AI Bot' }}
              </span>

              <div class="fw-bold mb-1 mt-3">
                {{ report.title }}
              </div>

              <div class="mb-1">
                <span class="badge text-bg-danger me-1">{{ report.scamAction }}</span>
                <span class="badge text-bg-warning">{{ report.scamContext }}</span>
              </div>

              <div class="text-end">
                <small class="text-muted">{{ formatDate(report.posted_at) }}</small>
              </div>
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
import { useBootstrapModal } from '@/composables/useBootstrapModal';
import ReportDetailModal from './ReportDetailModal.vue'
import apiClient from '@/api/apiClient';
import dayjs from 'dayjs'

const { show } = useBootstrapModal('#reportDetailModal');

const isOpen = ref(false);
const searchText = ref('');

const viewType = ref('country') // 'country' 또는 'state' 또는 'city' 또는 'report'

const sidebarCountries = ref([]);
const sidebarStates = ref([]);
const sidebarCities = ref([]);
const sidebarReports = ref([]);

const selectedCountry = reactive({
  id: null,
  name: '',
  scamCnt: 0
});

const selectedState = reactive({
  id: null,
  name: '',
  scamCnt: 0
});

const selectedCity = reactive({
  id: null,
  name: ''
});

const selectedReport = reactive({
  source: '',
  sourceUrl: '',
  author: '',
  nickname: '',
  scamAction: '',
  scamContext: '',
  countryName: '',
  stateName: '',
  cityName: '',
  title: '',
  content: '',
  postedAt: '',
  imageUrls: [],
});

const size = 12;

const countryPage = ref(0);
const isLastCountryPage = ref(false);
const isLoadingCountry = ref(false);

const statePage = ref(0);
const isLastStatePage = ref(false);
const isLoadingState = ref(false);

const cityPage = ref(0);
const isLastCityPage = ref(false);
const isLoadingCity = ref(false);

const reportPage = ref(0);
const isLastReportPage = ref(false);
const isLoadingReport = ref(false);

const sidebarRef = ref(null);

const formatDate = (date) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

const openReportDetailModal = (source, reportId) => {
  if (source === "SAFETRIP") {
    loadUserReportDetailInfo(reportId);
  } else {
    loadExternalReportDetailInfo(reportId);
  }
  show();
}

function mapUserReportDetail(result) {
  Object.assign(selectedReport, {
    source: result.source,
    sourceUrl: '',
    author: '',
    nickname: result.nickname,
    scamAction: result.scamAction,
    scamContext: result.scamContext,
    countryName: result.countryName,
    stateName: result.stateName,
    cityName: result.cityName,
    title: result.title,
    content: result.description,
    postedAt: formatDate(result.createdAt),
    imageUrls: result.urls || [],
  })
}

function mapExternalReportDetail(result) {
  Object.assign(selectedReport, {
    source: result.source,
    sourceUrl: result.sourceUrl,
    author: result.author,
    nickname: '',
    scamAction: result.scamAction,
    scamContext: result.scamContext,
    countryName: result.countryName,
    stateName: result.stateName,
    cityName: result.cityName,
    title: result.title,
    content: result.summary,
    postedAt: formatDate(result.postedAt),
  })
}

/**
 * 사용자 리포트 상세 정보를 조회합니다.
 * @param {number} reportId - 조회할 리포트 ID
 */
const loadUserReportDetailInfo = async (reportId) => {
  try {
    const response = await apiClient.get(`/user-reports/${reportId}`);

    const result = response.data.result;
    mapUserReportDetail(result);

  } catch (e) {
    console.error('API 요청 실패:', e);
  }
}

/**
 * 외부 리포트 상세 정보를 조회합니다.
 * @param {number} reportId - 조회할 리포트 ID
 */
const loadExternalReportDetailInfo = async (reportId) => {
  try {
    const response = await apiClient.get(`/external-reports/${reportId}`);

    const result = response.data.result;
    mapExternalReportDetail(result);

  } catch (e) {
    console.error('API 요청 실패:', e);
  }
}



/**
 * 국가별 스캠 통계 목록을 조회합니다 (페이지네이션 지원).
 * 스캠 리포트 개수 기준으로 내림차순 정렬하여 반환합니다.
 * @param {string} mode - 'click' (새로고침) 또는 'scroll' (스크롤 추가 로드)
 */
const loadSidebarCountryStatistics = async (mode = 'click') => {

  if (mode === 'scroll' && (isLoadingCountry.value || isLastCountryPage.value)) return;

  isLoadingCountry.value = true;

  if (mode === 'click') {
    countryPage.value = 0;
    isLastCountryPage.value = false;
    sidebarCountries.value = []; 
  }

  try {

    const response = await apiClient.get('/countries/statistics', {
      params: {
        page: countryPage.value,
        size: size,
        // sort: "countryName,ASC"
        sort: "scamCnt,DESC"
      }
    });
    const content = response.data.result.content;
    const last = response.data.result.last;

    sidebarCountries.value.push(...content);
    isLastCountryPage.value = last;
    countryPage.value += 1;
  } catch (e) {
    console.error("Failed to load countries sidebar info because of server error. Please try again later.", e);
  } finally {
    isLoadingCountry.value = false;
  }
}

const loadCountryDetail = async (countryId, countryName, scamCnt) => {
  selectedCountry.id = countryId;
  selectedCountry.name = countryName;
  selectedCountry.scamCnt = scamCnt;
  
  // 국가의 state 목록 로드
  await loadSidebarStateStatistics(countryId, countryName, 'click');
  // viewType을 'state'로 설정
  viewType.value = 'state';
};


const loadCountryAllReports = async (countryId, countryName) => {
  selectedCountry.id = countryId;
  selectedCountry.name = countryName;
  selectedState.id = null;
  selectedState.name = '';
  selectedCity.id = null;
  selectedCity.name = '';
  
  // 국가의 모든 리포트 로드
  await loadSidebarReportSummaryByCountry(countryId, 'click');
};

const loadStateAllReports = async (stateId, stateName) => {
  selectedState.id = stateId;
  selectedState.name = stateName;
  selectedCity.id = null;
  selectedCity.name = '';
  
  // State의 모든 리포트 로드
  await loadSidebarReportSummaryByState(stateId, 'click');
};

/**
 * 특정 주(State)의 모든 스캠 리포트 목록을 조회합니다 (페이지네이션 지원).
 * @param {number} stateId - 조회할 주(State) ID
 * @param {string} mode - 'click' (새로고침) 또는 'scroll' (스크롤 추가 로드)
 */
const loadSidebarReportSummaryByState = async (stateId, mode = 'click') => {
  if (mode === 'scroll' && (isLoadingReport.value || isLastReportPage.value)) return;

  isLoadingReport.value = true;

  if (mode === 'click') {
    reportPage.value = 0;
    isLastReportPage.value = false;
    sidebarReports.value = [];
  }

  try {
    const response = await apiClient.get(`/states/${stateId}/reports`, {
      params: {
        page: reportPage.value,
        size: size
      }
    });
    
    const content = response.data.result.content;
    const last = response.data.result.last;
    sidebarReports.value.push(...content);
    isLastReportPage.value = last;
    reportPage.value += 1;

    viewType.value = 'report';
  } catch (e) {
    console.error('Failed to load state reports:', e);
  } finally {
    isLoadingReport.value = false;
  }
};

/**
 * 특정 국가의 모든 스캠 리포트 목록을 조회합니다 (페이지네이션 지원).
 * @param {number} countryId - 조회할 국가 ID
 * @param {string} mode - 'click' (새로고침) 또는 'scroll' (스크롤 추가 로드)
 */
const loadSidebarReportSummaryByCountry = async (countryId, mode = 'click') => {
  if (mode === 'scroll' && (isLoadingReport.value || isLastReportPage.value)) return;

  isLoadingReport.value = true;

  if (mode === 'click') {
    reportPage.value = 0;
    isLastReportPage.value = false;
    sidebarReports.value = [];
  }

  try {
    const response = await apiClient.get(`/countries/${countryId}/reports`, {
      params: {
        page: reportPage.value,
        size: size
      }
    });
    
    const content = response.data.result.content;
    const last = response.data.result.last;
    sidebarReports.value.push(...content);
    isLastReportPage.value = last;
    reportPage.value += 1;

    viewType.value = 'report';
  } catch (e) {
    console.error('Failed to load country reports:', e);
  } finally {
    isLoadingReport.value = false;
  }
};

/**
 * 특정 국가의 주(State)별 스캠 통계 목록을 조회합니다 (페이지네이션 지원).
 * 스캠 리포트 개수 기준으로 내림차순 정렬하여 반환합니다.
 * @param {number} countryId - 조회할 국가 ID
 * @param {string} countryName - 국가 이름
 * @param {string} mode - 'click' (새로고침) 또는 'scroll' (스크롤 추가 로드)
 */
const loadSidebarStateStatistics = async (countryId, countryName, mode = 'click') => {
  if (mode === 'scroll' && (isLoadingState.value || isLastStatePage.value)) return;

  isLoadingState.value = true;
  selectedCountry.id = countryId;
  selectedCountry.name = countryName;

  // 새로운 Country 클릭이면 초기화
  if (mode === 'click') {
    statePage.value = 0;
    isLastStatePage.value = false;
    sidebarStates.value = []; 
  }

  try {
    const response = await apiClient.get('/states/statistics', {
      params: {
        countryId,
        page: statePage.value,
        size,
        sort: "scamCnt,DESC"
      }
    });

    const content = response.data.result.content;
    const last = response.data.result.last;

    sidebarStates.value.push(...content);
    isLastStatePage.value = last;
    statePage.value += 1;
    // viewType은 호출한 곳에서 설정 (state 유지)
  } catch (e) {
    console.error("Failed to load states sidebar info because of server error. Please try again later.", e);
  } finally {
    isLoadingState.value = false;
  }
};


/**
 * 특정 주(State)의 도시(City)별 스캠 통계 목록을 조회합니다 (페이지네이션 지원).
 * 스캠 리포트 개수 기준으로 내림차순 정렬하여 반환합니다.
 * @param {number} stateId - 조회할 주(State) ID
 * @param {string} stateName - 주(State) 이름
 * @param {string} mode - 'click' (새로고침) 또는 'scroll' (스크롤 추가 로드)
 */
const loadSidebarCityStatistics = async (stateId, stateName, mode = 'click') => {
  if (mode === 'scroll' && (isLoadingCity.value || isLastCityPage.value)) return;

  isLoadingCity.value = true;

  selectedState.id = stateId;
  selectedState.name = stateName;
  // State의 scamCnt는 sidebarStates에서 찾아서 설정
  const state = sidebarStates.value.find(s => s.id === stateId);
  selectedState.scamCnt = state?.scamCnt || 0;

  if (mode === 'click') {
    cityPage.value = 0;
    isLastCityPage.value = false;
    sidebarCities.value = []; 
  }

  try {
    const response = await apiClient.get('/cities/statistics', {
      params: {
        stateId: stateId,
        page: cityPage.value,
        size: size,
        sort: "scamCnt,DESC"
      }
    });
    const content = response.data.result.content;
    const last = response.data.result.last;

    sidebarCities.value.push(...content);
    isLastCityPage.value = last;
    cityPage.value += 1;

    viewType.value = 'city';
  } catch (e) {
    console.error("Failed to load cities sidebar info because of server error. Please try again later.", e);
  } finally {
    isLoadingCity.value = false;
  }
}

/**
 * 특정 도시(City)의 스캠 리포트 목록을 조회합니다 (페이지네이션 지원).
 * @param {number} cityId - 조회할 도시(City) ID
 * @param {string} cityName - 도시(City) 이름
 * @param {string} mode - 'click' (새로고침) 또는 'scroll' (스크롤 추가 로드)
 */
const loadSidebarReportSummary = async (cityId, cityName, mode = 'click') => {
  if (mode === 'scroll' && (isLoadingReport.value || isLastReportPage.value)) return;

  isLoadingReport.value = true;

  selectedCity.id = cityId;
  selectedCity.name = cityName;

  if (mode === 'click') {
    reportPage.value = 0;
    isLastReportPage.value = false;
    sidebarReports.value = []; 
  }

  try {
    const response = await apiClient.get(`/cities/${cityId}/reports`, {
      params: {
        page: reportPage.value,
        size: size
      }
    });
    const content = response.data.result.content;
    const last = response.data.result.last;
    sidebarReports.value.push(...content);
    isLastReportPage.value = last;
    reportPage.value += 1;

    viewType.value = 'report';
  } catch (e) {
    console.error('Failed to load reports sidebar info because of server error. Please try again later.', e);
  } finally {
    isLoadingReport.value = false;
  }
}


const backToList = (type) => {
  viewType.value = type;
}

const backToPreviousView = () => {
  if (selectedCity.id) {
    // 도시에서 온 경우
    viewType.value = 'city';
  } else if (selectedState.id) {
    // State에서 온 경우 - state로 돌아감
    viewType.value = 'state';
  } else if (selectedCountry.id) {
    // 국가에서 온 경우
    viewType.value = 'country';
  }
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
  
  if (viewType.value === 'country') {
    loadSidebarCountryStatistics('scroll');
  } else if (viewType.value === 'state') {
    loadSidebarStateStatistics(selectedCountry.id, selectedCountry.name, 'scroll');
  } else if (viewType.value === 'city') {
    loadSidebarCityStatistics(selectedState.id, selectedState.name, 'scroll');
  } else if (viewType.value === 'report') {
    if (selectedCity.id) {
      loadSidebarReportSummary(selectedCity.id, selectedCity.name, 'scroll');
    } else if (selectedState.id) {
      loadSidebarReportSummaryByState(selectedState.id, 'scroll');
    } else {
      loadSidebarReportSummaryByCountry(selectedCountry.id, 'scroll');
    }
  }
};

onMounted(() => {
  loadSidebarCountryStatistics();
})

</script>

<style scoped lang="scss">

$sidebar-width: 560px;

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
  left: -$sidebar-width; /* 기본적으로 숨김 */
  width: $sidebar-width;
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
  left: $sidebar-width;
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
