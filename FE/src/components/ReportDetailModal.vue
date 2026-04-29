<template>
  <div class="modal fade" ref="modalRef" id="reportDetailModal" data-bs-backdrop="static" data-bs-keyboard="false" tabindex="-1" aria-labelledby="staticBackdropLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered modal-dialog-scrollable modal-lg">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title" id="staticBackdropLabel">Travel Scam Report</h5>
          <button type="button" class="btn-close" @click="closeModal" aria-label="Close"></button>
        </div>

        <div class="modal-body">
          <div class="d-flex justify-content-between align-items-start mb-3">
            <div>
              <h5 class="fw-bold mb-2">{{ report.title }}</h5>
              <span class="badge bg-danger me-1">{{ report.scamAction }}</span>
              <span class="badge bg-warning text-dark">{{ report.scamContext }}</span>
            </div>

            <div class="text-end small ms-3 flex-shrink-0">
              <div class="d-flex justify-content-end mb-1">
                <span
                  v-if="report.source === 'SAFETRIP'"
                  class="badge rounded-pill border border-primary text-primary fs-6 px-2 py-1 d-inline-flex align-items-center gap-1"
                >
                  <font-awesome-icon :icon="['fas', 'user-shield']" />
                  User Report
                </span>
                <span
                  v-else
                  class="badge rounded-pill bg-light text-dark border fs-6 px-2 py-1 d-inline-flex align-items-center gap-1"
                >
                  <font-awesome-icon :icon="['fas', 'database']" />
                  AI-Assisted Report
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
                  <font-awesome-icon :icon="['fas', 'arrow-up-right-from-square']" class="me-1" />
                  Original Source
                </a>
              </div>

              <!-- Report Inaccuracy 영역 -->
              <div v-if="report.source !== 'SAFETRIP'" class="mt-2">
                <span v-if="alreadySubmitted" class="text-muted small d-inline-flex align-items-center gap-1">
                  <font-awesome-icon :icon="['fas', 'circle-check']" />
                  Feedback submitted
                </span>
                <button
                  v-else-if="!showInaccuracyForm"
                  type="button"
                  class="btn btn-sm btn-outline-warning"
                  @click="handleInaccuracyClick"
                >
                  <font-awesome-icon :icon="['fas', 'triangle-exclamation']" class="me-1" />
                  Report Inaccuracy
                </button>
              </div>
            </div>
          </div>

          <!-- 인라인 신고 폼 -->
          <div v-if="showInaccuracyForm" class="border rounded p-3 mb-3 bg-light">
            <p class="fw-bold small mb-2 d-flex align-items-center gap-1">
              <font-awesome-icon :icon="['fas', 'triangle-exclamation']" />
              Report Inaccuracy
            </p>
            <div class="mb-2">
              <select v-model="inaccuracyReason" class="form-select form-select-sm">
                <option value="" disabled>Select a reason</option>
                <option value="WRONG_LOCATION">Wrong Location</option>
                <option value="WRONG_SCAM_TYPE">Wrong Scam Type</option>
                <option value="NOT_A_SCAM">Not a Scam</option>
                <option value="INACCURATE_CONTENT">Inaccurate Content</option>
                <option value="OTHER">Other</option>
              </select>
            </div>
            <div class="mb-2">
              <textarea
                v-model="inaccuracyDescription"
                class="form-control form-control-sm"
                rows="2"
                placeholder="Describe the inaccuracy... (optional)"
                maxlength="500"
              ></textarea>
              <div class="text-end text-muted" style="font-size: 0.75rem;">{{ inaccuracyDescription.length }} / 500</div>
            </div>
            <div class="d-flex justify-content-end gap-2">
              <button type="button" class="btn btn-sm btn-secondary" @click="cancelInaccuracy">Cancel</button>
              <button
                type="button"
                class="btn btn-sm btn-warning"
                :disabled="!inaccuracyReason || isSubmitting"
                @click="submitInaccuracy"
              >
                <span v-if="isSubmitting" class="spinner-border spinner-border-sm me-1"></span>
                Submit
              </button>
            </div>
          </div>

          <hr class="my-2" />

          <div class="mb-3">
            <span class="fw-bold">{{ report.source === 'SAFETRIP' ? 'Description' : 'Summary' }}</span>
            <p class="text-body mt-1" style="white-space: pre-wrap; overflow-wrap: break-word;">{{ report.content }}</p>
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

          <hr class="my-2" />

          <div class="mb-3">
            <span class="fw-bold">Location</span>
            <p class="text-body">
              {{ [report.countryName, report.stateName, report.cityName].filter(Boolean).join(', ') }}
            </p>
          </div>

          <div class="text-end text-muted small mt-2">
            Posted: {{ report.postedAt }}
          </div>

          <!-- 댓글 -->
          <hr class="my-3" />
          <CommentSection
            :report-id="report.reportId"
            :report-type="report.source === 'SAFETRIP' ? 'USER' : 'EXTERNAL'"
            @request-login="handleRequestLogin"
          />
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
import { ref, watch, onMounted } from 'vue'
import { useBootstrapModal } from '@/composables/useBootstrapModal';
import { useAuthStore } from '@/stores/auth';
import { useMapStore } from '@/stores/map';
import { useToast } from '@/composables/useToast';
import apiClient from '@/api/apiClient';
import CommentSection from './CommentSection.vue';

