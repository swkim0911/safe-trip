<template>
  <div class="modal fade" ref="modal" data-bs-backdrop="static" data-bs-keyboard="false" tabindex="-1" aria-labelledby="staticBackdropLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered modal-dialog-scrollable modal-lg">
      <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="staticBackdropLabel">제보를 남겨주세요</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <form @submit.prevent>
              <div class="mb-3">
                <label for="report-title" class="col-form-label">
                  <font-awesome-icon :icon="['far', 'message']" class="modal-icon"/>
                  제목
                </label>
                <input type="text" class="form-control" id="report-title" v-model="form.title" maxlength="100" placeholder="제목을 입력하세요">
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
              <div class="mb-3">
                <label for="report-address" class="col-form-label">
                  <font-awesome-icon :icon="['far', 'map']" class="modal-icon" />
                  주소
                </label>
                <input
                    id="report-address"
                    placeholder="장소를 검색하세요"
                    class="form-control"
                    v-model="form.address"
                    @keyup.enter="searchAddress"
                  />
                  <div v-if="errorMessage" class="alert alert-danger mt-2">
                    {{ errorMessage }}
                  </div>
                  <div ref="mapRef" style="width: 100%; height: 400px; margin-top: 10px;"></div>
              </div>
              <div class="mb-3">
                <label for="photo" class="form-label">
                  <font-awesome-icon :icon="['fas', 'camera']" class="modal-icon"/>
                  사진 업로드
                </label>
                <input class="form-control" type="file" id="photo" accept="image/*" @change="handleFileChange">
              </div>

              <div class="mb-3">
                <label for="report-description" class="col-form-label">
                  <font-awesome-icon :icon="['far', 'message']" class="modal-icon"/>
                  내용
                </label>
                <textarea class="form-control" id="report-description" ref="descriptionRef" v-model="form.description" @input="e => updateCharCnt(e, 'description')"></textarea>
                <small ref="descriptionCntRef" class="d-flex justify-content-end">0 / {{ textareaLength }}</small>
              </div>
              <div class="mb-3">
                <label for="report-advice" class="col-form-label">
                  <font-awesome-icon :icon="['far', 'message']" class="modal-icon"/>
                  조언
                </label>
                <textarea class="form-control" id="report-advice" ref="adviceRef" v-model="form.advice" @input="e => updateCharCnt(e, 'advice')"></textarea>
                <small ref="adviceCntRef" class="d-flex justify-content-end">0 / {{ textareaLength }}</small>
              </div>
              <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">close</button>
                <button type="button" class="btn btn-primary" @click="submitForm">send</button>
              </div>
            </form>
          </div>          
        </div>    
    </div>
  </div>
</template>
<script setup>
import { ref, onMounted, reactive } from 'vue'
import { Loader } from '@googlemaps/js-api-loader'
import axios from 'axios'

const googleMapApiKey = import.meta.env.VITE_GOOGLE_MAP_API_KEY;

// 반응형 변수
const mapRef = ref(null)

const errorMessage = ref('')

let map, marker, geocoder

const textareaLength = 300

const descriptionRef = ref(null)
const descriptionCntRef = ref(null)

const adviceRef = ref(null)
const adviceCntRef = ref(null)

const form = reactive({
  title: '',
  scamId: null,
  address: '',
  lat: '',
  lng: '',
  description: '',
  advice: '',
  imageFile: null
})



function handleFileChange(event) {
  form.imageFile = event.target.files[0]
}

async function submitForm() {
  try {
    const formData = new FormData()
    formData.append('title', form.title)
    formData.append('scamId', form.scamId)
    formData.append('address', form.address)
    formData.append('lat', form.lat)
    formData.append('lng', form.lng)
    formData.append('description', form.description)
    formData.append('advice', form.advice)
    if (form.imageFile) {
      formData.append('image', form.imageFile)
    }
    await axios.post('http://localhost:8080/reports', formData)

    alert('신고가 성공적으로 접수되었습니다.')
  } catch (error) {
    console.error(error)
  } 
}



function updateCharCnt(event, type) {
  const textarea = event.target
  let text = textarea.value

  if (text.length > textareaLength) {
    text = text.slice(0, textareaLength)
    textarea.value = text
  }

  const countText = `${text.length} / ${textareaLength}`

  if (type === 'description') {
    descriptionCntRef.value.textContent = countText
  } else if (type === 'advice') {
    adviceCntRef.value.textContent = countText
  }
}




// 지도 초기화
async function initMap() {

  const loader = new Loader({
    apiKey: googleMapApiKey,
    version: 'weekly',
  })

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
function searchAddress() {
  if (!form.address || !geocoder) return

  geocoder.geocode({ address: form.address }, (results, status) => {
    if (status === 'OK') {
      const location = results[0].geometry.location
      const lat = location.lat()
      const lng = location.lng()
      map.setCenter(location) // location 위치로 지도의 중심 변경
      setMarker({ lat, lng })
      form.address = results[0].formatted_address
      form.lat = lat
      form.lng = lng
      errorMessage.value = ''
    } else {
      errorMessage.value = `Google 지도에서 ${form.address}을(를) 찾을 수 없습니다.`
    }
  })
}

// 위도/경도로 주소 변환
function reverseGeocode(lat, lng) {
  geocoder.geocode({ location: { lat, lng } }, (results, status) => {
    if (status === 'OK' && results[0]) {
      form.address = results[0].formatted_address
      form.lat = lat
      form.lng = lng
      errorMessage.value = ''
    } else {
      errorMessage.value = `Google 지도에서 ${form.address}을(를) 찾을 수 없습니다.`

      // 상태
    }
  })
}

// 마커 설정
async function setMarker({ lat, lng }) {
  if (marker) marker.setMap(null)

  marker = new google.maps.marker.AdvancedMarkerElement({
        map,
        position: { lat, lng },
    });
}

onMounted(initMap)

</script>
<style lang="scss">
  .modal-icon{
    font-size: 95%;
  }
</style>