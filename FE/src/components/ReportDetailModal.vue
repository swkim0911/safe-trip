<template>
  <div class="modal fade" ref="modalRef" id = "reportDetailModal" data-bs-backdrop="static" data-bs-keyboard="false" tabindex="-1" aria-labelledby="staticBackdropLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered modal-dialog-scrollable modal-lg">
      <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="staticBackdropLabel">Travel Scam Report</h5>
            <button type="button" class="btn-close" @click="hide" aria-label="Close"></button>
          </div>

          <div class="modal-body">
            <div class="d-flex justify-content-between align-items-start mb-3">
              <h5 class="fw-bold mb-0">{{ report.title }}</h5>

              <div class="text-end small ms-3">
                <div class="d-flex justify-content-end mb-1">
                  <span
                    v-if="report.source === 'SAFETRIP'"
                    class="badge rounded-pill border border-primary text-primary fs-6 px-2 py-1"
                  >
                    👤 User Report
                  </span>
                  <span
                    v-else
                    class="badge rounded-pill bg-light text-dark border fs-6 px-2 py-1"
                  >
                    🤖 Collected by AI Bot
                  </span>
                </div>
                <div>
                  <span v-if="report.source === 'SAFETRIP'" class="text-muted">
                    by {{ report.nickname }}
                  </span>
                  <span v-else>
                    <span class="fw-bold text-primary">{{ report.source }}</span>
                  </span>
                </div>
                <div v-if="report.sourceUrl">
                  <a :href="report.sourceUrl" target="_blank" class="small text-primary text-decoration-none">
                    🔗 Original
                  </a>
                </div>
              </div>

            </div>

            <div class="mb-3">
              <span class="badge bg-danger me-1">{{ report.scamAction }}</span>
              <span class="badge bg-warning">{{ report.scamContext }}</span>
            </div>

            <div class="mb-3">
              <span v-if="report.source === 'SAFETRIP'">
                <span class="fw-bold">Description</span>
              </span>
              <span v-else>
              <span class="fw-bold">Summary</span>
              </span>
              <p class="text-body mt-1">{{ report.content }}</p>
            
            </div>

            <!-- 이미지 표시 (User Report만) -->
            <div v-if="report.source === 'SAFETRIP' && report.imageUrls && report.imageUrls.length > 0" class="mb-3">
              <span class="fw-bold">Photos</span>
              <div class="row g-2 mt-1">
                <div 
                  v-for="(url, index) in report.imageUrls" 
                  :key="index"
                  class="col-md-6"
                >
                  <img 
                    :src="url" 
                    :alt="`Scam evidence ${index + 1}`"
                    class="w-100"
                    style="cursor: pointer; object-fit: cover; height: 200px; border-radius: 4px;"
                    @click="openImageModal(url)"
                  />
                </div>
              </div>
            </div>

            <div class="mb-3">
              <span class="fw-bold">Location</span>
              <p class="text-body">
                {{ [report.countryName, report.stateName, report.cityName].filter(Boolean).join(', ') }}
              </p>
            </div>

            <div class="d-flex flex-column text-end text-muted small mt-2">
              <div>
                Posted: {{ report.postedAt }}
              </div>
            </div>
          </div>
        </div>    
    </div>
  </div>
  <!-- 이미지 확대 모달 -->
  <div 
    v-if="selectedImage" 
    class="image-modal-overlay" 
    @click="closeImageModal"
  >
    <div class="image-modal-content">
      <button class="image-modal-close" @click="closeImageModal">✕</button>
      <img :src="selectedImage" alt="Full size image" class="full-size-image" />
    </div>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { useBootstrapModal } from '@/composables/useBootstrapModal';

const modalRef = ref(null);
const { hide } = useBootstrapModal(modalRef);

const selectedImage = ref(null);

defineProps({
  report: {
    type: Object,
    required: true
  }
});

const openImageModal = (url) => {
  selectedImage.value = url;
};

const closeImageModal = () => {
  selectedImage.value = null;
};

const setupModalEventListener = () => {
  const modal = document.getElementById('reportDetailModal');

  if (modal) {
    modal.addEventListener('hide.bs.modal', () => {
      document.activeElement.blur();
    });
  }
}

onMounted(() => {
  setupModalEventListener()
})

</script>
<style scoped lang="scss">
/* 이미지 확대 모달 스타일 */
.image-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.9);
  z-index: 2000;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
}

.image-modal-content {
  position: relative;
  max-width: 90%;
  max-height: 90vh;
  display: flex;
  justify-content: center;
  align-items: center;
}

.image-modal-close {
  position: absolute;
  top: -40px;
  right: 0;
  background: white;
  border: none;
  border-radius: 50%;
  width: 35px;
  height: 35px;
  font-size: 24px;
  cursor: pointer;
  color: #333;
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 2001;
  transition: all 0.2s;

  &:hover {
    background: #f0f0f0;
    transform: scale(1.1);
  }
}

.full-size-image {
  max-width: 100%;
  max-height: 90vh;
  object-fit: contain;
  border-radius: 8px;
}
</style>