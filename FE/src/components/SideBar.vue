<template>
  <div class="sidebar-container">
    <div :class="['sidebar', {'open': isOpen}]">
      <!-- 검색창 -->
      <div class="sidebar-header form-group position-relative">
        <font-awesome-icon 
          :icon="['fas', 'search']" 
          class="search-icon"
        />
        <input
          type="text"
          v-model="searchText"
          placeholder="Search by country, state, or city"
          class="form-control form-control-lg mb-3 search-input"
        />
      </div>

      <div class="sidebar-body" @scroll="onSidebarScroll" ref="sidebarRef">
        <template v-if="viewType === 'country'">
          <div v-if="isLoadingCountry && sidebarCountries.length === 0" class="loading-spinner">
            <div class="spinner-border text-primary" role="status">
              <span class="visually-hidden">Loading...</span>
            </div>
          </div>
          <div v-else-if="!isLoadingCountry && sidebarCountries.length === 0" class="empty-state">
            <font-awesome-icon :icon="['fas', 'globe']" class="empty-icon" />
            <p>No countries found.</p>
          </div>
          <ul class="list-group">
            <li
              class="list-group-item d-flex justify-content-between align-items-start"
              v-for="country in sidebarCountries"
              :key="country.id"
              @click="showStatesByCountry(country.id, country.name, country.scamCnt)"
            >
              <div class="ms-2 me-auto d-flex align-items-center">
                <span v-if="country.iso2" class="me-2" style="font-size: 1.2em;">
                  {{ getCountryFlag(country.iso2) }}
                </span>
                <div class="fw-bold">{{ country.name }}</div>
              </div>
              <div class="d-flex align-items-center gap-1">
                <span v-if="country.riskLevel" class="badge risk-badge" :class="country.riskLevel.toLowerCase()">
                  {{ country.riskLevel }}
                </span>
                <span class="badge text-bg-primary rounded-pill">{{ country.scamCnt }}</span>
              </div>
            </li>
          </ul>
        </template>

        <template v-else-if="viewType === 'state'">
          <div class="d-flex align-items-center justify-content-center position-relative mb-3">
            <button
              class="btn position-absolute start-0"
              @click="goToParentView('country')"
            >
              <font-awesome-icon :icon="['fas', 'chevron-left']" />
            </button>
            <h6 class="fw-bold mb-0 text-center">{{ selectedCountry.name }}</h6>
          </div>

          <!-- 상단: 국가의 모든 리포트 보기 버튼 -->
          <div class="mb-3">
            <button 
              class="btn btn-primary w-100 d-flex align-items-center justify-content-between p-3 report-cta"
              @click="showReportsByCountry(selectedCountry.id, selectedCountry.name)"
              style="border-radius: 8px;"
            >
              <span class="d-flex align-items-center">
                <font-awesome-icon :icon="['fas', 'file-alt']" class="me-2" />
                See all reports
              </span>
              <span class="badge bg-light text-dark">
                {{ selectedCountry.scamCnt }} reports
              </span>
            </button>
          </div>

          <!-- 하단: State 목록 -->
          <div>
            <div v-if="isLoadingState && sidebarStates.length === 0" class="loading-spinner">
              <div class="spinner-border text-primary" role="status">
                <span class="visually-hidden">Loading...</span>
              </div>
            </div>
            <div v-else-if="!isLoadingState && sidebarStates.length === 0" class="empty-state">
              <font-awesome-icon :icon="['fas', 'map']" class="empty-icon" />
              <p>No states found.</p>
            </div>
            <ul class="list-group">
              <li
                class="list-group-item d-flex justify-content-between align-items-start"
                v-for="state in sidebarStates"
                :key="state.id"
                @click="showCitiesByState(state.id, state.name)"
              >
                <div class="ms-2 me-auto">
                  <div class="fw-bold">{{ state.name }}</div>
                </div>
                <div class="d-flex align-items-center gap-1">
                  <span v-if="state.riskLevel" class="badge risk-badge" :class="state.riskLevel.toLowerCase()">
                    {{ state.riskLevel }}
                  </span>
                  <span class="badge text-bg-primary rounded-pill">{{ state.scamCnt }}</span>
                </div>
              </li>
            </ul>
          </div>
        </template>


        <template v-else-if="viewType === 'city'">
          <div class="d-flex align-items-center justify-content-center position-relative mb-3">
            <button
              class="btn position-absolute start-0"
              @click="goToParentView('state')"
            >
              <font-awesome-icon :icon="['fas', 'chevron-left']" />
            </button>
            <h6 class="fw-bold mb-0 text-center">Cities in {{ selectedState.name }}</h6>
          </div>

          <!-- 상단: 국가의 모든 리포트 보기 버튼 -->
          <div class="mb-3">
            <button 
              class="btn btn-primary w-100 d-flex align-items-center justify-content-between p-3 report-cta"
              @click="showReportsByState(selectedState.id, selectedState.name)"
              style="border-radius: 8px;"
            >
              <span class="d-flex align-items-center">
                <font-awesome-icon :icon="['fas', 'file-alt']" class="me-2" />
                See all reports in {{ selectedState.name }}
              </span>
              <span class="badge bg-light text-dark">
                {{ selectedState.scamCnt || 0 }} reports
              </span>
            </button>
          </div>

          <div v-if="isLoadingCity && sidebarCities.length === 0" class="loading-spinner">
            <div class="spinner-border text-primary" role="status">
              <span class="visually-hidden">Loading...</span>
            </div>
          </div>
          <div v-else-if="!isLoadingCity && sidebarCities.length === 0" class="empty-state">
            <font-awesome-icon :icon="['fas', 'city']" class="empty-icon" />
            <p>No cities found.</p>
          </div>
          <ul class="list-group">
            <li
              class="list-group-item d-flex justify-content-between align-items-start"
              v-for="city in sidebarCities"
              :key="city.id"
              @click="showReportsByCity(city.id, city.name)"
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
              @click="backFromReport"
            >
              <font-awesome-icon :icon="['fas', 'chevron-left']" />
            </button>

            <h6 class="fw-bold mb-0 text-center">
              <span v-if="selectedCity.id">Reports from {{ selectedCity.name }}</span>
              <span v-else-if="selectedState.id">Reports from {{ selectedState.name }}</span>
              <span v-else>Reports from {{ selectedCountry.name }}</span>
            </h6>
          </div>
          <div v-if="isLoadingReport && sidebarReports.length === 0" class="loading-spinner">
            <div class="spinner-border text-primary" role="status">
              <span class="visually-hidden">Loading...</span>
            </div>
          </div>
          <div v-else-if="!isLoadingReport && sidebarReports.length === 0" class="empty-state">
            <font-awesome-icon :icon="['fas', 'file-circle-xmark']" class="empty-icon" />
            <p>No reports found.</p>
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

              <div class="fw-bold mb-1 mt-3 d-flex align-items-start">
                <font-awesome-icon :icon="['fas', 'shield-alt']" class="me-2 text-danger mt-1" />
                <span>{{ report.title }}</span>
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
      <font-awesome-icon 
        v-if="!isOpen" 
        :icon="['fas', 'chevron-right']" 
      />
      <font-awesome-icon 
        v-else 
        :icon="['fas', 'chevron-left']" 
      />
    </button>
  </div>
  <ReportDetailModal :report="selectedReport"/>

