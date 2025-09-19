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
          <form @submit.prevent>
            <!-- Title -->
            <div class="mb-3">
              <label for="report-title" class="col-form-label fw-bold">
                <font-awesome-icon :icon="['fas', 'message']" class="modal-icon" />
                Title
              </label>
              <input
                type="text"
                :class="['form-control', { 'is-invalid': errors.title }]"
                id="report-title"
                v-model="form.title"
                maxlength="100"
                placeholder="Enter a title"
              />
              <div v-if="errors.title" class="text-danger small mt-1">
                {{ errors.title }}
              </div>
            </div>

            <!-- Scam Action / Context -->
            <div class="row mb-3">
              <div class="col-md-6">
                <label class="col-form-label fw-bold">
                  <font-awesome-icon :icon="['fas', 'triangle-exclamation']" class="modal-icon" />
                  Scam Action
                </label>
                <select
                  id="report-action"
                  v-model="form.scamAction"
                  :class="['form-select', { 'is-invalid': errors.scamAction }]"
                >
                  <option disabled value="">Select an action</option>
                  <option>Pickpocketing</option>
                  <option>Theft</option>
                  <option>Overcharging</option>
                  <option>Aggressive Solicitation</option>
                  <option>Fraud</option>
                  <option>System Tampering</option>
                  <option>Other Action</option>
                </select>
                <div v-if="errors.scamAction" class="text-danger small mt-1">
                  {{ errors.scamAction }}
                </div>
              </div>

              <div class="col-md-6">
                <label class="col-form-label fw-bold">
                  <font-awesome-icon :icon="['fas', 'triangle-exclamation']" class="modal-icon" />
                  Scam Context
                </label>
                <select
                  id="report-context"
                  v-model="form.scamContext"
                  :class="['form-select', { 'is-invalid': errors.scamContext }]"
                >
                  <option disabled value="">Select a context</option>
                  <option>Street/Public Area</option>
                  <option>Restaurant/Bar / Cafe</option>
                  <option>Transportation</option>
                  <option>Lodging</option>
                  <option>Tourist Attraction / Ticketed Venue</option>
                  <option>Financial Service</option>
                  <option>Other Context</option>
                </select>
                <div v-if="errors.scamContext" class="text-danger small mt-1">
                  {{ errors.scamContext }}
                </div>
              </div>
            </div>

            <!-- Image Upload -->
            <div class="mb-3">
              <label for="photo" class="form-label fw-bold">
                <font-awesome-icon :icon="['fas', 'camera']" class="modal-icon" />
                Upload Image (Optional)
              </label>
              <input
                ref="fileInput"
                class="form-control"
                type="file"
                id="photo"
                accept="image/*"
                @change="handleFileChange"
              />
              <div v-if="errors.imageFile" class="text-danger small mt-1">
                {{ errors.imageFile }}
              </div>
            </div>

            <!-- Description -->
            <div class="mb-3">
              <label for="report-description" class="col-form-label fw-bold">
                <font-awesome-icon :icon="['fas', 'message']" class="modal-icon" />
                Description
              </label>
              <textarea
                id="report-description"
                v-model="form.description"
                :class="['form-control', { 'is-invalid': errors.description }]"
                maxlength="500"
                rows="4"
                placeholder="Describe the scam in detail"
              ></textarea>
              <div v-if="errors.description" class="text-danger small mt-1">
                {{ errors.description }}
              </div>
            </div>

            <!-- Footer -->
            <div class="modal-footer">
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
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useAuthStore } from '@/stores/auth';
import { ref, reactive, onMounted } from 'vue';
import { useBootstrapModal } from '@/composables/useBootstrapModal';
import apiClient from '@/api/apiClient';

const authStore = useAuthStore();
const isLoggedIn = () => !!authStore.accessToken;

const modalRef = ref(null);
const { hide } = useBootstrapModal(modalRef);

const fileInput = ref(null);

const form = reactive({
  title: '',
  scamAction: '',
  scamContext: '',
  description: '',
  imageFile: null,
});

const errors = reactive({
  title: '',
  scamAction: '',
  scamContext: '',
  description: '',
  imageFile: '',
});

const submitMessage = ref('');
const submitStatus = ref('');
const isSubmitting = ref(false);

const handleFileChange = (event) => {
  const file = event.target.files[0];
  if (!file) return;

  const maxSizeInBytes = 5 * 1024 * 1024; // 5MB
  if (file.size > maxSizeInBytes) {
    errors.imageFile = 'Only images up to 5MB can be uploaded.';
    form.imageFile = null;
    event.target.value = '';
    return;
  }

  form.imageFile = file;
  errors.imageFile = '';
};

const extractJsonFromForm = () => {
  const { imageFile, ...json } = form;
  return new Blob([JSON.stringify(json)], { type: 'application/json' });
};

const checkForm = () => {
  let isValid = true;

  if (!form.title.trim()) {
    errors.title = 'Please enter a title.';
    isValid = false;
  } else errors.title = '';

  if (!form.scamAction) {
    errors.scamAction = 'Please select a scam action.';
    isValid = false;
  } else errors.scamAction = '';

  if (!form.scamContext) {
    errors.scamContext = 'Please select a scam context.';
    isValid = false;
  } else errors.scamContext = '';

  if (!form.description.trim()) {
    errors.description = 'Please provide a description.';
    isValid = false;
  } else errors.description = '';

  return isValid;
};


const resetForm = () => {
  form.title = '';
  form.scamAction = '';
  form.scamContext = '';
  form.description = '';
  form.imageFile = null;

  if (fileInput.value) {
    fileInput.value.value = '';
  }

  Object.keys(errors).forEach((key) => (errors[key] = ''));
  submitMessage.value = '';
  submitStatus.value = '';
};

const setupModalEventListener = () => {
  const modal = document.getElementById('reportFormModal');

  if (modal) {
    modal.addEventListener('hide.bs.modal', () => {
      if (modal.contains(document.activeElement)) {
        document.activeElement.blur();
      }
    });

    modal.addEventListener('hidden.bs.modal', resetForm);
  }
}

const submitForm = async () => {
  if (isSubmitting.value) return;
  if (!isLoggedIn()) {
    submitMessage.value = 'Please login.';
    submitStatus.value = 'error';
    return;
  }

  if (!checkForm()) {
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
    submitMessage.value = 'Submission failed. Please try again.';
    submitStatus.value = 'error';
  } finally {
    isSubmitting.value = false;
  }
};

const submitReportForm = async () => {
  const formData = new FormData();
  formData.append('request', extractJsonFromForm());
  if (form.imageFile) {
    formData.append('images', form.imageFile);
  }

  return apiClient.post('/reports', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
};

onMounted(setupModalEventListener);
</script>

<style scoped lang="scss">
.modal-icon {
  font-size: 95%;
}
</style>
