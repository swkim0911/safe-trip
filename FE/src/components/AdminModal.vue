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
                <th>Status</th>
                <th>Reporter</th>
                <th>Submitted At</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="item in reports"
                :key="item.id"
                class="clickable-row"
                @click="openDetail(item)"
              >
                <td class="text-muted small">{{ item.id }}</td>
                <td class="small">{{ item.externalReportTitle }}</td>
                <td>
                  <span class="badge bg-warning text-dark">{{ formatReason(item.reason) }}</span>
                </td>
                <td class="small text-muted description-cell">{{ truncate(item.description) }}</td>
                <td>
                  <span :class="item.status === 'RESOLVED' ? 'badge bg-success' : 'badge bg-secondary'">
                    {{ item.status === 'RESOLVED' ? 'Resolved' : 'Under Review' }}
                  </span>
                </td>
                <td class="small">{{ item.reporterNickname }}</td>
                <td class="small text-muted">{{ formatDate(item.createdAt) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>

  <!-- 상세 모달 -->
  <div class="modal fade" id="adminDetailModal" tabindex="-1" aria-hidden="true" data-bs-backdrop="static">
    <div class="modal-dialog modal-dialog-centered modal-dialog-scrollable">
      <div class="modal-content" v-if="selectedItem">
        <div class="modal-header">
          <h6 class="modal-title fw-bold">Feedback Detail</h6>
          <button type="button" class="btn-close" @click="closeDetail" aria-label="Close"></button>
        </div>
        <div class="modal-body">
          <!-- Report Info -->
          <div class="detail-section">
            <div class="detail-section-title">Report Info</div>
            <div class="mb-2">
              <div class="detail-label">Report</div>
              <div class="small">{{ selectedItem.externalReportTitle }}</div>
            </div>
            <div class="mb-2">
              <div class="detail-label">Reason</div>
              <span class="badge bg-warning text-dark">{{ formatReason(selectedItem.reason) }}</span>
            </div>
            <div>
              <div class="detail-label">Description</div>
              <div class="small text-muted" style="white-space: pre-wrap; overflow-wrap: break-word;">
                {{ selectedItem.description || '—' }}
              </div>
            </div>
          </div>

          <!-- Submission Info -->
          <div class="detail-section">
            <div class="detail-section-title">Submission Info</div>
            <div class="mb-2">
              <div class="detail-label">Reporter</div>
              <div class="small">{{ selectedItem.reporterNickname }}</div>
            </div>
            <div>
              <div class="detail-label">Submitted At</div>
              <div class="small text-muted">{{ formatDate(selectedItem.createdAt) }}</div>
            </div>
          </div>

          <!-- Status -->
          <div :class="selectedItem.status === 'RESOLVED' ? 'detail-section status-resolved' : 'detail-section status-pending'">
            <div class="detail-section-title">Status</div>
            <div class="d-flex align-items-center gap-2">
              <span :class="selectedItem.status === 'RESOLVED' ? 'badge bg-success' : 'badge bg-secondary'">
                {{ selectedItem.status === 'RESOLVED' ? 'Resolved' : 'Under Review' }}
              </span>
              <span v-if="selectedItem.resolvedAt" class="text-muted small">
                {{ formatDate(selectedItem.resolvedAt) }}
              </span>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button
            v-if="selectedItem.status === 'PENDING'"
            class="btn btn-success btn-sm"
            :disabled="isResolving"
            @click="resolveItem(selectedItem)"
          >
            Mark as Resolved
          </button>
          <button type="button" class="btn btn-secondary btn-sm" @click="closeDetail">Close</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue';
import { Modal } from 'bootstrap';
import apiClient from '@/api/apiClient';

const reports = ref([]);
const isLoading = ref(false);
const selectedItem = ref(null);
const isResolving = ref(false);

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

const openDetail = async (item) => {
  selectedItem.value = item;
  await nextTick();
  Modal.getOrCreateInstance(document.getElementById('adminModal')).hide();
  Modal.getOrCreateInstance(document.getElementById('adminDetailModal')).show();
};

const closeDetail = () => {
  Modal.getOrCreateInstance(document.getElementById('adminDetailModal')).hide();
  Modal.getOrCreateInstance(document.getElementById('adminModal')).show();
};

const resolveItem = async (item) => {
  isResolving.value = true;
  try {
    await apiClient.patch(`/admin/report-inaccuracies/${item.id}/resolve`);
    item.status = 'RESOLVED';
    item.resolvedAt = new Date().toISOString();
    const target = reports.value.find(r => r.id === item.id);
    if (target) {
      target.status = 'RESOLVED';
      target.resolvedAt = item.resolvedAt;
    }
  } catch (e) {
    console.error('Failed to resolve inaccuracy report', e);
  } finally {
    isResolving.value = false;
  }
};

const truncate = (text, max = 60) => {
  if (!text) return '—';
  return text.length > max ? text.slice(0, max) + '...' : text;
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

<style scoped>
.clickable-row {
  cursor: pointer;
}

.description-cell {
  max-width: 200px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.detail-section {
  padding: 14px 16px;
  border-radius: 8px;
  background: #f8f9fa;
  margin-bottom: 10px;

  &.status-resolved { background: #f0faf4; }
  &.status-pending  { background: #f5f5f5; }
}

.detail-section-title {
  font-weight: 700;
  font-size: 0.7rem;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: #adb5bd;
  margin-bottom: 10px;
  padding-bottom: 8px;
  border-bottom: 1px solid #e9ecef;
}

.detail-label {
  font-weight: 600;
  font-size: 0.75rem;
  color: #6c757d;
  margin-bottom: 3px;
}
</style>
