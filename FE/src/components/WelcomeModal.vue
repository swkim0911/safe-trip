<template>
  <div class="modal fade" ref="modalRef" data-bs-backdrop="static" data-bs-keyboard="false" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered modal-md">
      <div class="modal-content">
        <div class="modal-body px-4 py-4">
          <div class="text-center mb-3">
            <img src="/airplane.png" alt="SafeTrip" width="40" height="40" />
            <h4 class="fw-bold mt-2 mb-0">SafeTrip</h4>
            <p class="text-muted small mt-1">Travel smarter, worry less</p>
          </div>

          <p class="mb-3">Explore real travel scam stories and local patterns before your trip, so you can plan with a little more confidence.</p>

          <ul class="info-list mb-3">
            <li>Browse reports from Reddit travel communities and SafeTrip travelers.</li>
            <li>Use each report as a helpful reference, and flag anything that looks inaccurate.</li>
          </ul>

          <p class="mb-3 small text-muted">
            Have an idea to make trip planning easier?
            <a href="mailto:safetripworld.contact@gmail.com" class="feedback-link">Send Feedback</a>
          </p>

          <div class="form-check mb-3">
            <input class="form-check-input" type="checkbox" id="dontShowAgain" v-model="dontShowAgain" />
            <label class="form-check-label small text-muted" for="dontShowAgain">Don't show again</label>
          </div>

          <button class="btn btn-primary w-100 fw-bold" @click="close">Explore the Map</button>

          <p class="text-center mt-3 mb-0" style="font-size: 12px; color: #aaa;">
            By using SafeTrip, you agree to our
            <a href="#" class="policy-link" @click.prevent="openModal('termsOfServiceModal')">Terms of Service</a>
            and
            <a href="#" class="policy-link" @click.prevent="openModal('privacyPolicyModal')">Privacy Policy</a>.
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { Modal } from 'bootstrap';

const openModal = (id) => {
  const el = document.getElementById(id);
  if (el) Modal.getOrCreateInstance(el).show();
};

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
  color: var(--safetrip-primary);
  text-decoration: none;
  &:hover {
    text-decoration: underline;
  }
}

.policy-link {
  color: #aaa;
  text-decoration: underline;
  &:hover { color: #666; }
}
</style>
