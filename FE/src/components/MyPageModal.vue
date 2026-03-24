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
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">My Page</h5>
          <button type="button" class="btn-close" @click="hide" aria-label="Close"></button>
        </div>

        <!-- 탭 -->
        <div class="tab-nav border-bottom px-3">
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

        <div class="modal-body">

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
              <button class="btn btn-primary" :disabled="isSavingNickname" @click="saveNickname">
                Save
              </button>
            </div>
            <div v-if="nicknameMessage" class="mt-2 small text-danger">
              {{ nicknameMessage }}
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
                <div class="report-title">{{ item.externalReportTitle }}</div>
                <div class="d-flex align-items-center justify-content-between mt-1">
                  <span class="badge bg-warning text-dark small">{{ formatReason(item.reason) }}</span>
                  <span class="text-muted small">{{ formatDate(item.createdAt) }}</span>
                </div>
                <div v-if="item.description" class="text-muted small mt-1">{{ item.description }}</div>
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
                      <button class="btn btn-sm btn-outline-primary" @click="editReport(report)">Edit</button>
                      <button class="btn btn-sm btn-outline-danger ms-2" @click="confirmDeleteId = report.id">Delete</button>
                    </div>
                  </div>
                </div>

                <!-- 인라인 삭제 확인 -->
                <div v-else class="delete-confirm">
                  <span class="text-danger small fw-bold">Delete this report?</span>
                  <div class="mt-2 d-flex gap-2">
                    <button class="btn btn-sm btn-danger" :disabled="isDeletingId === report.id" @click="deleteReport(report.id)">Yes, delete</button>
                    <button class="btn btn-sm btn-secondary" @click="confirmDeleteId = null">Cancel</button>
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
import { useBootstrapModal } from '@/composables/useBootstrapModal';
import { useAuthStore } from '@/stores/auth';
import { useToast } from '@/composables/useToast';
import apiClient from '@/api/apiClient';

const emit = defineEmits(['edit-report']);

const authStore = useAuthStore();
const modalRef = ref(null);
const { hide } = useBootstrapModal(modalRef);
const toast = useToast();

const tab = ref('profile');

// Profile
const nickname = ref(authStore.user?.nickname || '');
const nicknameError = ref('');
const nicknameMessage = ref('');
const isSavingNickname = ref(false);

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
  emit('edit-report', report);
};

const formatDate = (dateStr) => {
  return new Date(dateStr).toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' });
};

const resetState = () => {
  tab.value = 'profile';
  nickname.value = authStore.user?.nickname || '';
  nicknameError.value = '';
  nicknameMessage.value = '';
  reports.value = [];
  feedbacks.value = [];
  confirmDeleteId.value = null;
};

onMounted(() => {
  modalRef.value?.addEventListener('show.bs.modal', resetState);
});

const refreshReports = () => {
  if (tab.value === 'reports') loadReports();
};

defineExpose({ refreshReports });
</script>

<style scoped lang="scss">
.tab-nav {
  display: flex;
  gap: 0;
}

.tab-btn {
  background: none;
  border: none;
  border-bottom: 2px solid transparent;
  padding: 10px 18px;
  font-size: 0.9rem;
  color: #6c757d;
  cursor: pointer;
  transition: color 0.2s, border-color 0.2s;

  &.active {
    color: #0d6efd;
    border-bottom-color: #0d6efd;
    font-weight: 600;
  }

  &:hover:not(.active) {
    color: #343a40;
  }
}

.report-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.report-item {
  padding: 12px 4px;
  border-bottom: 1px solid #f0f0f0;

  &:last-child {
    border-bottom: none;
  }
}

.report-title {
  font-weight: 500;
  margin-bottom: 8px;
  color: #212529;
}

.report-actions {
  display: flex;
}

.delete-confirm {
  padding: 8px;
  background: #fff5f5;
  border-radius: 6px;
  border: 1px solid #ffcccc;
}
</style>
