<template>
  <div class="modal fade" ref="modalRef" id="reportDetailModal" data-bs-backdrop="static" data-bs-keyboard="false" tabindex="-1" aria-labelledby="staticBackdropLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered modal-dialog-scrollable modal-xl">
      <div class="modal-content report-modal">
        <div class="modal-header report-modal-header">
          <div>
            <h5 class="modal-title report-modal-title" id="staticBackdropLabel">Travel Note</h5>
          </div>
          <button type="button" class="btn-close" @click="closeModal" aria-label="Close"></button>
        </div>

        <div class="modal-body report-modal-body">
          <section class="report-hero">
            <div class="report-main">
              <div class="report-title-row">
                <div class="report-meta-line">
                  <span>{{ report.postedAt }}</span>
                </div>
                <div class="report-source-actions">
                  <span
                    v-if="report.source === 'SAFETRIP'"
                    class="source-badge source-user"
                  >
                    <font-awesome-icon :icon="['fas', 'user-shield']" />
                    Traveler note
                  </span>
                  <span
                    v-else
                    class="source-badge source-ai"
                  >
                    <font-awesome-icon :icon="['fas', 'database']" />
                    AI-Assisted
                  </span>
                  <span v-if="report.source === 'SAFETRIP'" class="source-author">
                    by {{ report.nickname }}
                  </span>
                  <a v-if="report.sourceUrl" :href="report.sourceUrl" target="_blank" class="source-text-link">
                    <font-awesome-icon :icon="['fas', 'arrow-up-right-from-square']" />
                    {{ sourceLinkLabel }}
                  </a>
                  <button
                    v-if="report.source !== 'SAFETRIP' && !alreadySubmitted && !showInaccuracyForm"
                    type="button"
                    class="correction-text-btn"
                    @click="handleInaccuracyClick"
                  >
                    <font-awesome-icon :icon="['fas', 'triangle-exclamation']" />
                    Suggest edit
                  </button>
                  <span v-else-if="report.source !== 'SAFETRIP' && alreadySubmitted" class="feedback-icon">
                    <font-awesome-icon :icon="['fas', 'circle-check']" />
                  </span>
                </div>
              </div>
              <h2 class="report-title">{{ report.title }}</h2>
              <div class="report-tags">
                <span class="report-chip action-chip">{{ report.scamAction }}</span>
                <span class="report-chip context-chip">{{ report.scamContext }}</span>
              </div>
            </div>
          </section>

          <!-- 인라인 신고 폼 -->
          <div v-if="showInaccuracyForm" class="correction-panel">
            <p class="correction-title">
              <font-awesome-icon :icon="['fas', 'triangle-exclamation']" />
              Suggest a quick fix
            </p>
            <div class="mb-2">
              <select v-model="inaccuracyReason" class="form-select form-select-sm">
                <option value="" disabled>Choose a reason</option>
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
                placeholder="Tell us what looks off... (optional)"
                maxlength="500"
              ></textarea>
              <div class="text-end text-muted" style="font-size: 0.75rem;">{{ inaccuracyDescription.length }} / 500</div>
            </div>
            <div class="d-flex justify-content-end gap-2">
              <button type="button" class="btn btn-sm btn-secondary" @click="cancelInaccuracy">Cancel</button>
              <button
                type="button"
                class="btn btn-sm btn-primary"
                :disabled="!inaccuracyReason || isSubmitting"
                @click="submitInaccuracy"
              >
                <span v-if="isSubmitting" class="spinner-border spinner-border-sm me-1"></span>
                Send
              </button>
            </div>
          </div>

          <section class="report-section">
            <div class="section-label">{{ report.source === 'SAFETRIP' ? 'Traveler Story' : 'Summary' }}</div>
            <p class="report-copy">{{ report.content }}</p>
          </section>

          <!-- 이미지 표시 -->
          <section v-if="normalizedImageUrls.length > 0" class="report-section">
            <div class="section-label">Photos</div>
            <div class="report-photo-grid">
              <div
                v-for="(url, index) in normalizedImageUrls"
                :key="index"
                class="report-photo-frame"
              >
                <img
                  :src="url"
                  :alt="`Travel note photo ${index + 1}`"
                  class="report-photo"
                  @click="openImageModal(url)"
                />
              </div>
            </div>
          </section>

          <section class="report-section location-section">
            <div>
              <div class="section-label">Location</div>
              <p class="location-copy">
                <font-awesome-icon :icon="['fas', 'location-dot']" class="location-icon" />
              {{ [report.cityName, report.stateName, report.countryName].filter(Boolean).join(', ') }}
              </p>
            </div>
          </section>

          <!-- 댓글 -->
          <section class="report-section comments-section">
            <CommentSection
              :report-id="report.reportId"
              :report-type="report.source === 'SAFETRIP' ? 'USER' : 'EXTERNAL'"
              @request-login="handleRequestLogin"
            />
          </section>
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
import { ref, watch, onMounted, computed } from 'vue'
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

