<template>
  <div class="modal fade" id="adminModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered modal-dialog-scrollable modal-xl">
      <div class="modal-content admin-modal">
        <div class="modal-header admin-header">
          <div>
            <div class="admin-kicker">Admin review</div>
            <h5 class="modal-title">Report Feedback</h5>
          </div>
          <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
        </div>
        <div class="modal-body admin-body">
          <div v-if="isLoading" class="admin-empty-state">
            <div class="spinner-border admin-spinner" role="status"></div>
          </div>
          <div v-else-if="reports.length === 0" class="admin-empty-state">
            No feedback to review.
          </div>
          <div v-else class="admin-table-wrap">
            <table class="admin-table">
              <thead>
                <tr>
                  <th>Report</th>
                  <th>Reason</th>
                  <th>Description</th>
                  <th>Status</th>
                  <th>Reporter</th>
                  <th>Submitted</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="item in reports"
                  :key="item.id"
                  class="clickable-row"
                  @click="openDetail(item)"
                >
                  <td>
                    <div class="report-title-cell">{{ item.externalReportTitle }}</div>
                    <div class="report-id-cell">Feedback #{{ item.id }}</div>
                  </td>
                  <td>
                    <span class="reason-chip">{{ formatReason(item.reason) }}</span>
                  </td>
                  <td class="description-cell">{{ truncate(item.description) }}</td>
                  <td>
                    <span :class="statusClass(item.status)">
                      {{ item.status === 'RESOLVED' ? 'Resolved' : 'Under review' }}
                    </span>
                  </td>
                  <td class="muted-cell">{{ item.reporterNickname }}</td>
                  <td class="muted-cell">{{ formatDate(item.createdAt) }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- 상세 모달 -->
  <div class="modal fade" id="adminDetailModal" tabindex="-1" aria-hidden="true" data-bs-backdrop="static">
    <div class="modal-dialog modal-dialog-centered modal-dialog-scrollable">
      <div class="modal-content admin-modal" v-if="selectedItem">
        <div class="modal-header admin-header">
          <div>
            <div class="admin-kicker">Feedback review</div>
            <h6 class="modal-title">Feedback Detail</h6>
          </div>
          <button type="button" class="btn-close" @click="closeDetail" aria-label="Close"></button>
        </div>
        <div class="modal-body admin-body">
          <!-- Report Info -->
          <div class="detail-section">
            <div class="detail-section-title">Report Info</div>
            <div class="mb-2">
              <div class="detail-label">Report</div>
              <div class="detail-copy strong">{{ selectedItem.externalReportTitle }}</div>
              <div class="detail-meta-line">External report #{{ selectedItem.externalReportId }}</div>
            </div>
            <div class="mb-2">
              <div class="detail-label">Reason</div>
              <span class="reason-chip">{{ formatReason(selectedItem.reason) }}</span>
            </div>
            <div>
              <div class="detail-label">Description</div>
              <div class="detail-copy">
                {{ selectedItem.description || '—' }}
              </div>
            </div>
          </div>

          <!-- Submission Info -->
          <div class="detail-section">
            <div class="detail-section-title">Submission Info</div>
            <div class="mb-2">
              <div class="detail-label">Reporter</div>
              <div class="detail-copy strong">{{ selectedItem.reporterNickname }}</div>
            </div>
            <div>
              <div class="detail-label">Submitted At</div>
              <div class="detail-copy">{{ formatDate(selectedItem.createdAt) }}</div>
            </div>
          </div>

          <!-- Status -->
          <div :class="selectedItem.status === 'RESOLVED' ? 'detail-section status-resolved' : 'detail-section status-pending'">
            <div class="detail-section-title">Status</div>
            <div class="d-flex align-items-center gap-2">
              <span :class="statusClass(selectedItem.status)">
                {{ selectedItem.status === 'RESOLVED' ? 'Resolved' : 'Under review' }}
              </span>
              <span v-if="selectedItem.resolvedAt" class="detail-copy">
                {{ formatDate(selectedItem.resolvedAt) }}
              </span>
            </div>
          </div>
        </div>
        <div class="modal-footer admin-footer">
          <button
            type="button"
            class="btn admin-secondary-btn btn-sm"
            @click="openExternalReport"
          >
            Open report
          </button>
          <button
            v-if="selectedItem.status === 'PENDING'"
            class="btn admin-primary-btn btn-sm"
            :disabled="isResolving"
            @click="resolveItem(selectedItem)"
          >
            Mark resolved
          </button>
          <button type="button" class="btn admin-secondary-btn btn-sm" @click="closeDetail">Close</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue';
import { Modal } from 'bootstrap';
import apiClient from '@/api/apiClient';
import { useMapStore } from '@/stores/map';

const reports = ref([]);
const isLoading = ref(false);
const selectedItem = ref(null);
const isResolving = ref(false);
const mapStore = useMapStore();

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

const openExternalReport = () => {
  if (!selectedItem.value?.externalReportId) return;
  Modal.getOrCreateInstance(document.getElementById('adminDetailModal')).hide();
  mapStore.requestOpenExternalReportFromAdmin(selectedItem.value.externalReportId);
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

const statusClass = (status) => [
  'status-chip',
  status === 'RESOLVED' ? 'resolved' : 'pending',
];

const formatDate = (dt) => {
  if (!dt) return '—';
  return new Date(dt).toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' });
};

defineExpose({ load });
</script>

<style scoped lang="scss">
.admin-modal {
  overflow: hidden;
  border: 1px solid var(--safetrip-border);
  border-radius: 14px;
  background: var(--safetrip-page);
}

.admin-header {
  background: #fffdf8;
  border-bottom: 1px solid var(--safetrip-border);
  padding: 20px 24px;

  .modal-title {
    color: var(--safetrip-text);
    font-weight: 800;
  }
}

.admin-kicker {
  margin-bottom: 2px;
  color: var(--safetrip-primary);
  font-size: 0.74rem;
  font-weight: 850;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.admin-body {
  padding: 22px 24px 24px;
}

.admin-empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 180px;
  border: 1px solid var(--safetrip-border);
  border-radius: 12px;
  background: var(--safetrip-surface);
  color: var(--safetrip-muted);
  font-weight: 750;
}

.admin-spinner {
  color: var(--safetrip-primary);
}

.admin-table-wrap {
  overflow: auto;
  border: 1px solid var(--safetrip-border);
  border-radius: 12px;
  background: var(--safetrip-surface);
  box-shadow: 0 8px 24px rgba(36, 49, 58, 0.05);
}

.admin-table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
  min-width: 860px;

  th,
  td {
    padding: 14px 16px;
    border-bottom: 1px solid var(--safetrip-border);
    vertical-align: middle;
  }

  th {
    color: var(--safetrip-muted);
    background: #fffdf8;
    font-size: 0.72rem;
    font-weight: 850;
    letter-spacing: 0.07em;
    text-transform: uppercase;
    white-space: nowrap;
  }

  tbody tr:last-child td {
    border-bottom: 0;
  }
}

.clickable-row {
  cursor: pointer;

  &:hover {
    background: var(--safetrip-primary-soft);
  }
}

.report-title-cell {
  max-width: 300px;
  color: var(--safetrip-text);
  font-size: 0.93rem;
  font-weight: 800;
  line-height: 1.35;
}

.report-id-cell {
  margin-top: 3px;
  color: var(--safetrip-muted);
  font-size: 0.78rem;
  font-weight: 650;
}

.description-cell {
  max-width: 200px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  color: var(--safetrip-muted);
  font-size: 0.88rem;
  font-weight: 650;
}

.muted-cell {
  color: var(--safetrip-muted);
  font-size: 0.86rem;
  font-weight: 650;
  white-space: nowrap;
}

.reason-chip,
.status-chip {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 28px;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 0.78rem;
  font-weight: 800;
  white-space: nowrap;
}

.reason-chip {
  color: #73511f;
  background: #fff7df;
  border: 1px solid #ecd39a;
}

.status-chip {
  &.pending {
    color: #4b5d62;
    background: #f6f3ed;
    border: 1px solid #ded6ca;
  }

  &.resolved {
    color: #1f7f74;
    background: var(--safetrip-primary-soft);
    border: 1px solid #b9e4dc;
  }
}

.detail-section {
  padding: 16px 18px;
  border: 1px solid var(--safetrip-border);
  border-radius: 12px;
  background: var(--safetrip-surface);
  margin-bottom: 12px;
  box-shadow: 0 8px 24px rgba(36, 49, 58, 0.05);

  &.status-resolved {
    background: var(--safetrip-primary-soft);
    border-color: #b9e4dc;
  }

  &.status-pending  {
    background: #fffdf8;
  }
}

.detail-section-title {
  margin-bottom: 10px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--safetrip-border);
  color: var(--safetrip-muted);
  font-size: 0.72rem;
  font-weight: 850;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.detail-label {
  margin-bottom: 3px;
  color: var(--safetrip-muted);
  font-size: 0.78rem;
  font-weight: 800;
}

.detail-copy {
  color: var(--safetrip-muted);
  font-size: 0.9rem;
  font-weight: 650;
  overflow-wrap: break-word;
  white-space: pre-wrap;

  &.strong {
    color: var(--safetrip-text);
    font-weight: 750;
  }
}

.detail-meta-line {
  margin-top: 5px;
  color: var(--safetrip-muted);
  font-size: 0.8rem;
  font-weight: 700;
}

.admin-footer {
  border-top: 1px solid var(--safetrip-border);
  background: #fffdf8;
}

.admin-primary-btn {
  color: #fff;
  background: var(--safetrip-primary);
  border-color: var(--safetrip-primary);

  &:hover,
  &:focus {
    color: #fff;
    background: var(--safetrip-primary-hover);
    border-color: var(--safetrip-primary-hover);
  }
}

.admin-secondary-btn {
  color: var(--safetrip-muted);
  background: #fffdf8;
  border-color: var(--safetrip-border);

  &:hover,
  &:focus {
    color: var(--safetrip-text);
    background: #f7f1e8;
    border-color: #d8cec2;
  }
}

@media (max-width: 768px) {
  .admin-header,
  .admin-body {
    padding-left: 18px;
    padding-right: 18px;
  }
}
</style>
