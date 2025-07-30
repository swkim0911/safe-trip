<template>
  <div class="modal fade" ref="modalRef" id = "reportFormModal" data-bs-backdrop="static" data-bs-keyboard="false" tabindex="-1" aria-labelledby="staticBackdropLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered modal-dialog-scrollable modal-lg">
      <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="staticBackdropLabel">제보를 남겨주세요</h5>
            <button type="button" class="btn-close" @click="hide" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <form @submit.prevent>
              <div class="mb-3">
                  <label for="report-title" class="col-form-label fw-bold">
                    <font-awesome-icon :icon="['far', 'message']" class="modal-icon"/>
                    제목
                  </label>
                <input type="text" :class="['form-control', { 'is-invalid': errors.title }]" id="report-title" v-model="form.title" maxlength="100" placeholder="제목을 입력하세요">
              </div>
              <div class="btn-group d-flex justify-content-center ms-3 me-3" role="group">
                <input type="radio" class="btn-check" name="scamType" id="scam1" v-model="form.scamId" :value="0">
                <label class="btn btn-outline-primary" for="scam1">교통수단</label>

                <input type="radio" class="btn-check" name="scamType" id="scam2" v-model="form.scamId" :value="1">
                <label class="btn btn-outline-primary" for="scam2">소매치기</label>

                <input type="radio" class="btn-check" name="scamType" id="scam3" v-model="form.scamId" :value="2">
                <label class="btn btn-outline-primary" for="scam3">투어 사기</label>

                <input type="radio" class="btn-check" name="scamType" id="scam4" v-model="form.scamId" :value="3">
                <label class="btn btn-outline-primary" for="scam4">강매 / 바가지 (금전사기)</label>

                <input type="radio" class="btn-check" name="scamType" id="scam5" v-model="form.scamId" :value="4">
                <label class="btn btn-outline-primary" for="scam5">기타</label>
              </div>
              <div v-if="errors.scamId && form.scamId === null" class="text-danger small mt-1 text-start ms-3">
                {{ errors.scamId }}
              </div>
              <div class="mb-3">
                <label for="report-address" class="col-form-label fw-bold">
                  <font-awesome-icon :icon="['far', 'map']" class="modal-icon" />
                  주소
                </label>
                <input
                  id="report-address"
                  placeholder="주소를 검색하세요"
                  :class="['form-control', { 'is-invalid': errors.address }]"
                  v-model="form.address"
                  @keyup.enter="searchAddress"
                />
                <div v-if="errorMessage" class="alert alert-danger mt-2">
                  {{ errorMessage }}
                </div>
                <div ref="mapRef" style="width: 100%; height: 400px; margin-top: 10px;"></div>
              </div>
              <div class="mb-3">
                <label for="photo" class="form-label fw-bold">
                  <font-awesome-icon :icon="['fas', 'camera']" class="modal-icon"/>
                  사진 업로드 (선택)
                </label>
                <input ref="fileInput" class="form-control" type="file" id="photo" accept="image/*" @change="handleFileChange">
                <div v-if="errors.imageFile" class="text-danger small mt-1">
                {{ errors.imageFile }}
              </div>
              </div>
              <div class="mb-3">
                <label for="report-description" class="col-form-label fw-bold">
                  <font-awesome-icon :icon="['far', 'message']" class="modal-icon"/>
                  내용
                </label>
                <textarea :class="['form-control', { 'is-invalid': errors.advice }]" id="report-description" v-model="form.description" @input="e => updateCharCnt(e, 'description')" placeholder="내용을 입력하세요"></textarea>
                <small ref="descriptionCntRef" class="d-flex justify-content-end">0 / {{ textareaLength }}</small>
                
              </div>
              <div class="mb-3">
                <label for="report-advice" class="col-form-label fw-bold">
                  <font-awesome-icon :icon="['far', 'message']" class="modal-icon"/>
                  조언
                </label>
                <textarea :class="['form-control', { 'is-invalid': errors.advice }]" id="report-advice" v-model="form.advice" @input="e => updateCharCnt(e, 'advice')" placeholder="조언을 입력하세요"></textarea>
                <small ref="adviceCntRef" class="d-flex justify-content-end">0 / {{ textareaLength }}</small>
              </div>
              <div class="modal-footer">
                <div v-if="submitMessage" :class="['me-3 fw-bold', submitStatus === 'success' ? 'text-success' : 'text-danger']">
                  {{ submitMessage }}
                </div>
                <button type="button" class="btn btn-secondary" @click="hide">close</button>
                <button :disabled="isSubmitting" type="button" class="btn btn-primary" @click="submitForm">send</button>
              </div>
            </form>
          </div>          
        </div>    
    </div>
  </div>
