<template>
  <div
    class="modal fade"
    ref="modalRef"
    id="reportEditModal"
    data-bs-backdrop="static"
    data-bs-keyboard="false"
    tabindex="-1"
    aria-hidden="true"
  >
    <div class="modal-dialog modal-dialog-centered modal-dialog-scrollable modal-lg">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">Edit Report</h5>
          <button type="button" class="btn-close" @click="hide" aria-label="Close"></button>
        </div>
        <div class="modal-body">
          <form @submit.prevent>
            <!-- Title -->
            <div class="mb-3">
              <label class="col-form-label fw-bold">Title</label>
              <input
                type="text"
                :class="['form-control', { 'is-invalid': errors.title }]"
                v-model="form.title"
                maxlength="100"
              />
              <div v-if="errors.title" class="text-danger small mt-1">{{ errors.title }}</div>
            </div>

            <!-- Scam Action / Context -->
            <div class="row mb-3">
              <div class="col-md-6">
                <label class="col-form-label fw-bold">Scam Action</label>
                <select v-model="form.scamActionId" :class="['form-select', { 'is-invalid': errors.scamActionId }]">
                  <option disabled value="">Select an Action</option>
                  <option v-for="a in scamActions" :key="a.id" :value="a.id">{{ a.name }}</option>
                </select>
                <div v-if="errors.scamActionId" class="text-danger small mt-1">{{ errors.scamActionId }}</div>
              </div>
              <div class="col-md-6">
                <label class="col-form-label fw-bold">Scam Context</label>
                <select v-model="form.scamContextId" :class="['form-select', { 'is-invalid': errors.scamContextId }]">
                  <option disabled value="">Select a context</option>
                  <option v-for="c in scamContexts" :key="c.id" :value="c.id">{{ c.name }}</option>
                </select>
                <div v-if="errors.scamContextId" class="text-danger small mt-1">{{ errors.scamContextId }}</div>
              </div>
            </div>

            <!-- Location -->
            <div class="mb-3">
              <label class="col-form-label fw-bold">Location</label>

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
              <div v-if="form.stateId" class="position-relative">
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

            <!-- Description -->
            <div class="mb-3">
              <label class="col-form-label fw-bold">Description</label>
              <textarea
                v-model="form.description"
                :class="['form-control', { 'is-invalid': errors.description }]"
                maxlength="500"
                rows="4"
              ></textarea>
              <div class="d-flex justify-content-between mt-1">
                <div v-if="errors.description" class="text-danger small">{{ errors.description }}</div>
                <div v-else></div>
                <small class="text-muted">{{ form.description.length }} / 500</small>
              </div>
            </div>

            <div class="modal-footer px-0">
              <div v-if="submitMessage" class="me-3 fw-bold small" :class="submitStatus === 'success' ? 'text-success' : 'text-danger'">
                {{ submitMessage }}
              </div>
              <button type="button" class="btn btn-secondary" @click="hide">Cancel</button>
              <button type="button" class="btn btn-primary" :disabled="isSubmitting" @click="submitForm">Save</button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue';
import { useBootstrapModal } from '@/composables/useBootstrapModal';
import apiClient from '@/api/apiClient';

const props = defineProps({
  report: { type: Object, default: null },
});
const emit = defineEmits(['updated']);

const modalRef = ref(null);
const { hide } = useBootstrapModal(modalRef);

const scamActions = [
  { id: 1, name: "Pickpocketing" }, { id: 2, name: "Theft" },
  { id: 3, name: "Overcharging" }, { id: 4, name: "Aggressive Solicitation" },
  { id: 5, name: "Fraud" }, { id: 6, name: "System Tampering" },
  { id: 7, name: "Other Action" },
];
const scamContexts = [
  { id: 1, name: "Street / Public Area" }, { id: 2, name: "Restaurant /Bar / Cafe" },
  { id: 3, name: "Transportation" }, { id: 4, name: "Lodging" },
  { id: 5, name: "Tourist Attraction / Ticketed Venue" },
  { id: 6, name: "Financial Service" }, { id: 7, name: "Other Context" },
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

const form = reactive({
  title: '', scamActionId: '', scamContextId: '',
  countryId: '', stateId: '', cityId: '', description: '',
});
const errors = reactive({
  title: '', scamActionId: '', scamContextId: '', countryId: '', description: '',
});
const submitMessage = ref('');
const submitStatus = ref('');
const isSubmitting = ref(false);

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

watch(() => props.report, async (r) => {
  if (!r) return;
  await loadCountries();

  form.title = r.title;
  form.scamActionId = r.scamActionId;
  form.scamContextId = r.scamContextId;
  form.countryId = r.countryId;
  countrySearch.value = r.countryName;
  form.description = r.description;
  submitMessage.value = '';

  states.value = [];
  cities.value = [];

  if (r.stateId) {
    await loadStates();
    form.stateId = r.stateId;
    stateSearch.value = r.stateName || '';
  } else {
    form.stateId = '';
    stateSearch.value = '';
    await loadStates();
  }

  if (r.cityId) {
    await loadCities();
    form.cityId = r.cityId;
    citySearch.value = r.cityName || '';
  } else {
    form.cityId = '';
    citySearch.value = '';
  }
});

const checkForm = () => {
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

const submitForm = async () => {
  if (isSubmitting.value || !props.report) return;
  if (!checkForm()) return;

  isSubmitting.value = true;
  try {
    await apiClient.put(`/user-reports/${props.report.id}`, {
      title: form.title,
      scamActionId: form.scamActionId,
      scamContextId: form.scamContextId,
      countryId: form.countryId,
      stateId: form.stateId || null,
      cityId: form.cityId || null,
      description: form.description,
    });
    submitMessage.value = 'Report updated successfully.';
    submitStatus.value = 'success';
    emit('updated');
    setTimeout(hide, 1000);
  } catch (e) {
    submitMessage.value = 'Failed to update. Please try again.';
    submitStatus.value = 'error';
  } finally {
    isSubmitting.value = false;
  }
};
</script>

<style scoped lang="scss">
.location-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  z-index: 1060;
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
