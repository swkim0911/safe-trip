<template>
  <div class="modal fade" id="adminModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered modal-dialog-scrollable modal-xl">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">🛡️ Admin — Report Inaccuracies</h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
        </div>
        <div class="modal-body">
          <div v-if="isLoading" class="text-center py-4">
            <div class="spinner-border text-primary" role="status"></div>
          </div>
          <div v-else-if="reports.length === 0" class="text-center text-muted py-4">
            No inaccuracy reports yet.
          </div>
          <table v-else class="table table-sm table-hover align-middle">
            <thead class="table-light">
              <tr>
                <th>#</th>
                <th>Report Title</th>
                <th>Reason</th>
                <th>Description</th>
                <th>Reporter</th>
                <th>Submitted At</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in reports" :key="item.id">
                <td class="text-muted small">{{ item.id }}</td>
                <td class="small">{{ item.externalReportTitle }}</td>
                <td>
                  <span class="badge bg-warning text-dark">{{ formatReason(item.reason) }}</span>
                </td>
                <td class="small text-muted">{{ item.description || '—' }}</td>
                <td class="small">{{ item.reporterNickname }}</td>
                <td class="small text-muted">{{ formatDate(item.createdAt) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import apiClient from '@/api/apiClient';

const reports = ref([]);
const isLoading = ref(false);

const load = async () => {
  isLoading.value = true;
  try {
    const response = await apiClient.get('/admin/report-inaccuracies');
    reports.value = response.data.result;
  } catch (e) {
    console.error('Failed to load inaccuracy reports', e);
  } finally {
    isLoading.value = false;
  }
};

const reasonLabels = {
  WRONG_LOCATION: 'Wrong Location',
  WRONG_SCAM_TYPE: 'Wrong Scam Type',
  NOT_A_SCAM: 'Not a Scam',
  INACCURATE_CONTENT: 'Inaccurate Content',
  OTHER: 'Other',
};

const formatReason = (reason) => reasonLabels[reason] ?? reason;

const formatDate = (dt) => {
  if (!dt) return '—';
  return new Date(dt).toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' });
};

defineExpose({ load });
</script>
