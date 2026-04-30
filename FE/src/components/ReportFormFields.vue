<template>
  <form class="report-form-fields" @submit.prevent>
    <!-- Title -->
    <section class="form-section">
      <label class="form-section-label">
        <font-awesome-icon :icon="['fas', 'message']" class="modal-icon" />
        Title
      </label>
      <input
        type="text"
        :class="['form-control', { 'is-invalid': errors.title }]"
        v-model="form.title"
        maxlength="100"
      />
      <div class="d-flex justify-content-between mt-1">
        <div v-if="errors.title" class="text-danger small">{{ errors.title }}</div>
        <div v-else></div>
        <small class="text-muted">{{ form.title.length }} / 100</small>
      </div>
    </section>

    <!-- Scam Action / Context -->
    <section class="form-section">
      <div class="form-section-label">
        <font-awesome-icon :icon="['fas', 'triangle-exclamation']" class="modal-icon" />
        Report details
      </div>
      <div class="row">
        <div class="col-md-6">
          <label class="field-label">
            Scam Action
          </label>
          <select v-model="form.scamActionId" :class="['form-select', { 'is-invalid': errors.scamActionId }]">
            <option disabled value="">Select an Action</option>
            <option v-for="a in scamActions" :key="a.id" :value="a.id">{{ a.name }}</option>
          </select>
          <div v-if="errors.scamActionId" class="text-danger small mt-1">{{ errors.scamActionId }}</div>
        </div>
        <div class="col-md-6">
          <label class="field-label">
            Scam Context
          </label>
          <select v-model="form.scamContextId" :class="['form-select', { 'is-invalid': errors.scamContextId }]">
            <option disabled value="">Select a context</option>
            <option v-for="c in scamContexts" :key="c.id" :value="c.id">{{ c.name }}</option>
          </select>
          <div v-if="errors.scamContextId" class="text-danger small mt-1">{{ errors.scamContextId }}</div>
        </div>
      </div>
    </section>

    <!-- Location -->
    <section class="form-section">
      <label class="form-section-label">
        <font-awesome-icon :icon="['fas', 'map-location-dot']" class="modal-icon" />
        Location
      </label>

      <div v-if="form.countryId" class="text-muted small mb-2">
        <span>{{ countrySearch }}</span>
        <span v-if="form.stateId"> → {{ stateSearch }}</span>
        <span v-if="form.cityId"> → {{ citySearch }}</span>
      </div>

      <!-- Country -->
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
        <ul v-if="showCountryDropdown && filteredCountries.length > 0" class="location-dropdown">
          <li v-for="c in filteredCountries" :key="c.id" @mousedown="selectCountry(c)">{{ c.name }}</li>
        </ul>
        <div v-if="errors.countryId" class="text-danger small mt-1">{{ errors.countryId }}</div>
      </div>

      <!-- State -->
      <div v-if="form.countryId" class="position-relative mb-2">
        <input
          type="text"
          v-model="stateSearch"
          class="form-control"
          placeholder="Search state... (optional)"
          @focus="showStateDropdown = true"
          @blur="handleStateBlur"
          autocomplete="off"
        />
        <ul v-if="showStateDropdown && filteredStates.length > 0" class="location-dropdown">
          <li v-for="s in filteredStates" :key="s.id" @mousedown="selectState(s)">{{ s.name }}</li>
        </ul>
      </div>

      <!-- City -->
      <div v-if="form.stateId">
        <div v-if="cities.length === 0" class="text-muted small fst-italic ps-1">
          No cities available for this state.
        </div>
        <div v-else class="position-relative">
          <input
            type="text"
            v-model="citySearch"
            class="form-control"
            placeholder="Search city... (optional)"
            @focus="showCityDropdown = true"
            @blur="handleCityBlur"
            autocomplete="off"
          />
          <ul v-if="showCityDropdown && filteredCities.length > 0" class="location-dropdown">
            <li v-for="c in filteredCities" :key="c.id" @mousedown="selectCity(c)">{{ c.name }}</li>
          </ul>
        </div>
      </div>
    </section>

    <!-- Image Upload -->
    <section class="form-section">
      <label class="form-section-label">
        <font-awesome-icon :icon="['fas', 'camera']" class="modal-icon" />
        Upload Image (Optional)
      </label>

      <!-- 기존 이미지 (새 파일 선택 전까지 표시) -->
      <div v-if="existingImageUrl && !removeExistingImage && !imagePreviewUrl" class="current-image-panel">
        <div class="image-preview-frame">
          <img :src="existingImageUrl" alt="Current image" class="image-preview" />
          <button type="button" class="btn-remove-image" @click="markExistingImageForRemoval">×</button>
        </div>
        <div class="image-help-text">Current image · Upload a new file to replace, or remove it.</div>
      </div>

      <div v-if="existingImageUrl && removeExistingImage && !imagePreviewUrl" class="image-removal-panel mb-3">
        <span>Current image will be removed.</span>
        <button type="button" class="btn-restore-image" @click="restoreExistingImage">Undo</button>
      </div>

      <input
        ref="fileInput"
        class="form-control"
        type="file"
        accept="image/*"
        @change="handleFileChange"
      />
      <div v-if="errors.imageFile" class="text-danger small mt-1">{{ errors.imageFile }}</div>
      <div v-if="imagePreviewUrl" class="mt-2">
        <div class="image-preview-frame">
          <img :src="imagePreviewUrl" alt="Preview" class="image-preview" />
          <button type="button" class="btn-remove-image" @click="removeImage">✕</button>
        </div>
        <div class="text-muted small mt-1">
          {{ imageFile?.name }} · {{ (imageFile?.size / 1024).toFixed(1) }} KB
        </div>
      </div>
    </section>

    <!-- Description -->
    <section class="form-section">
      <label class="form-section-label">
        <font-awesome-icon :icon="['fas', 'message']" class="modal-icon" />
        Description
      </label>
      <textarea
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
    </section>
  </form>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue';