</template>
<script setup>
import { useAuthStore } from '@/stores/auth';

import { ref, onMounted, reactive } from 'vue'
import { Loader } from '@googlemaps/js-api-loader'
import { useBootstrapModal } from '@/composables/useBootstrapModal';
import apiClient from '@/api/apiClient';

const authStore = useAuthStore();
const isLoggedIn = () => {
  return !!authStore.accessToken;
}

const googleMapApiKey = import.meta.env.VITE_GOOGLE_MAP_API_KEY;

const mapRef = ref(null);
const modalRef = ref(null);

const { hide } = useBootstrapModal(modalRef);

const errorMessage = ref('');

let map, marker, geocoder;

const textareaLength = 500;
const fileInput = ref('');

const descriptionCntRef = ref(null);
const adviceCntRef = ref(null);

const form = reactive({
  title: '',
  scamId: null,
  address: '',
  lat: '',
  lng: '',
  country: '',
  city: '',
  description: '',
  advice: '',
  imageFile: null
})

const errors = reactive({
  title: false,
  scamId: '',
  address: false,
  imageFile: '',
  description: false,
  advice: false,
})

const submitMessage = ref('');
const submitStatus = ref(''); // 'success' | 'error'

const handleFileChange = (event) => {
  const file = event.target.files[0];
  if (!file) return;

  const maxSizeInBytes = 5 * 1024 * 1024; // 5MB

   if (file.size > maxSizeInBytes) {
    errors.imageFile = '5MB 이하의 이미지만 업로드할 수 있습니다.';
    form.imageFile = null;
    event.target.value = ''; // input 초기화
    return;
  }

  // 통과 시 저장
  form.imageFile = file;
  errors.imageFile = '';
}

const extractJsonFromForm = (excludeKeys = ['imageFile']) => {
  const json = {}

  for (const key in form) {
    if (!excludeKeys.includes(key)) {
      json[key] = form[key]
    }
  }
  return new Blob([JSON.stringify(json)], {type:'application/json'})
}

const checkForm = () => {
  let isValid = true;

  if (!form.title.trim()) {
    errors.title = true;
    isValid = false;
  } else {
    errors.title = false;
  }

  if (form.scamId === null) {
    errors.scamId = '사기를 선택해주세요.';
    isValid = false;
  } else {
    errors.scamId = '';
  }

  if (!form.address.trim()) {
    errors.address = true;
    isValid = false;
  } else {
    errors.address = false;
  }

  if (!form.lat.trim() && !form.lng.trim()) {
    errors.address = true;
    isValid = false;
  } else {
    errors.address = false;
  }

  if (!form.description.trim()) {
    errors.description = true;
    isValid = false;
  } else {
    errors.description = false;
  }

  if (!form.advice.trim()) {
    errors.advice = true;
    isValid = false;
  } else {
    errors.advice = false;
  }

  return isValid;
};

