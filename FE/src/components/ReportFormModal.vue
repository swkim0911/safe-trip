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
              <div class="d-flex justify-content-between mt-1">
                <div v-if="errors.title" class="text-danger small">{{ errors.title }}</div>
                <div v-else></div>
                <small class="text-muted">{{ form.title.length }} / 100</small>
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
                  <option disabled value="">Select an Action</option>
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
                Location
              </label>

              <!-- Breadcrumb -->
              <div v-if="form.countryId" class="text-muted small mb-2">
                <span>{{ countrySearch }}</span>
                <span v-if="form.stateId"> → {{ stateSearch }}</span>
                <span v-if="form.cityId"> → {{ citySearch }}</span>
              </div>

              <!-- Country 검색 -->
              <div class="position-relative mb-2">
                <input
                  type="text"
                  v-model="countrySearch"
                  :class="['form-control', { 'is-invalid': errors.countryId }]"
                  placeholder="Search country..."
                  @focus="showCountryDropdown = true"
                  @blur="handleCountryBlur"
                  autocomplete="off"
                />
                <ul v-if="showCountryDropdown && filteredCountries.length > 0" class="country-dropdown">
                  <li
                    v-for="c in filteredCountries"
                    :key="c.id"
                    @mousedown="selectCountry(c)"
                  >
                    {{ c.name }}
                  </li>
                </ul>
                <div v-if="errors.countryId" class="text-danger small mt-1">{{ errors.countryId }}</div>
              </div>

              <!-- State (Country 선택 후 노출) -->
              <div v-if="form.countryId" class="position-relative mb-2">
                <input
                  type="text"
                  v-model="stateSearch"
                  :class="['form-control', { 'is-invalid': errors.stateId }]"
                  placeholder="Search state... (optional)"
                  @focus="showStateDropdown = true"
                  @blur="handleStateBlur"
                  autocomplete="off"
                />
                <ul v-if="showStateDropdown && filteredStates.length > 0" class="country-dropdown">
                  <li
                    v-for="s in filteredStates"
                    :key="s.id"
                    @mousedown="selectState(s)"
                  >
                    {{ s.name }}
                  </li>
                </ul>
                <div v-if="errors.stateId" class="text-danger small mt-1">{{ errors.stateId }}</div>
              </div>

              <!-- City (State 선택 후 노출) -->
              <div v-if="form.stateId">
                <div v-if="cities.length === 0" class="text-muted small fst-italic ps-1">
                  No cities available for this state.
                </div>
                <div v-else class="position-relative">
                  <input
                    type="text"
                    v-model="citySearch"
                    :class="['form-control', { 'is-invalid': errors.cityId }]"
                    placeholder="Search city... (optional)"
                    @focus="showCityDropdown = true"
                    @blur="handleCityBlur"
                    autocomplete="off"
                  />
                  <ul v-if="showCityDropdown && filteredCities.length > 0" class="country-dropdown">
                    <li
                      v-for="c in filteredCities"
                      :key="c.id"
                      @mousedown="selectCity(c)"
                    >
                      {{ c.name }}
                    </li>
                  </ul>
                  <div v-if="errors.cityId" class="text-danger small mt-1">{{ errors.cityId }}</div>
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
              <div v-if="imagePreviewUrl" class="mt-2">
                <div class="position-relative d-inline-block">
                  <img :src="imagePreviewUrl" alt="Preview" class="image-preview" />
                  <button type="button" class="btn-remove-image" @click="removeImage">✕</button>
                </div>
                <div class="text-muted small mt-1">{{ form.imageFile?.name }} · {{ (form.imageFile?.size / 1024).toFixed(1) }} KB</div>
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
              <div class="d-flex justify-content-between mt-1">
                <div v-if="errors.description" class="text-danger small">{{ errors.description }}</div>
                <div v-else></div>
                <small class="text-muted">{{ form.description.length }} / 500</small>
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
import { ref, reactive, onMounted, computed } from 'vue';
import { useBootstrapModal } from '@/composables/useBootstrapModal';
import apiClient from '@/api/apiClient';

const authStore = useAuthStore();
const isLoggedIn = () => !!authStore.accessToken;

const modalRef = ref(null);
const { hide } = useBootstrapModal(modalRef);