import apiClient from '@/api/apiClient';

const props = defineProps({
  initialData: { type: Object, default: null },
});

const scamActions = [
  { id: 1, name: 'Pickpocketing' }, { id: 2, name: 'Theft' },
  { id: 3, name: 'Overcharging' }, { id: 4, name: 'Aggressive Solicitation' },
  { id: 5, name: 'Fraud' }, { id: 6, name: 'System Tampering' },
  { id: 7, name: 'Other Action' },
];
const scamContexts = [
  { id: 1, name: 'Street / Public Area' }, { id: 2, name: 'Restaurant /Bar / Cafe' },
  { id: 3, name: 'Transportation' }, { id: 4, name: 'Lodging' },
  { id: 5, name: 'Tourist Attraction / Ticketed Venue' },
  { id: 6, name: 'Financial Service' }, { id: 7, name: 'Other Context' },
];

const countries = ref([]);
const states = ref([]);
const cities = ref([]);
const countrySearch = ref('');
const stateSearch = ref('');
const citySearch = ref('');
const showCountryDropdown = ref(false);
const showStateDropdown = ref(false);
const showCityDropdown = ref(false);

const fileInput = ref(null);
const imageFile = ref(null);
const imagePreviewUrl = ref(null);
const existingImageUrl = ref(null);
const removeExistingImage = ref(false);

const form = reactive({
  title: '',
  scamActionId: '',
  scamContextId: '',
  countryId: '',
  stateId: '',
  cityId: '',
  description: '',
});

const errors = reactive({
  title: '',
  scamActionId: '',
  scamContextId: '',
  countryId: '',
  description: '',
  imageFile: '',
});

const filteredCountries = computed(() =>
  countrySearch.value
    ? countries.value.filter(c => c.name.toLowerCase().includes(countrySearch.value.toLowerCase()))
    : countries.value
);
const filteredStates = computed(() =>
  stateSearch.value
    ? states.value.filter(s => s.name.toLowerCase().includes(stateSearch.value.toLowerCase()))
    : states.value
);
const filteredCities = computed(() =>
  citySearch.value
    ? cities.value.filter(c => c.name.toLowerCase().includes(citySearch.value.toLowerCase()))
    : cities.value
);

