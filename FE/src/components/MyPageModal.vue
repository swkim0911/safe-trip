<template>
  <div
    class="modal fade"
    ref="modalRef"
    id="myPageModal"
    data-bs-backdrop="static"
    data-bs-keyboard="false"
    tabindex="-1"
    aria-hidden="true"
  >
    <div class="modal-dialog modal-dialog-centered modal-dialog-scrollable modal-lg">
      <div class="modal-content mypage-modal">
        <div class="modal-header mypage-header">
          <h5 class="modal-title">My Page</h5>
          <button type="button" class="btn-close" @click="hide" aria-label="Close"></button>
        </div>

        <!-- 탭 -->
        <div class="tab-nav">
          <button
            class="tab-btn"
            :class="{ active: tab === 'profile' }"
            @click="tab = 'profile'"
          >Profile</button>
          <button
            class="tab-btn"
            :class="{ active: tab === 'reports' }"
            @click="switchToReports"
          >My Reports</button>
          <button
            class="tab-btn"
            :class="{ active: tab === 'feedback' }"
            @click="switchToFeedback"
          >My Feedback</button>
        </div>

        <div class="modal-body mypage-body">

          <!-- Profile 탭 -->
          <div v-if="tab === 'profile'" class="px-2">
            <h6 class="fw-bold mb-3">Change Nickname</h6>
            <div class="d-flex gap-2 align-items-start">
              <div class="flex-grow-1">
                <input
                  type="text"
                  class="form-control"
                  :class="{ 'is-invalid': nicknameError }"
                  v-model="nickname"
                  placeholder="New nickname"
                  maxlength="15"
                />
                <div v-if="nicknameError" class="text-danger small mt-1">{{ nicknameError }}</div>
              </div>
              <button class="btn mypage-primary-btn" :disabled="isSavingNickname" @click="saveNickname">
                Save
              </button>
            </div>
            <div v-if="nicknameMessage" class="mt-2 small text-danger">
              {{ nicknameMessage }}
            </div>

            <hr class="my-4" />

            <div class="danger-zone">
              <h6 class="fw-bold text-danger mb-1">Delete Account</h6>
              <p class="text-muted small mb-3">
                This will permanently delete your account, all your reports, and your images.
                Your comments will be anonymized. This action cannot be undone.
              </p>

              <div v-if="!showDeleteConfirm">
                <button class="btn mypage-danger-outline-btn btn-sm" @click="showDeleteConfirm = true">
                  Delete my account
                </button>
              </div>

              <div v-else class="delete-confirm">
                <p class="text-danger small fw-bold mb-2">Are you sure? This cannot be undone.</p>
                <div class="d-flex gap-2">
                  <button
                    class="btn mypage-danger-btn btn-sm"
                    :disabled="isDeletingAccount"
                    @click="deleteAccount"
                  >
                    {{ isDeletingAccount ? 'Deleting...' : 'Yes, delete my account' }}
                  </button>
                  <button class="btn mypage-secondary-btn btn-sm" @click="showDeleteConfirm = false">
                    Cancel
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- My Feedback 탭 -->
          <div v-else-if="tab === 'feedback'">
            <div v-if="isLoadingFeedback" class="text-center py-4 text-muted">Loading...</div>
            <div v-else-if="feedbacks.length === 0" class="text-center py-4 text-muted">
              No feedback submitted yet.
            </div>
            <ul v-else class="report-list">
              <li v-for="item in feedbacks" :key="item.id" class="report-item">
                <div class="report-title feedback-title" @click="openFeedbackReport(item)">{{ item.externalReportTitle }}</div>
                <hr class="my-2" />
                <div class="d-flex align-items-center justify-content-between">
                  <span class="soft-chip warning-chip">{{ formatReason(item.reason) }}</span>
                  <span class="text-muted small">{{ formatDate(item.createdAt) }}</span>
                </div>
                <div v-if="item.description" class="text-muted small mt-1">
                  <div style="white-space: pre-wrap; overflow-wrap: break-word;">{{ getDisplayDescription(item) }}</div>
                  <button v-if="isLongDescription(item.description)" class="btn btn-link btn-sm p-0" style="font-size: 0.75rem;" @click="toggleExpand(item.id)">
                    {{ expandedFeedbackIds.has(item.id) ? 'Less' : 'More' }}
                  </button>
                </div>
                <div :class="item.status === 'RESOLVED' ? 'status-bar resolved' : 'status-bar pending'">
                  <span class="status-label">Feedback Status</span>
                  <span :class="item.status === 'RESOLVED' ? 'soft-chip resolved-chip' : 'soft-chip pending-chip'">
                    {{ item.status === 'RESOLVED' ? 'Resolved' : 'Under Review' }}
                  </span>
                </div>
              </li>
            </ul>
          </div>

          <!-- My Reports 탭 -->
          <div v-else>
            <div v-if="isLoadingReports" class="text-center py-4 text-muted">Loading...</div>
            <div v-else-if="reports.length === 0" class="text-center py-4 text-muted">
              No reports yet.
            </div>
            <ul v-else class="report-list">
              <li v-for="report in reports" :key="report.id" class="report-item">
                <div v-if="confirmDeleteId !== report.id">
                  <div class="report-title">{{ report.title }}</div>
                  <div class="d-flex align-items-center justify-content-between mt-1">
                    <span class="text-muted small">{{ formatDate(report.createdAt) }}</span>
                    <div class="report-actions">
                      <button class="btn report-action-btn primary" @click="openUserReport(report.id)">View</button>
                      <button class="btn report-action-btn" @click="editReport(report)">Edit</button>
                      <button class="btn report-action-btn danger" @click="confirmDeleteId = report.id">Delete</button>
                    </div>
                  </div>
                </div>

                <!-- 인라인 삭제 확인 -->
                <div v-else class="delete-confirm">
                  <span class="text-danger small fw-bold">Delete this report?</span>
                  <div class="mt-2 d-flex gap-2">
                    <button class="btn mypage-danger-btn btn-sm" :disabled="isDeletingId === report.id" @click="deleteReport(report.id)">Yes, delete</button>
                    <button class="btn mypage-secondary-btn btn-sm" @click="confirmDeleteId = null">Cancel</button>
                  </div>
                </div>
              </li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useMapStore } from '@/stores/map';