</template>

<script setup>
import { ref, onMounted, reactive, watch } from 'vue';
import { useMapStore } from '@/stores/map';
import { useBootstrapModal } from '@/composables/useBootstrapModal';
import ReportDetailModal from './ReportDetailModal.vue'
import apiClient from '@/api/apiClient';
import dayjs from 'dayjs'

const { show } = useBootstrapModal('#reportDetailModal');

const mapStore = useMapStore();

watch(() => mapStore.selectedMarker, (marker) => {
  if (!marker) return;
  isOpen.value = true;
  const { id, name, scamCnt, groupBy } = marker;
  if (groupBy === 'country') {
    showStatesByCountry(id, name, scamCnt);
  } else if (groupBy === 'state') {
    showCitiesByState(id, name, 'click', scamCnt);
  } else if (groupBy === 'city') {
    showReportsByCity(id, name);
  }
});

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

/**
 * ISO2 코드를 플래그 이모티콘으로 변환합니다.
 * @param {string} iso2 - ISO2 국가 코드 (예: "US", "KR", "JP")
 * @returns {string} 플래그 이모티콘 (예: "🇺🇸", "🇰🇷", "🇯🇵")
 */
const getCountryFlag = (iso2) => {
  if (!iso2 || typeof iso2 !== 'string' || iso2.length !== 2) {
    return '';
  }
  
  const upperIso2 = iso2.toUpperCase();
  const codePoints = upperIso2
    .split('')
    .map(char => 0x1F1E6 + (char.charCodeAt(0) - 0x41));
  
  try {
    return String.fromCodePoint(...codePoints);
  } catch {
    return '';
  }
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
 * 통계 정보를 포함한 국가 목록을 조회합니다 (페이지네이션).
 * 스캠 리포트 개수 기준으로 내림차순 정렬하여 반환합니다.
 * @param {string} mode - 'click' (새로고침) 또는 'scroll' (스크롤 추가 로드)
 */
const loadCountriesWithStatistics = async (mode = 'click') => {

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

const showStatesByCountry = async (countryId, countryName, scamCnt) => {
  selectedCountry.id = countryId;
  selectedCountry.name = countryName;
  selectedCountry.scamCnt = scamCnt;
  
  // 국가의 state 목록 로드
  await loadStatesWithStatistics(countryId, countryName, 'click');
  // viewType을 'state'로 설정
  viewType.value = 'state';
};


const showReportsByCountry = async (countryId, countryName) => {
  selectedCountry.id = countryId;
  selectedCountry.name = countryName;
  selectedState.id = null;
  selectedState.name = '';
  selectedCity.id = null;
  selectedCity.name = '';
  
  // 국가의 모든 리포트 로드
  await loadReportsByCountry(countryId, 'click');
  viewType.value = 'report';
};

const showReportsByState = async (stateId, stateName) => {
  selectedState.id = stateId;
  selectedState.name = stateName;
  selectedCity.id = null;
  selectedCity.name = '';
  
  // State의 모든 리포트 로드
  await loadReportsByState(stateId, 'click');
  viewType.value = 'report';
};

/**
 * 특정 주(State)의 모든 스캠 리포트 목록을 조회합니다 (페이지네이션).
 * @param {number} stateId - 조회할 주(State) ID
 * @param {string} mode - 'click' (새로고침) 또는 'scroll' (스크롤 추가 로드)
 */
const loadReportsByState = async (stateId, mode = 'click') => {
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
  } catch (e) {
    console.error('Failed to load state reports:', e);
  } finally {
    isLoadingReport.value = false;
  }
};

/**
 * 특정 국가의 모든 스캠 리포트 목록을 조회합니다 (페이지네이션).
 * @param {number} countryId - 조회할 국가 ID
 * @param {string} mode - 'click' (새로고침) 또는 'scroll' (스크롤 추가 로드)
 */
const loadReportsByCountry = async (countryId, mode = 'click') => {
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
  } catch (e) {
    console.error('Failed to load country reports:', e);
  } finally {
    isLoadingReport.value = false;
  }
};