const loadCountries = async () => {
  if (countries.value.length > 0) return;
  const res = await apiClient.get('/countries');
  countries.value = res.data.result.countries;
};

const loadStates = async () => {
  if (!form.countryId) return;
  const res = await apiClient.get(`/countries/${form.countryId}/states`);
  states.value = res.data.result.states;
};

const loadCities = async () => {
  if (!form.stateId) return;
  const res = await apiClient.get(`/states/${form.stateId}/cities`);
  cities.value = res.data.result.cities;
};

const selectCountry = (c) => {
  form.countryId = c.id; countrySearch.value = c.name;
  form.stateId = ''; stateSearch.value = '';
  form.cityId = ''; citySearch.value = '';
  states.value = []; cities.value = [];
  showCountryDropdown.value = false;
  loadStates();
};
const selectState = (s) => {
  form.stateId = s.id; stateSearch.value = s.name;
  form.cityId = ''; citySearch.value = '';
  cities.value = [];
  showStateDropdown.value = false;
  loadCities();
};
const selectCity = (c) => {
  form.cityId = c.id; citySearch.value = c.name;
  showCityDropdown.value = false;
};

const handleCountryBlur = () => {
  showCountryDropdown.value = false;
  if (!countries.value.find(c => c.name === countrySearch.value)) {
    countrySearch.value = ''; form.countryId = '';
    form.stateId = ''; stateSearch.value = '';
    form.cityId = ''; citySearch.value = '';
    states.value = []; cities.value = [];
  }
};
const handleStateBlur = () => {
  showStateDropdown.value = false;
  if (!states.value.find(s => s.name === stateSearch.value)) {
    stateSearch.value = ''; form.stateId = '';
    form.cityId = ''; citySearch.value = '';
    cities.value = [];
  }
};
const handleCityBlur = () => {
  showCityDropdown.value = false;
  if (!cities.value.find(c => c.name === citySearch.value)) {
    citySearch.value = ''; form.cityId = '';
  }
};

const handleFileChange = (event) => {
  const file = event.target.files[0];
  if (!file) return;
  if (file.size > 5 * 1024 * 1024) {
    errors.imageFile = 'Only images up to 5MB can be uploaded.';
    imageFile.value = null;
    event.target.value = '';
    return;
  }
  imageFile.value = file;
  removeExistingImage.value = false;
  errors.imageFile = '';
  if (imagePreviewUrl.value) URL.revokeObjectURL(imagePreviewUrl.value);
  imagePreviewUrl.value = URL.createObjectURL(file);
};

const removeImage = () => {
  imageFile.value = null;
  if (imagePreviewUrl.value) URL.revokeObjectURL(imagePreviewUrl.value);
  imagePreviewUrl.value = null;
  if (fileInput.value) fileInput.value.value = '';
};

const markExistingImageForRemoval = () => {
  removeExistingImage.value = true;
  removeImage();
};

const restoreExistingImage = () => {
  removeExistingImage.value = false;
};

const validate = () => {
  let valid = true;
  errors.title = form.title.trim() ? '' : 'Please enter a title.';
  if (errors.title) valid = false;
  errors.scamActionId = form.scamActionId ? '' : 'Please select a scam action.';
  if (errors.scamActionId) valid = false;
  errors.scamContextId = form.scamContextId ? '' : 'Please select a scam context.';
  if (errors.scamContextId) valid = false;
  errors.countryId = form.countryId ? '' : 'Please select a country.';
  if (errors.countryId) valid = false;
  errors.description = form.description.trim() ? '' : 'Please provide a description.';
  if (errors.description) valid = false;
  return valid;
};

const getFormData = () => ({
  title: form.title,
  scamActionId: form.scamActionId,
  scamContextId: form.scamContextId,
  countryId: form.countryId,
  stateId: form.stateId || null,
  cityId: form.cityId || null,
  description: form.description,
});

const getImageFile = () => imageFile.value;
const getRemoveImage = () => removeExistingImage.value;