const fileInput = ref(null)
const imagePreviewUrl = ref(null);

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
  { id: 2, name: "Restaurant /Bar / Cafe" },
  { id: 3, name: "Transportation" },
  { id: 4, name: "Lodging" },
  { id: 5, name: "Tourist Attraction / Ticketed Venue" },
  { id: 6, name: "Financial Service" },
  { id: 7, name: "Other Context" },
]);

const countries = ref([])
const states = ref([])
const cities = ref([])

const countrySearch = ref('')
const stateSearch = ref('')
const citySearch = ref('')
const showCountryDropdown = ref(false)
const showStateDropdown = ref(false)
const showCityDropdown = ref(false)

const filteredCountries = computed(() => {
  if (!countrySearch.value) return countries.value
  return countries.value.filter(c =>
    c.name.toLowerCase().includes(countrySearch.value.toLowerCase())
  )
})

const filteredStates = computed(() => {
  if (!stateSearch.value) return states.value
  return states.value.filter(s =>
    s.name.toLowerCase().includes(stateSearch.value.toLowerCase())
  )
})

const filteredCities = computed(() => {
  if (!citySearch.value) return cities.value
  return cities.value.filter(c =>
    c.name.toLowerCase().includes(citySearch.value.toLowerCase())
  )
})

const selectCountry = (country) => {
  form.countryId = country.id
  countrySearch.value = country.name
  showCountryDropdown.value = false
  form.stateId = ''
  form.cityId = ''
  stateSearch.value = ''
  citySearch.value = ''
  states.value = []
  cities.value = []
  loadStates()
}

const selectState = (state) => {
  form.stateId = state.id
  stateSearch.value = state.name
  showStateDropdown.value = false
  form.cityId = ''
  citySearch.value = ''
  cities.value = []
  loadCities()
}

const selectCity = (city) => {
  form.cityId = city.id
  citySearch.value = city.name
  showCityDropdown.value = false
}

const handleCountryBlur = () => {
  showCountryDropdown.value = false
  const matched = countries.value.find(c => c.name === countrySearch.value)
  if (!matched) {
    countrySearch.value = ''
    form.countryId = ''
    form.stateId = ''
    form.cityId = ''
    stateSearch.value = ''
    citySearch.value = ''
    states.value = []
    cities.value = []
  }
}

const handleStateBlur = () => {
  showStateDropdown.value = false
  const matched = states.value.find(s => s.name === stateSearch.value)
  if (!matched) {
    stateSearch.value = ''
    form.stateId = ''
    form.cityId = ''
    citySearch.value = ''
    cities.value = []
  }
}

const handleCityBlur = () => {
  showCityDropdown.value = false
  const matched = cities.value.find(c => c.name === citySearch.value)
  if (!matched) {
    citySearch.value = ''
    form.cityId = ''
  }
}

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

  const response = await apiClient.get(`/states/${form.stateId}/cities`);
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
  if (imagePreviewUrl.value) URL.revokeObjectURL(imagePreviewUrl.value);
  imagePreviewUrl.value = URL.createObjectURL(file);
};

const removeImage = () => {
  form.imageFile = null;
  if (imagePreviewUrl.value) URL.revokeObjectURL(imagePreviewUrl.value);
  imagePreviewUrl.value = null;
  if (fileInput.value) fileInput.value.value = '';
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

  errors.stateId = '';
  errors.cityId = '';

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
  countrySearch.value = '';
  stateSearch.value = '';
  citySearch.value = '';
  states.value = [];
  cities.value = [];

  if (fileInput.value) fileInput.value.value = '';
  if (imagePreviewUrl.value) URL.revokeObjectURL(imagePreviewUrl.value);
  imagePreviewUrl.value = null;

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

.image-preview-wrap {
  display: inline-block;
}

.image-preview {
  display: block;
  max-width: 100%;
  max-height: 200px;
  object-fit: contain;
  border-radius: 8px;
  border: 1px solid #dee2e6;
  background: #f8f9fa;
}

.btn-remove-image {
  position: absolute;
  top: -8px;
  right: -8px;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: #dc3545;
  color: white;
  border: none;
  font-size: 11px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;

  &:hover {
    background: #bb2d3b;
  }
}

.country-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  z-index: 1000;
  max-height: 200px;
  overflow-y: auto;
  background: white;
  border: 1px solid #dee2e6;
  border-top: none;
  border-radius: 0 0 4px 4px;
  list-style: none;
  margin: 0;
  padding: 0;

  li {
    padding: 8px 12px;
    cursor: pointer;

    &:hover {
      background-color: #f0f4ff;
    }
  }
}
</style>
