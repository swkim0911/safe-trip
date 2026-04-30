<template>
  <div
    class="modal fade"
    ref="modalRef"
    id="reportFormModal"
    data-bs-backdrop="static"
    data-bs-keyboard="false"
    tabindex="-1"
    aria-labelledby="staticBackdropLabel"
    aria-hidden="true"
  >
    <div class="modal-dialog modal-dialog-centered modal-dialog-scrollable modal-lg">
      <div class="modal-content report-form-modal">
        <div class="modal-header report-form-header">
          <div>
            <div class="form-kicker">Traveler report</div>
            <h5 class="modal-title" id="staticBackdropLabel">Share your story</h5>
          </div>
          <button type="button" class="btn-close" @click="hide" aria-label="Close"></button>
        </div>
        <div class="modal-body report-form-body">
          <ReportFormFields ref="formRef" />

          <!-- Footer -->
          <div class="report-form-footer">
            <div v-if="submitMessage" class="form-message">
              {{ submitMessage }}
            </div>
            <button type="button" class="btn form-secondary-btn" @click="hide">Close</button>
            <button :disabled="isSubmitting" type="button" class="btn form-primary-btn" @click="submitForm">
              Send
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useBootstrapModal } from '@/composables/useBootstrapModal';
import { useAuthStore } from '@/stores/auth';
import { useMapStore } from '@/stores/map';
import { useToast } from '@/composables/useToast';
import ReportFormFields from '@/components/ReportFormFields.vue';
import apiClient from '@/api/apiClient';

const authStore = useAuthStore();
const mapStore = useMapStore();
const isLoggedIn = () => !!authStore.accessToken;

const modalRef = ref(null);
const { hide } = useBootstrapModal(modalRef);
const formRef = ref(null);
const toast = useToast();

const submitMessage = ref('');
const isSubmitting = ref(false);

const closeForm = () => {
  formRef.value?.reset();
  submitMessage.value = '';
};

const setupModalEventListener = () => {
  const modal = document.getElementById('reportFormModal');
  if (modal) {
    modal.addEventListener('hide.bs.modal', () => {
      if (modal.contains(document.activeElement)) document.activeElement.blur();
    });
    modal.addEventListener('hidden.bs.modal', closeForm);
  }
};

const submitForm = async () => {
  if (isSubmitting.value) return;
  if (!isLoggedIn()) {
    submitMessage.value = 'Please login.';
    return;
  }

  if (!formRef.value.validate()) {
    submitMessage.value = 'Invalid input. Please check your entries.';
    return;
  }

  isSubmitting.value = true;
  try {
    const data = formRef.value.getFormData();
    const imageFile = formRef.value.getImageFile();

    const formData = new FormData();
    formData.append('request', new Blob([JSON.stringify(data)], { type: 'application/json' }));
    if (imageFile) formData.append('images', imageFile);

    const res = await apiClient.post('/user-reports', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });

    hide();
    toast.show('Your report has been successfully submitted.');
    mapStore.requestOpenUserReport(res.data.result);
  } catch (error) {
    console.error(error);
    submitMessage.value = 'Submission failed. Please try again.';
  } finally {
    isSubmitting.value = false;
  }
};

import { onMounted } from 'vue';
onMounted(setupModalEventListener);
</script>

<style scoped lang="scss">
.report-form-modal {
  overflow: hidden;
  border: 1px solid var(--safetrip-border);
  border-radius: 14px;
  background: var(--safetrip-page);
}

.report-form-header {
  background: #fffdf8;
  border-bottom: 1px solid var(--safetrip-border);
  padding: 20px 24px;
}

.form-kicker {
  margin-bottom: 2px;
  color: var(--safetrip-primary);
  font-size: 0.75rem;
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.report-form-body {
  padding: 22px 24px 24px;
}

.report-form-footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 18px;
  padding-top: 18px;
  border-top: 1px solid var(--safetrip-border);
}

.form-message {
  margin-right: auto;
  color: #b6523b;
  font-size: 0.9rem;
  font-weight: 700;
}

.form-primary-btn {
  color: #fff;
  background: var(--safetrip-primary);
  border-color: var(--safetrip-primary);

  &:hover,
  &:focus {
    color: #fff;
    background: var(--safetrip-primary-hover);
    border-color: var(--safetrip-primary-hover);
  }

  &:disabled {
    background: #a7cfc8;
    border-color: #a7cfc8;
    opacity: 1;
  }
}

.form-secondary-btn {
  color: var(--safetrip-muted);
  background: #fffdf8;
  border-color: var(--safetrip-border);

  &:hover,
  &:focus {
    color: var(--safetrip-text);
    background: #f7f1e8;
    border-color: #d8cec2;
  }
}
</style>