/**
 * 특정 국가의 통계 정보를 포함한 주(State) 목록을 조회합니다 (페이지네이션).
 * 스캠 리포트 개수 기준으로 내림차순 정렬하여 반환합니다.
 * @param {number} countryId - 조회할 국가 ID
 * @param {string} countryName - 국가 이름
 * @param {string} mode - 'click' (새로고침) 또는 'scroll' (스크롤 추가 로드)
 */
const loadStatesWithStatistics = async (countryId, countryName, mode = 'click') => {
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
  } catch (e) {
    console.error("Failed to load states sidebar info because of server error. Please try again later.", e);
  } finally {
    isLoadingState.value = false;
  }
};


/**
 * 특정 주(State)의 도시(City) 목록을 보여줍니다 (페이지네이션 지원).
 * 스캠 리포트 개수 기준으로 내림차순 정렬하여 반환합니다.
 * @param {number} stateId - 조회할 주(State) ID
 * @param {string} stateName - 주(State) 이름
 * @param {string} mode - 'click' (새로고침) 또는 'scroll' (스크롤 추가 로드)
 */
const showCitiesByState = async (stateId, stateName, mode = 'click', fallbackScamCnt = null) => {
  if (mode === 'scroll' && (isLoadingCity.value || isLastCityPage.value)) return;

  isLoadingCity.value = true;

  selectedState.id = stateId;
  selectedState.name = stateName;
  const state = sidebarStates.value.find(s => s.id === stateId);
  selectedState.scamCnt = state?.scamCnt ?? fallbackScamCnt ?? 0;

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
 * 특정 도시(City)의 스캠 리포트 목록을 보여줍니다 (페이지네이션 지원).
 * @param {number} cityId - 조회할 도시(City) ID
 * @param {string} cityName - 도시(City) 이름
 * @param {string} mode - 'click' (새로고침) 또는 'scroll' (스크롤 추가 로드)
 */
const showReportsByCity = async (cityId, cityName, mode = 'click') => {
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


const goToParentView = (type) => {
  viewType.value = type;
}

const backFromReport = () => {
  if (selectedCity.id) {
    // 도시에서 온 경우
    viewType.value = 'city';
  } else if (selectedState.id) {
    // State에서 온 경우 - state로 돌아감
    viewType.value = 'city';
  } else if (selectedCountry.id) {
    // 국가에서 온 경우
    viewType.value = 'state';
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
    loadCountriesWithStatistics('scroll');
  } else if (viewType.value === 'state') {
    loadStatesWithStatistics(selectedCountry.id, selectedCountry.name, 'scroll');
  } else if (viewType.value === 'city') {
    showCitiesByState(selectedState.id, selectedState.name, 'scroll');
  } else if (viewType.value === 'report') {
    if (selectedCity.id) {
      showReportsByCity(selectedCity.id, selectedCity.name, 'scroll');
    } else if (selectedState.id) {
      loadReportsByState(selectedState.id, 'scroll');
    } else {
      loadReportsByCountry(selectedCountry.id, 'scroll');
    }
  }
};

onMounted(() => {
  loadCountriesWithStatistics();
})

</script>

<style scoped lang="scss">

$sidebar-width: 560px;

/* 사이드바 컨테이너 */
.sidebar-container {
  position: fixed;
  display: flex;
  height: 100vh;
  z-index: 1001;
}

/* 사이드바 */
.sidebar {
  position: fixed;
  display: flex;
  top: 0;
  left: -$sidebar-width; /* 기본적으로 숨김 */
  width: $sidebar-width;
  height: 100vh;
  background-color: #ffffff;
  box-shadow: 4px 0 24px rgba(0, 0, 0, 0.08);
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

.search-icon {
  position: absolute;
  left: 24px;
  top: 39%;
  transform: translateY(-50%);
  color: #3B82F6;
  z-index: 10;
  font-size: 1rem;
}

.search-input {
  background: linear-gradient(135deg, #ffffff, #f2f6ff);
  border-radius: 50px;
  border: 1px solid rgba(59, 130, 246, 0.25);
  padding-left: 50px;
  font-size: 1rem;
  box-shadow: 0 8px 18px rgba(59, 130, 246, 0.08);
  transition: box-shadow 0.2s ease, border-color 0.2s ease;

  &::placeholder {
    color: #8aa0c2;
  }

  &:focus {
    border-color: #3B82F6;
    box-shadow: 0 12px 26px rgba(59, 130, 246, 0.15);
  }
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
  position: relative;
  background: linear-gradient(135deg, #ffffff, #f2f6ff);
  color: black;
  border-radius: 12px; /* 모서리를 둥글게 */
  padding: 14px; /* 내부 여백 */
  margin-bottom: 14px; /* 아래 여백 */
  margin-top: 14px;
  border: 1px solid rgba(59, 130, 246, 0.15);
  box-shadow: 0 8px 18px rgba(59, 130, 246, 0.08); /* 그림자 효과 */
  cursor: pointer;
  transition: background 0.25s ease, transform 0.25s ease, box-shadow 0.25s ease;
  isolation: isolate;

  &::before {
    content: '';
    position: absolute;
    inset: -30% auto auto -30%;
    width: 110px;
    height: 110px;
    background: radial-gradient(circle, rgba(59, 130, 246, 0.15), transparent 70%);
    z-index: -1;
  }

  &:hover {
    background: linear-gradient(135deg, #eef3ff, #ffffff);
    transform: translateY(-3px);
    box-shadow: 0 12px 26px rgba(59, 130, 246, 0.15);
  }

  &:active {
    background: linear-gradient(135deg, #e0e9ff, #f7f9ff);
    transform: translateY(0);
    box-shadow: 0 4px 10px rgba(59, 130, 246, 0.2);
  }
}

.report-cta {
  position: relative;
  overflow: hidden;
  border-radius: 12px !important;
  background: linear-gradient(135deg, #3B82F6, #60A5FA);
  box-shadow: 0 8px 18px rgba(59, 130, 246, 0.2);
  transition: transform 0.25s ease, box-shadow 0.25s ease, background 0.25s ease;

  &::before {
    content: '';
    position: absolute;
    inset: 20% -20% auto auto;
    width: 160px;
    height: 160px;
    background: radial-gradient(circle, rgba(255, 255, 255, 0.45), transparent 70%);
    opacity: 0.6;
    transition: opacity 0.25s ease;
  }

  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 12px 26px rgba(59, 130, 246, 0.3);
    background: linear-gradient(135deg, #2563EB, #3B82F6);
  }

  &:hover::before {
    opacity: 0.9;
  }

  &:active {
    transform: translateY(0);
    box-shadow: 0 6px 14px rgba(59, 130, 246, 0.35);
  }
}

.risk-badge {
  font-size: 11px;
  font-weight: 600;
  padding: 2px 6px;
  border-radius: 4px;
  color: white;

  &.high   { background-color: #e74c3c; }
  &.medium { background-color: #f39c12; }
  &.low    { background-color: #2ecc71; }
}

.loading-spinner {
  display: flex;
  justify-content: center;
  padding: 40px 0;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px 0;
  color: #adb5bd;

  .empty-icon {
    font-size: 2rem;
    margin-bottom: 10px;
  }

  p {
    font-size: 14px;
    margin: 0;
  }
}

/* 토글 버튼 (사이드바 여닫기 버튼) */
.toggle-btn {
  position: fixed;
  top: 50%;
  left: 0;
  transform: translateY(-50%);
  background-color: #3B82F6;
  color: white;
  border: none;
  padding: 14px 10px;
  font-size: 15px;
  cursor: pointer;
  border-radius: 0 10px 10px 0;
  box-shadow: 3px 0 12px rgba(59, 130, 246, 0.35);
  transition: left 0.3s ease, background-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;

  &:hover {
    background-color: #2563EB;
    box-shadow: 4px 0 18px rgba(59, 130, 246, 0.5);
    transform: translateY(-50%) translateX(2px);
  }

  &:active {
    background-color: #1D4ED8;
    transform: translateY(-50%) translateX(0);
  }
}
</style>
