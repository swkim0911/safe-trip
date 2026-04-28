<template>
  <div class="modal fade" ref="modalRef" data-bs-backdrop="static" data-bs-keyboard="false" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered modal-md">
      <div class="modal-content">
        <div class="modal-body px-4 py-4">
          <div class="text-center mb-3">
            <img src="/airplane.png" alt="SafeTrip" width="40" height="40" />
            <h4 class="fw-bold mt-2 mb-0">SafeTrip</h4>
            <p class="text-muted small mt-1">Make Your Travel Safer</p>
          </div>

          <p class="mb-3">Browse scam reports from real travelers around the world. Click on a country or city to explore scam types and actual cases.</p>

          <ul class="info-list mb-3">
            <li>Data is collected from Reddit travel communities and user-submitted reports.</li>
            <li>For reference only — accuracy is not guaranteed. You can flag inaccurate reports to help us improve.</li>
          </ul>

          <p class="mb-3 small text-muted">
            SafeTrip is in early stage and we'd love your feedback. →
            <a href="mailto:safetripworld.contact@gmail.com" class="feedback-link">Send Feedback</a>
          </p>

          <div class="form-check mb-3">
            <input class="form-check-input" type="checkbox" id="dontShowAgain" v-model="dontShowAgain" />
            <label class="form-check-label small text-muted" for="dontShowAgain">Don't show again</label>
          </div>

          <button class="btn btn-primary w-100 fw-bold" @click="close">Start Exploring</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { Modal } from 'bootstrap';

const modalRef = ref(null);
let modal = null;

const STORAGE_KEY = 'safetrip_welcomed';
const dontShowAgain = ref(false);

const close = () => {
  if (dontShowAgain.value) {
    localStorage.setItem(STORAGE_KEY, 'true');
  }
  modal.hide();
};

onMounted(() => {
  modal = new Modal(modalRef.value);
  if (!localStorage.getItem(STORAGE_KEY)) {
    modal.show();
  }
});
</script>

<style scoped lang="scss">
.info-list {
  padding-left: 1.2rem;
  li {
    margin-bottom: 6px;
    font-size: 14px;
    color: #444;
  }
}

.feedback-link {
  color: #3B82F6;
  text-decoration: none;
  &:hover {
    text-decoration: underline;
  }
}
</style>
