<template>
  <Transition name="toast">
    <div v-if="visible" class="app-toast" :class="status">
      <font-awesome-icon
        :icon="status === 'success' ? ['fas', 'circle-check'] : ['fas', 'circle-xmark']"
        class="toast-icon"
      />
      {{ message }}
    </div>
  </Transition>
</template>

<script setup>
import { useToast } from '@/composables/useToast';
const { message, visible, status } = useToast();
</script>

<style scoped lang="scss">
.app-toast {
  position: fixed;
  top: 24px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 9999;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  border-radius: 8px;
  font-size: 0.9rem;
  font-weight: 500;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  white-space: nowrap;

  &.success {
    background: #198754;
    color: white;
  }

  &.error {
    background: #dc3545;
    color: white;
  }
}

.toast-icon {
  font-size: 1rem;
}

.toast-enter-active,
.toast-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}

.toast-enter-from {
  opacity: 0;
  transform: translateX(-50%) translateY(-12px);
}

.toast-leave-to {
  opacity: 0;
  transform: translateX(-50%) translateY(-8px);
}
</style>