const normalizedImageUrls = computed(() => {
  if (!Array.isArray(props.report.imageUrls)) return [];
  return props.report.imageUrls.filter(Boolean);
});

const sourceLinkLabel = computed(() =>
  props.report.source === 'REDDIT' ? 'Reddit source' : 'Original source'
);

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
  toast.show('Please log in to suggest an edit.');
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
  toast.show('Please log in to join the conversation.');
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
.report-modal {
  overflow: hidden;
  border: 1px solid var(--safetrip-border);
  border-radius: 14px;
  background: var(--safetrip-page);
}

.report-modal-header {
  align-items: flex-start;
  background: #fffdf8;
  border-bottom: 1px solid var(--safetrip-border);
  padding: 22px 28px;
}

.report-modal-title {
  color: var(--safetrip-primary);
  font-weight: 800;
}

.report-modal-body {
  padding: 24px 28px 28px;
}

.report-hero {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 18px;
}

.report-main,
.report-section,
.correction-panel {
  background: var(--safetrip-surface);
  border: 1px solid var(--safetrip-border);
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(36, 49, 58, 0.06);
}

.report-main {
  padding: 22px;
}

.report-title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 14px;
  margin-bottom: 10px;
}

.report-meta-line {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--safetrip-muted);
  font-size: 0.82rem;
  padding-top: 6px;
}

.report-source-actions {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 8px;
  flex-shrink: 0;
}

.report-title {
  margin: 0 0 16px;
  color: var(--safetrip-text);
  font-size: 1.38rem;
  font-weight: 700;
  line-height: 1.35;
}

.report-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.report-chip {
  display: inline-flex;
  align-items: center;
  padding: 6px 11px;
  border-radius: 999px;
  font-size: 0.86rem;
  font-weight: 700;
}

.action-chip {
  color: #8f432f;
  background: #fff1ec;
  border: 1px solid #f3c6b8;
}

.context-chip {
  color: #73511f;
  background: #fff7df;
  border: 1px solid #ecd39a;
}

.source-badge {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 6px 10px;
  border-radius: 999px;
  font-size: 0.82rem;
  font-weight: 700;
}

.source-user {
  color: #1f7f74;
  background: var(--safetrip-primary-soft);
  border: 1px solid #b9e4dc;
}

.source-ai {
  color: #4b5d62;
  background: #f6f3ed;
  border: 1px solid #ded6ca;
}

.source-text-link,
.correction-text-btn,
.feedback-icon {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  justify-content: center;
  min-height: 32px;
  padding: 0 10px;
  border-radius: 999px;
  border: 1px solid var(--safetrip-border);
  background: #fffdf8;
  font-size: 0.82rem;
  font-weight: 700;
}

.source-author {
  color: var(--safetrip-muted);
  font-size: 0.82rem;
  font-weight: 600;
}

.source-text-link {
  color: var(--safetrip-primary);
  text-decoration: none;

  &:hover {
    background: var(--safetrip-primary-soft);
    color: var(--safetrip-primary-hover);
  }
}

.correction-text-btn {
  color: #9a562a;
  border-color: #e9b872;
  background: #fff8e7;

  &:hover,
  &:focus {
    color: #8c4d25;
    background: #d99a45;
    border-color: #d99a45;
  }
}

.feedback-icon {
  color: var(--safetrip-primary);
  background: var(--safetrip-primary-soft);
  border-color: #b9e4dc;
}

.correction-panel {
  margin-bottom: 18px;
  padding: 16px;
}

.correction-title {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 10px;
  color: var(--safetrip-text);
  font-size: 0.9rem;
  font-weight: 700;
}

.report-section {
  margin-top: 14px;
  padding: 20px 22px;
}

.section-label {
  margin-bottom: 10px;
  color: var(--safetrip-muted);
  font-size: 0.78rem;
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.report-copy {
  margin: 0;
  color: var(--safetrip-text);
  font-size: 1rem;
  line-height: 1.75;
  overflow-wrap: break-word;
  white-space: pre-wrap;
}

.report-photo-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, max-content));
  gap: 12px;
}

.report-photo-frame {
  display: flex;
  align-items: center;
  justify-content: center;
  width: min(100%, 520px);
  min-height: 220px;
}

.report-photo {
  display: block;
  max-width: 100%;
  max-height: 420px;
  object-fit: contain;
  border-radius: 8px;
  cursor: pointer;
}

.location-section {
  padding: 16px 22px;
}

.location-copy {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  color: var(--safetrip-text);
  font-size: 1rem;
}

.location-icon {
  color: var(--safetrip-primary);
}

.comments-section {
  padding-top: 18px;
}

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

@media (max-width: 768px) {
  .report-modal-header,
  .report-modal-body {
    padding-left: 18px;
    padding-right: 18px;
  }

  .report-title {
    font-size: 1.3rem;
  }

  .report-title-row {
    flex-direction: column;
  }

  .report-source-actions {
    justify-content: flex-start;
  }
}
</style>