import { useBootstrapModal } from '@/composables/useBootstrapModal';
import { useAuthStore } from '@/stores/auth';
import { useToast } from '@/composables/useToast';
import apiClient from '@/api/apiClient';

const emit = defineEmits(['edit-report']);

const authStore = useAuthStore();
const mapStore = useMapStore();
const modalRef = ref(null);
const { hide } = useBootstrapModal(modalRef);
const toast = useToast();

const tab = ref('profile');

// Profile
const nickname = ref(authStore.user?.nickname || '');
const nicknameError = ref('');
const nicknameMessage = ref('');
const isSavingNickname = ref(false);

// Delete Account
const showDeleteConfirm = ref(false);
const isDeletingAccount = ref(false);

const deleteAccount = async () => {
  isDeletingAccount.value = true;
  try {
    await apiClient.delete('/users/me');
    authStore.clearAccessToken();
    authStore.clearUser();
    window.location.reload();
  } catch (e) {
    console.error('Failed to delete account', e);
    toast.show('Failed to delete account. Please try again.');
    isDeletingAccount.value = false;
  }
};

const saveNickname = async () => {
  nicknameError.value = '';
  nicknameMessage.value = '';

  const trimmed = nickname.value.trim();

  if (!trimmed) {
    nicknameError.value = 'Please enter a nickname.';
    return;
  }
  if (trimmed.length < 2 || trimmed.length > 15) {
    nicknameError.value = 'Nickname must be between 2 and 15 characters.';
    return;
  }

  isSavingNickname.value = true;
  try {
    await apiClient.patch('/users/me/nickname', { nickname: nickname.value });
    authStore.setUser({ ...authStore.user, nickname: nickname.value });
    toast.show('Nickname updated successfully.');
  } catch (e) {
    const result = e.response?.data?.result;
    const fieldMessage = result && typeof result === 'object' ? Object.values(result)[0] : null;
    nicknameMessage.value = fieldMessage || e.response?.data?.message || 'Failed to update nickname.';
  } finally {
    isSavingNickname.value = false;
  }
};

// My Feedback
const feedbacks = ref([]);
const isLoadingFeedback = ref(false);
const expandedFeedbackIds = ref(new Set());

