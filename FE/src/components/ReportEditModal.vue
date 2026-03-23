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
          <ReportFormFields ref="formRef" :initialData="report" />

          <div class="modal-footer px-0">
            <div v-if="submitMessage" class="me-3 fw-bold small text-danger">
              {{ submitMessage }}
            </div>
            <button type="button" class="btn btn-secondary" @click="hide">Cancel</button>
            <button type="button" class="btn btn-primary" :disabled="isSubmitting" @click="submitForm">
              Save
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
import { useToast } from '@/composables/useToast';
import ReportFormFields from '@/components/ReportFormFields.vue';
import apiClient from '@/api/apiClient';

const props = defineProps({
  report: { type: Object, default: null },
});
const emit = defineEmits(['updated']);

const modalRef = ref(null);
const { hide } = useBootstrapModal(modalRef);
const formRef = ref(null);
const toast = useToast();

const submitMessage = ref('');
const isSubmitting = ref(false);

const submitForm = async () => {
  if (isSubmitting.value || !props.report) return;
  if (!formRef.value.validate()) return;

  isSubmitting.value = true;
  try {
    const data = formRef.value.getFormData();
    const imageFile = formRef.value.getImageFile();

    const formData = new FormData();
    formData.append('request', new Blob([JSON.stringify(data)], { type: 'application/json' }));
    if (imageFile) formData.append('images', imageFile);

    await apiClient.put(`/user-reports/${props.report.id}`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });

    emit('updated');
    hide();
    toast.show('Report updated successfully.');
  } catch (e) {
    submitMessage.value = 'Failed to update. Please try again.';
    submitStatus.value = 'error';
  } finally {
    isSubmitting.value = false;
  }
};
</script>