const resetForm = () => {
  form.title = '';
  form.scamId = null;
  form.address = null;
  form.lat = '';
  form.lng = '';
  form.country = '';
  form.city = '';
  form.description = '';
  form.advice = '';
  form.imageFile = null;

  if (fileInput) {
    fileInput.value.value = '';
  }
  marker?.setMap(null);
  descriptionCntRef.value.textContent = `0 / ${textareaLength}`;
  adviceCntRef.value.textContent = `0 / ${textareaLength}`;

  errors.title = false;
  errors.scamId = '';
  errors.address = false;
  errors.imageFile = '';
  errors.description = false;
  errors.advice = false;

  errorMessage.value = '';
}

const setupModalEventListener = () => {
  const modal = document.getElementById('reportFormModal');

  if (modal) {
    modal.addEventListener('hide.bs.modal', () => {
      document.activeElement.blur(); // 모달이 닫히기 직전에 blur
    });

    modal.addEventListener('hidden.bs.modal', () => {
      resetForm(); // 모달이 닫힐 때 form에 입력된 값들 모두 지움.
      submitMessage.value = '';
      submitStatus.value = '';
    });
  }
}

const isSubmitting = ref(false); // 코드 내에서 중복 호출 방지

const submitForm = async () => {
  if (isSubmitting.value) return;
  if (!isLoggedIn()) {
    submitMessage.value = 'Please login.';
    submitStatus.value = 'error';
    return;
  }

  if (!checkForm() || errorMessage.value) {
    submitMessage.value = 'Invalid input. Please check your entries.';
    submitStatus.value = 'error';
    return;
  }
  isSubmitting.value = true;

  try {
    await submitReportForm();
    submitMessage.value = 'Your report has been successfully submitted.';
    submitStatus.value = 'success';
    resetForm();

  } catch (error) {
    console.error(error);

    if (error.response) {
    // 서버에서 응답을 받았지만 오류 상태 코드
      const status = error.response.status;

      if (status === 400) {
        submitMessage.value = 'Invalid input data.';
      } else if (status == 401) {
        const errorCode = error.response.data?.code;

        if (errorCode === 40101) {
          // Access Token 만료 → silent refresh 시도
          const ok = await restoreSession();
          if (ok) {
            try {
              await submitReportForm();
              submitMessage.value = 'Your report has been successfully submitted.';
              submitStatus.value = 'success';
              resetForm();
            } catch (retryError) {
              submitMessage.value = 'Submission failed after session refresh. Please try again.';
              submitStatus.value = 'error';
            } finally {
              isSubmitting.value = false;
            }
            return;
          }
          // 리프레시 실패 → 아래의 invalid 토큰 처리로 이어짐
        }

        // === Invalid, tampered token ===
        authStore.clearAccessToken();
        authStore.clearUser();
        hide(); // 글쓰기 모달 닫기
        return;
      } else if (status === 404) {
        submitMessage.value = 'The requested API endpoint was not found.';
      } else if (status === 500) {
        submitMessage.value = 'A server error occurred. Please try again later.';
      } else {
        submitMessage.value = `An unknown error occurred (code ${status}).`;
      }

    } else if (error.request) { // 요청이 전송되었지만 응답이 없음
      submitMessage.value = 'No response from the server. Please check your network connection.';
    } else { // 기타 에러
      submitMessage.value = 'An unknown error occurred during the request.';
    }
    submitStatus.value = 'error';
  } finally {
    isSubmitting.value = false;
  } 
}

