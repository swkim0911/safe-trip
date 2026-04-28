<template>
  <Transition name="slide-up">
    <div v-if="visible" class="cookie-banner">
      <div class="cookie-content">
        <p class="cookie-text">
          We use cookies to analyze site usage. By clicking <strong>Accept</strong>, you agree to our
          <a href="#" @click.prevent="openModal('privacyPolicyModal')">Privacy Policy</a>
          and
          <a href="#" @click.prevent="openModal('termsOfServiceModal')">Terms of Service</a>.
        </p>
        <div class="cookie-actions">
          <button class="btn btn-outline-secondary btn-sm" @click="decline">Decline</button>
          <button class="btn btn-primary btn-sm" @click="accept">Accept</button>
        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { Modal } from 'bootstrap';

const CONSENT_KEY = 'safetrip_cookie_consent';
const visible = ref(false);

const openModal = (id) => {
  const el = document.getElementById(id);
  if (el) Modal.getOrCreateInstance(el).show();
};

const accept = () => {
  localStorage.setItem(CONSENT_KEY, 'accepted');
  visible.value = false;
  if (import.meta.env.PROD) window.__initGA?.();
};

const decline = () => {
  localStorage.setItem(CONSENT_KEY, 'declined');
  visible.value = false;
};

onMounted(() => {
  if (!localStorage.getItem(CONSENT_KEY)) {
    visible.value = true;
  }
});
</script>

<style scoped lang="scss">
.cookie-banner {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 2000;
  background: #1e293b;
  color: #f1f5f9;
  padding: 14px 24px;
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.2);
}

.cookie-content {
  max-width: 900px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.cookie-text {
  margin: 0;
  font-size: 13px;
  line-height: 1.6;
  flex: 1;
  min-width: 200px;

  a {
    color: #93c5fd;
    text-decoration: underline;
    &:hover { color: #bfdbfe; }
  }
}

.cookie-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;

  .btn-outline-secondary {
    color: #cbd5e1;
    border-color: #475569;
    &:hover { background: #334155; color: #f1f5f9; }
  }
}

.slide-up-enter-active,
.slide-up-leave-active {
  transition: transform 0.3s ease, opacity 0.3s ease;
}

.slide-up-enter-from,
.slide-up-leave-to {
  transform: translateY(100%);
  opacity: 0;
}
</style>
