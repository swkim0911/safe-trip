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
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title" id="staticBackdropLabel">Share your story</h5>
          <button type="button" class="btn-close" @click="hide" aria-label="Close"></button>
        </div>
        <div class="modal-body">
          <ReportFormFields ref="formRef" />

          <!-- Footer -->
          <div class="modal-footer px-0">
            <div
              v-if="submitMessage"
              :class="['me-3 fw-bold', submitStatus === 'success' ? 'text-success' : 'text-danger']"
            >
              {{ submitMessage }}
            </div>
            <button type="button" class="btn btn-secondary" @click="hide">Close</button>
            <button :disabled="isSubmitting" type="button" class="btn btn-primary" @click="submitForm">
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
import ReportFormFields from '@/components/ReportFormFields.vue';
import apiClient from '@/api/apiClient';

const authStore = useAuthStore();
const isLoggedIn = () => !!authStore.accessToken;

const modalRef = ref(null);
const { hide } = useBootstrapModal(modalRef);
const formRef = ref(null);

const submitMessage = ref('');
const submitStatus = ref('');
const isSubmitting = ref(false);

const closeForm = () => {
  formRef.value?.reset();
  submitMessage.value = '';
  submitStatus.value = '';
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
    submitStatus.value = 'error';
    return;
  }

  if (!formRef.value.validate()) {
    submitMessage.value = 'Invalid input. Please check your entries.';
    submitStatus.value = 'error';
    return;
  }

  isSubmitting.value = true;
  try {
    const data = formRef.value.getFormData();
    const imageFile = formRef.value.getImageFile();

    const formData = new FormData();
    formData.append('request', new Blob([JSON.stringify(data)], { type: 'application/json' }));
    if (imageFile) formData.append('images', imageFile);

    await apiClient.post('/user-reports', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });

    submitMessage.value = 'Your report has been successfully submitted.';
    submitStatus.value = 'success';
    formRef.value.reset();
  } catch (error) {
    console.error(error);
    submitMessage.value = 'Submission failed. Please try again.';
    submitStatus.value = 'error';
  } finally {
    isSubmitting.value = false;
  }
};

import { onMounted } from 'vue';
onMounted(setupModalEventListener);
</script>