const submitReportForm = async () => {
  const formData = new FormData();
  formData.append('request', extractJsonFromForm());
  if (form.imageFile) {
    formData.append('images', form.imageFile);
  }

  return apiClient.post('/reports', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
};

const restoreSession = async () => {
  try {
    const { data } = await apiClient.post('/auth/refresh', {}, { withCredentials: true });
    authStore.setAccessToken(data.result.accessToken);

    // accessToken 얻었으니 사용자 정보 요청
    const { data: meResponse } = await apiClient.get('/me');
    authStore.setUser(meResponse.result);

    return true;
    
  } catch (e) {
    // refreshToken 없거나 만료된 상태
    console.warn('Silent refresh failed:', e);
    return false;
  }
}

const updateCharCnt = (event, type) => {
  const textarea = event.target;
  let text = textarea.value;

  if (text.length > textareaLength) {
    text = text.slice(0, textareaLength);
    textarea.value = text;
  }

  const countText = `${text.length} / ${textareaLength}`;

  if (type === 'description') {
    descriptionCntRef.value.textContent = countText;
  } else if (type === 'advice') {
    adviceCntRef.value.textContent = countText;
  }
}

// 지도 초기화
const initMap = async () => {
  const loader = new Loader({
    apiKey: googleMapApiKey,
    version: 'weekly',
  });

  await loader.load();

  const { Map } = await google.maps.importLibrary("maps");
  const { AdvancedMarkerElement } = await google.maps.importLibrary("marker");

  geocoder = new google.maps.Geocoder();

  map = new Map(mapRef.value, {
    center: { lat: 37.5665, lng: 126.9780 }, // 서울
    zoom: 14,
    mapId: "MAP_ID"
  });

  map.addListener("click", (e) => {
    const lat = e.latLng.lat();
    const lng = e.latLng.lng();
    setMarker({ lat, lng });
    reverseGeocode(lat, lng);
  });
}

// 주소 검색 → 좌표 → 지도 표시
const searchAddress = () => {
  if (!form.address || !geocoder) return;

  geocoder.geocode({ address: form.address }, (results, status) => {
    if (status === 'OK') {
      const location = results[0].geometry.location;
      const lat = location.lat();
      const lng = location.lng();
      map.setCenter(location); // location 위치로 지도의 중심 변경
      reverseGeocode(lat, lng);
      setMarker({ lat, lng });
    } else {
      errorMessage.value = `Google 지도에서 ${form.address}을(를) 찾을 수 없습니다.`;
      errors.address = false;
    }
  })
}

// 위도/경도로 주소 변환
const reverseGeocode = (lat, lng) => {
  geocoder.geocode({ location: { lat, lng } }, (results, status) => {
    if (status === 'OK' && results[0]) {
      form.address = results[0].formatted_address;
      form.lat = lat;
      form.lng = lng;

      const address_components = results[0].address_components;
      const countryComponent = extractCountry(address_components);
      const country = countryComponent.name;
      let city;
      if (countryComponent.code === 'JP') {
        city = address_components.find(comp =>
          comp.types.includes("administrative_area_level_1")
        ).long_name;
      } else {
        city = extractCity(address_components);
      }
      form.country = country;
      form.city = city;

      errorMessage.value = '';
      errors.address = false;
    } else {
      errorMessage.value = `Google 지도에서 ${form.address}을(를) 찾을 수 없습니다.`;
      errors.address = false;
    }
  })
}

const extractCity = (address_components) => {
  const locality = address_components.find(comp =>
    comp.types.includes("locality")
  );
  if (locality) return locality.long_name;

  const postalTown = address_components.find(comp =>
    comp.types.includes("postal_town")
  );
  if (postalTown) return postalTown.long_name;

  const admin1 = address_components.find(comp =>
    comp.types.includes("administrative_area_level_1")
  );
  if (admin1) return admin1.long_name;

  console.warn("도시 정보를 추출할 수 없습니다.", address_components);
  return "Unknown City";
}

const extractCountry = (address_components) => {
  const countryComponent = address_components.find(comp =>
    comp.types.includes("country")
  );

  return {
    name: countryComponent?.long_name || null,
    code: countryComponent?.short_name || null
  };
}


// 마커 설정
const setMarker = async ({lat, lng}) => {
  if (marker) marker.setMap(null);

    marker = new google.maps.marker.AdvancedMarkerElement({
          map,
          position: { lat, lng },
      });
}

onMounted(() => {
  initMap(),
  setupModalEventListener()

})

</script>
<style scoped lang="scss">
  .modal-icon{
    font-size: 95%;
  }
</style>