const modalRef = ref(null);
const { hide } = useBootstrapModal(modalRef);

const authStore = useAuthStore();
const mapStore = useMapStore();
const toast = useToast();
const { show: openAuthModal } = useBootstrapModal('#authFormModal');
const selectedImage = ref(null);

const alreadySubmitted = ref(false);
const showInaccuracyForm = ref(false);
const inaccuracyReason = ref('');
const inaccuracyDescription = ref('');
const isSubmitting = ref(false);

const props = defineProps({
  report: {
    type: Object,
    required: true
  }
});

watch(() => props.report.reportId, async (reportId) => {
  alreadySubmitted.value = false;
  showInaccuracyForm.value = false;
  inaccuracyReason.value = '';
  inaccuracyDescription.value = '';
  if (!reportId || props.report.source === 'SAFETRIP' || !authStore.accessToken) return;
  try {
    const res = await apiClient.get(`/external-reports/${reportId}/inaccuracies/my`);
    alreadySubmitted.value = res.data.result;
  } catch {
    alreadySubmitted.value = false;
  }
});

const cancelInaccuracy = () => {
  showInaccuracyForm.value = false;
  inaccuracyReason.value = '';
  inaccuracyDescription.value = '';
};

const handleInaccuracyClick = () => {
  if (!authStore.accessToken) {
    toast.show('Login is required to report inaccuracies.');
    mapStore.requestReopenReportDetail();
    hide();
    openAuthModal();
    return;
  }
  showInaccuracyForm.value = true;
};

const submitInaccuracy = async () => {
  if (!inaccuracyReason.value) return;
  isSubmitting.value = true;
  try {
    await apiClient.post(`/external-reports/${props.report.reportId}/inaccuracies`, {
      reason: inaccuracyReason.value,
      description: inaccuracyDescription.value || null,
    });
    alreadySubmitted.value = true;
    showInaccuracyForm.value = false;
  } catch (e) {
    console.error('Failed to submit inaccuracy report', e);
  } finally {
    isSubmitting.value = false;
  }
};

const handleRequestLogin = () => {
  toast.show('Login is required to write a comment.');
  mapStore.requestReopenReportDetail();
  hide();
  openAuthModal();
};

const closeModal = () => {
  cancelInaccuracy();
  hide();
};

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
};

onMounted(() => {
  setupModalEventListener();
});
</script>

<style scoped lang="scss">
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
