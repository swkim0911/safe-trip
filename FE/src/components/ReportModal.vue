<template>
  <div class="modal fade" ref="modal" data-bs-backdrop="static" data-bs-keyboard="false" tabindex="-1" aria-labelledby="staticBackdropLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered modal-dialog-scrollable modal-lg">
      <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="staticBackdropLabel">제보를 남겨주세요</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <div class="mb-3">
              <label for="report-title" class="col-form-label">
                <font-awesome-icon :icon="['far', 'message']" class="modal-icon"/>
                제목
              </label>
              <input type="text" class="form-control" id="report-title" placeholder="제목을 입력하세요">
            </div>
            <div class="btn-group d-flex justify-content-center ms-3 me-3" role="group">
              <input type="radio" class="btn-check" name="scamType" id="scam1" autocomplete="off">
              <label class="btn btn-outline-primary" for="scam1">교통수단</label>

              <input type="radio" class="btn-check" name="scamType" id="scam2" autocomplete="off">
              <label class="btn btn-outline-primary" for="scam2">소매치기</label>

              <input type="radio" class="btn-check" name="scamType" id="scam3" autocomplete="off">
              <label class="btn btn-outline-primary" for="scam3">투어 사기</label>

              <input type="radio" class="btn-check" name="scamType" id="scam4" autocomplete="off">
              <label class="btn btn-outline-primary" for="scam4">강매 / 바가지</label>

              <input type="radio" class="btn-check" name="scamType" id="scam5" autocomplete="off">
              <label class="btn btn-outline-primary" for="scam5">기타</label>

            </div>
            <div class="mb-3">
              <label for="report-location" class="col-form-label">
                <font-awesome-icon :icon="['far', 'map']" class="modal-icon" />
                장소
              </label>
              <input
                  placeholder="장소를 검색하세요"
                  class="form-control"
                  v-model="address"
                  @keyup.enter="searchAddress"
                />
                <div v-if="errorMessage" class="alert alert-danger mt-2">
                  {{ errorMessage }}
                </div>
                <div ref="mapRef" style="width: 100%; height: 400px; margin-top: 10px;"></div>

              <p>선택된 주소: {{ selectedAddress }}</p>
              <p>위도: {{ selectedLat }}, 경도: {{ selectedLng }}</p>
            </div>
            <div class="mb-3">
              <label for="photo" class="form-label">사진 업로드</label>
              <input class="form-control" type="file" id="photo" accept="image/*" multiple>
            </div>

            <div class="mb-3">
              <label for="report-description" class="col-form-label">내용</label>
              <textarea class="form-control" id="report-description"></textarea>
            </div>
            <div class="mb-3">
              <label for="report-advice" class="col-form-label">조언</label>
              <textarea class="form-control" id="report-advice"></textarea>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">close</button>
            <button type="button" class="btn btn-primary">send</button>
          </div>
        </div>    
    </div>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { Loader } from '@googlemaps/js-api-loader'

const googleMapApiKey = import.meta.env.VITE_GOOGLE_MAP_API_KEY;

// 반응형 변수
const mapRef = ref(null)
const address = ref('')
const selectedLat = ref(null) // 값이 존재하지 않음을 표시하기 위해 null로 초기화
const selectedLng = ref(null)
const selectedAddress = ref('')

const errorMessage = ref('')


let map, marker, geocoder

// 2. 지도 초기화
async function initMap() {

  const loader = new Loader({
    apiKey: googleMapApiKey,
    version: 'weekly',
  })

  await loader.load();

  const { Map } = await google.maps.importLibrary("maps");
  geocoder = new google.maps.Geocoder();
  map = new Map(mapRef.value, {
    center: { lat: 37.5665, lng: 126.9780 }, // 서울
    zoom: 14,
  });

  map.addListener("click", (e) => {
    const lat = e.latLng.lat();
    const lng = e.latLng.lng();
    setMarker({ lat, lng });
    reverseGeocode(lat, lng);
  });
}

// 3. 주소 검색 → 좌표 → 지도 표시
function searchAddress() {
  if (!address.value || !geocoder) return

  geocoder.geocode({ address: address.value }, (results, status) => {
    if (status === 'OK') {
      const location = results[0].geometry.location
      const lat = location.lat()
      const lng = location.lng()
      map.setCenter(location) // location 위치로 지도의 중심 변경
      setMarker({ lat, lng })
      selectedAddress.value = results[0].formatted_address
      selectedLat.value = lat
      selectedLng.value = lng
      errorMessage.value = ''
    } else {
      errorMessage.value = `Google 지도에서 ${address.value}을(를) 찾을 수 없습니다.`;
    }
  })
}

// 4. 위도/경도로 주소 변환
function reverseGeocode(lat, lng) {
  geocoder.geocode({ location: { lat, lng } }, (results, status) => {
    if (status === 'OK' && results[0]) {
      selectedAddress.value = results[0].formatted_address
    } else {
      selectedAddress.value = '주소를 찾을 수 없음'
    }
    selectedLat.value = lat
    selectedLng.value = lng
  })
}

// 마커 설정
function setMarker({ lat, lng }) {
  if (marker) marker.setMap(null)
  marker = new google.maps.Marker({
    position: { lat, lng },
    map,
  })
}

onMounted(initMap)

</script>
<style lang="scss">
  .modal-icon{
    font-size: 95%;
  }
</style>