const switchToFeedback = () => {
  tab.value = 'feedback';
  loadFeedback();
};

const loadFeedback = async () => {
  isLoadingFeedback.value = true;
  try {
    const res = await apiClient.get('/users/me/inaccuracies');
    feedbacks.value = res.data.result;
  } catch (e) {
    console.error('Failed to load feedback', e);
  } finally {
    isLoadingFeedback.value = false;
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

const MAX_LINES = 3;
const MAX_CHARS = 150;

const isLongDescription = (text) => text.split('\n').length > MAX_LINES || text.length > MAX_CHARS;

const getDisplayDescription = (item) => {
  if (expandedFeedbackIds.value.has(item.id) || !isLongDescription(item.description)) {
    return item.description;
  }
  const byLines = item.description.split('\n').slice(0, MAX_LINES).join('\n');
  const truncated = byLines.length > MAX_CHARS ? byLines.slice(0, MAX_CHARS) : byLines;
  return truncated + '...';
};

const openFeedbackReport = async (item) => {
  try {
    await apiClient.get(`/external-reports/${item.externalReportId}`);
    hide();
    mapStore.requestOpenExternalReport(item.externalReportId);
  } catch (e) {
    if (e.response?.status === 404) {
      toast.show('This report is no longer available.');
    }
  }
};

const toggleExpand = (id) => {
  const next = new Set(expandedFeedbackIds.value);
  next.has(id) ? next.delete(id) : next.add(id);
  expandedFeedbackIds.value = next;
};

// My Reports
const reports = ref([]);
const isLoadingReports = ref(false);
const confirmDeleteId = ref(null);
const isDeletingId = ref(null);

const switchToReports = () => {
  tab.value = 'reports';
  loadReports();
};

const loadReports = async () => {
  isLoadingReports.value = true;
  try {
    const res = await apiClient.get('/users/me/reports');
    reports.value = res.data.result;
  } catch (e) {
    console.error('Failed to load reports', e);
  } finally {
    isLoadingReports.value = false;
  }
};

const deleteReport = async (id) => {
  isDeletingId.value = id;
  try {
    await apiClient.delete(`/user-reports/${id}`);
    reports.value = reports.value.filter(r => r.id !== id);
    confirmDeleteId.value = null;
    toast.show('Report deleted.');
  } catch (e) {
    console.error('Failed to delete report', e);
  } finally {
    isDeletingId.value = null;
  }
};

const editReport = (report) => {
  hide();
  emit('edit-report', report);
};

const openUserReport = (reportId) => {
  hide();
  mapStore.requestOpenUserReportFromEdit(reportId);
};

const formatDate = (dateStr) => {
  return new Date(dateStr).toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' });
};

const pendingTab = ref(null);

const resetState = () => {
  tab.value = pendingTab.value ?? 'profile';
  pendingTab.value = null;
  nickname.value = authStore.user?.nickname || '';
  nicknameError.value = '';
  nicknameMessage.value = '';
  showDeleteConfirm.value = false;
  reports.value = [];
  feedbacks.value = [];
  expandedFeedbackIds.value = new Set();
  confirmDeleteId.value = null;
  if (tab.value === 'feedback') loadFeedback();
  else if (tab.value === 'reports') loadReports();
};

onMounted(() => {
  modalRef.value?.addEventListener('show.bs.modal', resetState);
});

const refreshReports = () => {
  if (tab.value === 'reports') loadReports();
};

const openOnTab = (tab) => {
  pendingTab.value = tab;
};

defineExpose({ refreshReports, openOnTab });
</script>

<style scoped lang="scss">
.mypage-modal {
  overflow: hidden;
  border: 1px solid var(--safetrip-border);
  border-radius: 14px;
  background: var(--safetrip-page);
}

.mypage-header {
  background: #fffdf8;
  border-bottom: 1px solid var(--safetrip-border);
  padding: 20px 24px;
}

.mypage-body {
  padding: 22px 24px 24px;
}

.tab-nav {
  display: flex;
  gap: 0;
  background: #fffdf8;
  border-bottom: 1px solid var(--safetrip-border);
  padding: 0 18px;
}

.tab-btn {
  background: none;
  border: none;
  border-bottom: 2px solid transparent;
  padding: 12px 18px;
  font-size: 0.9rem;
  color: var(--safetrip-muted);
  cursor: pointer;
  transition: color 0.2s, border-color 0.2s;

  &.active {
    color: var(--safetrip-primary);
    border-bottom-color: var(--safetrip-primary);
    font-weight: 800;
  }

  &:hover:not(.active) {
    color: var(--safetrip-text);
  }
}

.form-control {
  border-color: var(--safetrip-border);
  background: #fffdf8;
  color: var(--safetrip-text);

  &:focus {
    border-color: var(--safetrip-primary);
    box-shadow: 0 0 0 3px rgba(42, 157, 143, 0.12);
  }
}

.text-danger {
  color: #b6523b !important;
}

.report-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.report-item {
  margin-bottom: 10px;
  padding: 14px 15px;
  border: 1px solid var(--safetrip-border);
  border-radius: 12px;
  background: var(--safetrip-surface);
  box-shadow: 0 8px 24px rgba(36, 49, 58, 0.05);

  &:last-child {
    margin-bottom: 0;
  }
}

.report-title {
  font-weight: 700;
  margin-bottom: 8px;
  color: var(--safetrip-text);
}

.feedback-title {
  cursor: pointer;
  margin-bottom: 0;

  &:hover {
    color: var(--safetrip-primary);
    text-decoration: underline;
  }
}

.report-actions {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.report-action-btn {
  border: 1px solid var(--safetrip-border);
  border-radius: 999px;
  background: #fffdf8;
  color: var(--safetrip-muted);
  padding: 3px 10px;
  font-size: 0.78rem;
  font-weight: 800;

  &:hover,
  &:focus {
    color: var(--safetrip-text);
    background: #f7f1e8;
    border-color: #d8cec2;
  }

  &.primary {
    color: var(--safetrip-primary);
    border-color: #b9e4dc;

    &:hover,
    &:focus {
      background: var(--safetrip-primary-soft);
      border-color: var(--safetrip-primary);
    }
  }

  &.danger {
    color: #b6523b;
    border-color: #f3c6b8;

    &:hover,
    &:focus {
      background: #fff1ec;
      border-color: #d97757;
    }
  }
}

.delete-confirm {
  padding: 12px;
  background: #fff1ec;
  border-radius: 10px;
  border: 1px solid #f3c6b8;
}

.status-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 8px;
  padding: 8px 10px;
  border-radius: 10px;
  font-size: 0.75rem;

  &.resolved {
    background: var(--safetrip-primary-soft);
  }

  &.pending {
    background: #f7f1e8;
  }
}

.status-label {
  color: var(--safetrip-muted);
  font-weight: 700;
}

.soft-chip {
  display: inline-flex;
  align-items: center;
  border-radius: 999px;
  padding: 4px 9px;
  font-size: 0.75rem;
  font-weight: 800;
}

.warning-chip {
  color: #73511f;
  background: #fff7df;
  border: 1px solid #ecd39a;
}

.resolved-chip {
  color: #1f7f74;
  background: var(--safetrip-primary-soft);
  border: 1px solid #b9e4dc;
}

.pending-chip {
  color: var(--safetrip-muted);
  background: #fffdf8;
  border: 1px solid var(--safetrip-border);
}

.danger-zone {
  border: 1px solid #f3c6b8;
  border-radius: 12px;
  padding: 16px;
  background: #fff1ec;
}

.mypage-primary-btn {
  color: #fff;
  background: var(--safetrip-primary);
  border-color: var(--safetrip-primary);

  &:hover,
  &:focus {
    color: #fff;
    background: var(--safetrip-primary-hover);
    border-color: var(--safetrip-primary-hover);
  }

  &:disabled {
    background: #a7cfc8;
    border-color: #a7cfc8;
    opacity: 1;
  }
}

.mypage-secondary-btn {
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

.mypage-danger-outline-btn,
.mypage-danger-btn {
  color: #b6523b;
  border-color: #f3c6b8;
}

.mypage-danger-outline-btn {
  background: #fffdf8;

  &:hover,
  &:focus {
    color: #944330;
    background: #fff1ec;
    border-color: #d97757;
  }
}

.mypage-danger-btn {
  color: #fff;
  background: #b6523b;

  &:hover,
  &:focus {
    color: #fff;
    background: #944330;
    border-color: #944330;
  }
}
</style>
