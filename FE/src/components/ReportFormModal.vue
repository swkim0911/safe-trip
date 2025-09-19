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
                  v-model="form.scamActionId"
                  :class="['form-select', { 'is-invalid': errors.scamActionId }]"
                >
                  <option disabled value="">Select a Action</option>
                  <option v-for="a in scamActions" :key="a.id" :value="a.id">
                    {{ a.name }}
                  </option>
                </select>
                <div v-if="errors.scamActionId" class="text-danger small mt-1">
                  {{ errors.scamActionId }}
                </div>
              </div>

              <div class="col-md-6">
                <label class="col-form-label fw-bold">
                  <font-awesome-icon :icon="['fas', 'triangle-exclamation']" class="modal-icon" />
                  Scam Context
                </label>
                <select
                  id="report-context"
                  v-model="form.scamContextId"
                  :class="['form-select', { 'is-invalid': errors.scamContextId }]"
                >
                  <option disabled value="">Select a context</option>
                  <option v-for="c in scamContexts" :key="c.id" :value="c.id">
                    {{ c.name }}
                  </option>
                </select>
                <div v-if="errors.scamContextId" class="text-danger small mt-1">
                  {{ errors.scamContextId }}
                </div>
              </div>
            </div>
            <!-- Location -->
            <div class="mb-3">
              <label class="col-form-label fw-bold">
                <font-awesome-icon :icon="['fas', 'map-location-dot']" class="modal-icon" />
                Where did it happened
              </label>
              <div class="row g-2">
                <div class="col-md-4">
                  <select
                    v-model="form.countryId"
                    class="form-select"
                    :class="{ 'is-invalid': errors.countryId }"
                    @change="loadStates"
                  >
                    <option disabled value="">Select a country</option>
                    <option v-for="c in countries" :key="c.id" :value="c.id">
                      {{ c.name }}
                    </option>
                  </select>
                  <div v-if="errors.countryId" class="text-danger small mt-1">
                    {{ errors.countryId }}
                  </div>
                </div>

                <div class="col-md-4">
                  <select
                    v-model="form.stateId"
                    class="form-select"
                    :disabled="!form.countryId"
                    :class="{ 'is-invalid': errors.stateId }"
                    @change="loadCities"
                  >
                    <option disabled value="">Select a state</option>
                    <option v-for="s in states" :key="s.id" :value="s.id">
                      {{ s.name }}
                    </option>
                  </select>
                  <div v-if="errors.stateId" class="text-danger small mt-1">
                    {{ errors.stateId }}
                  </div>
                </div>

                <div class="col-md-4">
                  <select
                    v-model="form.cityId"
                    class="form-select"
                    :disabled="!form.stateId"
                    :class="{ 'is-invalid': errors.cityId }"
                  >
                    <option disabled value="">Select a city</option>
                    <option v-for="c in cities" :key="c.id" :value="c.id">
                      {{ c.name }}
                    </option>
                  </select>
                  <div v-if="errors.city" class="text-danger small mt-1">
                    {{ errors.cityId }}
                  </div>
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
                placeholder="Please describe the scam in detail"
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

const scamActions = ref([
  { id: 1, name: "Pickpocketing" },
  { id: 2, name: "Theft" },
  { id: 3, name: "Overcharging" },
  { id: 4, name: "Aggressive Solicitation" },
  { id: 5, name: "Fraud" },
  { id: 6, name: "System Tampering" },
  { id: 7, name: "Other Action" },
]);

const scamContexts = ref([
  { id: 1, name: "Street / Public Area" },
  { id: 2, name: "Restaurant/Bar / Cafe" },
  { id: 3, name: "Transportation" },
  { id: 4, name: "Lodging" },
  { id: 5, name: "Tourist Attraction / Ticketed Venue" },
  { id: 6, name: "Financial Service" },
  { id: 7, name: "Other Context" },
]);

const countries = ref([])
const states = ref([])
const cities = ref([])

const form = reactive({
  title: '',
  scamActionId: '',
  scamContextId: '',
  description: '',
  imageFile: null,
  countryId: '',   
  stateId: '',     
  cityId: '',      
});

const errors = reactive({
  title: '',
  scamActionId: '',
  scamContextId: '',
  description: '',
  imageFile: '',
  countryId: '',
  stateId: '',
  cityId: '',
});

const submitMessage = ref('');
const submitStatus = ref('');
const isSubmitting = ref(false);

const loadCountries = async () => {
  const response = await apiClient.get('/countries');
  countries.value = response.data.result.countries;
}


const loadStates = async () => {
  if (!form.countryId) return;
  form.stateId = '';
  form.cityId = '';
  states.value = [];
  cities.value = [];

  const response = await apiClient.get(`/countries/${form.countryId}/states`);
  states.value = response.data.result.states;
};

const loadCities = async () => {
  if (!form.countryId || !form.stateId) return
  form.cityId = '';
  cities.value = [];

  const response = await apiClient.get(`/countries/states/${form.stateId}/cities`);
  cities.value = response.data.result.cities;
}


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

  if (!form.scamActionId) {
    errors.scamActionId = 'Please select a scam action.';
    isValid = false;
  } else errors.scamActionId = '';

  if (!form.scamContextId) {
    errors.scamContextId = 'Please select a scam context.';
    isValid = false;
  } else errors.scamContextId = '';

  if (!form.countryId) {
    errors.countryId = 'Please select a country.';
    isValid = false;
  } else errors.countryId = '';

  if (!form.stateId) {
    errors.stateId = 'Please select a state.';
    isValid = false;
  } else errors.stateId = '';

  if (!form.cityId) {
    errors.cityId = 'Please select a city.';
    isValid = false;
  } 

  if (!form.description.trim()) {
    errors.description = 'Please provide a description.';
    isValid = false;
  } else errors.description = '';

  return isValid;
};


const resetForm = () => {
  form.title = '';
  form.scamActionId = '';
  form.scamContextId = '';
  form.countryId = '';  
  form.stateId = '';    
  form.cityId = '';    
  form.description = '';
  form.imageFile = null;

  if (fileInput.value) {
    fileInput.value.value = '';
  }

  Object.keys(errors).forEach((key) => (errors[key] = ''));
};

const closeForm = () => {
  resetForm();
  submitMessage.value = '';
  submitStatus.value = '';
}

const setupModalEventListener = () => {
  const modal = document.getElementById('reportFormModal');

  if (modal) {
    modal.addEventListener('hide.bs.modal', () => {
      if (modal.contains(document.activeElement)) {
        document.activeElement.blur();
      }
    });

    modal.addEventListener('hidden.bs.modal', closeForm);
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
    console.log(submitMessage.value);
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

  return apiClient.post('/user-reports', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
};

onMounted(() => {
  setupModalEventListener(); 
  loadCountries(); 
});
</script>

<style scoped lang="scss">
.modal-icon {
  font-size: 95%;
}
</style>