const reset = () => {
  form.title = '';
  form.scamActionId = '';
  form.scamContextId = '';
  form.countryId = '';
  form.stateId = '';
  form.cityId = '';
  form.description = '';
  countrySearch.value = '';
  stateSearch.value = '';
  citySearch.value = '';
  states.value = [];
  cities.value = [];
  Object.keys(errors).forEach(k => (errors[k] = ''));
  existingImageUrl.value = null;
  removeExistingImage.value = false;
  removeImage();
};

watch(() => props.initialData, async (data) => {
  if (!data) return;
  await loadCountries();

  form.title = data.title;
  form.scamActionId = data.scamActionId;
  form.scamContextId = data.scamContextId;
  form.countryId = data.countryId;
  countrySearch.value = data.countryName;
  form.description = data.description ?? '';

  states.value = [];
  cities.value = [];
  stateSearch.value = '';
  citySearch.value = '';
  form.stateId = '';
  form.cityId = '';

  if (data.stateId) {
    await loadStates();
    form.stateId = data.stateId;
    stateSearch.value = data.stateName ?? '';
  } else {
    await loadStates();
  }

  if (data.cityId) {
    await loadCities();
    form.cityId = data.cityId;
    citySearch.value = data.cityName ?? '';
  }

  existingImageUrl.value = data.imageUrl ?? null;
  removeExistingImage.value = false;
  removeImage();
});

onMounted(loadCountries);

defineExpose({ validate, getFormData, getImageFile, getRemoveImage, reset });
</script>

<style scoped lang="scss">
.report-form-fields {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.form-section {
  padding: 18px 20px;
  border: 1px solid var(--safetrip-border);
  border-radius: 12px;
  background: var(--safetrip-surface);
  box-shadow: 0 8px 24px rgba(36, 49, 58, 0.05);
}

.form-section-label {
  display: flex;
  align-items: center;
  gap: 7px;
  margin-bottom: 12px;
  color: var(--safetrip-muted);
  font-size: 0.78rem;
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.field-label {
  margin-bottom: 6px;
  color: var(--safetrip-text);
  font-size: 0.9rem;
  font-weight: 700;
}

.modal-icon {
  font-size: 95%;
  color: var(--safetrip-primary);
}

.form-control,
.form-select {
  border-color: var(--safetrip-border);
  background-color: #fffdf8;
  color: var(--safetrip-text);

  &:focus {
    border-color: var(--safetrip-primary);
    box-shadow: 0 0 0 3px rgba(42, 157, 143, 0.12);
  }
}

.form-control::placeholder {
  color: #a8a29e;
}

.text-danger {
  color: #b6523b !important;
}

.image-preview {
  display: block;
  max-width: 100%;
  max-height: 260px;
  object-fit: contain;
  border-radius: 9px;
}

.current-image-panel {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 8px;
  margin-bottom: 14px;
}

.image-preview-frame {
  position: relative;
  display: inline-flex;
  max-width: 100%;
  padding: 8px;
  border: 1px solid var(--safetrip-border);
  border-radius: 12px;
  background: #fffdf8;
}

.image-help-text {
  margin-top: 6px;
  color: var(--safetrip-muted);
  font-size: 0.84rem;
  font-weight: 650;
}

.image-removal-panel {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  max-width: 420px;
  padding: 10px 12px;
  border: 1px solid #e8c7b7;
  border-radius: 10px;
  background: #fff5ee;
  color: #9b5139;
  font-size: 0.9rem;
  font-weight: 750;
}

.btn-restore-image {
  border: 0;
  background: transparent;
  color: var(--safetrip-primary);
  font-weight: 850;
  white-space: nowrap;
}

.btn-remove-image {
  position: absolute;
  top: -8px;
  right: -8px;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: #b6523b;
  color: white;
  border: none;
  font-size: 11px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;

  &:hover {
    background: #944330;
  }
}

.location-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  z-index: 1060;
  max-height: 200px;
  overflow-y: auto;
  background: #fffdf8;
  border: 1px solid var(--safetrip-border);
  border-top: none;
  border-radius: 0 0 4px 4px;
  list-style: none;
  margin: 0;
  padding: 0;

  li {
    padding: 8px 12px;
    cursor: pointer;

    &:hover {
      background-color: var(--safetrip-primary-soft);
    }
  }
}
</style>
