<template>
  <div class="modal fade" id="reportInaccuracyModal" data-bs-backdrop="static" data-bs-keyboard="false" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title d-flex align-items-center gap-2">
            <font-awesome-icon :icon="['fas', 'triangle-exclamation']" />
            Suggest an Edit
          </h5>
          <button type="button" class="btn-close" @click="closeModal" aria-label="Close"></button>
        </div>
        <div class="modal-body">
          <p class="text-muted small mb-3">Let us know if an AI-assisted note looks off.</p>

          <div class="mb-3">
            <label class="form-label fw-bold">Reason <span class="text-danger">*</span></label>
            <select v-model="reason" class="form-select">
              <option value="" disabled>Select a reason</option>
              <option value="WRONG_LOCATION">Wrong Location</option>
              <option value="WRONG_SCAM_TYPE">Wrong Scam Type</option>
              <option value="NOT_A_SCAM">Not a Scam</option>
              <option value="INACCURATE_CONTENT">Inaccurate Content</option>
              <option value="OTHER">Other</option>
            </select>
          </div>

          <div class="mb-3">
            <label class="form-label fw-bold">A few details <span class="text-muted">(optional)</span></label>
            <textarea
              v-model="description"
              class="form-control"
              rows="3"
              placeholder="Tell us what should be corrected..."
              maxlength="500"
            ></textarea>
            <div class="text-end text-muted small mt-1">{{ description.length }}/500</div>
          </div>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" @click="closeModal">Cancel</button>
          <button type="button" class="btn btn-warning" :disabled="!reason || isSubmitting" @click="submit">
            <span v-if="isSubmitting" class="spinner-border spinner-border-sm me-1"></span>
            Send
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useBootstrapModal } from '@/composables/useBootstrapModal';
import apiClient from '@/api/apiClient';

const { hide } = useBootstrapModal('#reportInaccuracyModal');

const reportId = ref(null);
const reason = ref('');
const description = ref('');
const isSubmitting = ref(false);

const emit = defineEmits(['submitted']);

const open = (id) => {
  reportId.value = id;
  reason.value = '';
  description.value = '';
  const modal = window.bootstrap?.Modal.getOrCreateInstance(document.getElementById('reportInaccuracyModal'));
  modal?.show();
};

const closeModal = () => {
  hide();
};

const submit = async () => {
  if (!reason.value) return;
  isSubmitting.value = true;
  try {
    await apiClient.post(`/external-reports/${reportId.value}/inaccuracies`, {
      reason: reason.value,
      description: description.value || null,
    });
    hide();
    emit('submitted');
  } catch (e) {
    console.error('Failed to submit inaccuracy report', e);
  } finally {
    isSubmitting.value = false;
  }
};

defineExpose({ open });
</script>
