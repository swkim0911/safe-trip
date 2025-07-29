
import { ref, onMounted, onBeforeUnmount, unref } from 'vue';
import { Modal } from 'bootstrap';

/**
 * target: 1) ref(HTMLElement)  2) 문자열 selector('#authFormModal' / 'authFormModal')
 * options: Modal 옵션 (backdrop, keyboard 등)
 */
export function useBootstrapModal(target, options = { backdrop: 'static', keyboard: false }) {
  const instance = ref(null);

  const getEl = () => {
    const t = unref(target);
    if (typeof t === 'string') {
      return document.getElementById(t.replace(/^#/, '')) || document.querySelector(t);
    }
    return t; // ref(HTMLElement)
  };

  const ensureInstance = () => {
    const el = getEl();
    if (!el) return null;
    instance.value = Modal.getOrCreateInstance(el, options);
    return instance.value;
  };

  const show = () => {
    (instance.value || ensureInstance())?.show();
  };

  const hide = () => {
    (instance.value || ensureInstance())?.hide();
  };

  const dispose = () => {
    instance.value?.dispose();
    instance.value = null;
  };

  onMounted(() => ensureInstance());
  onBeforeUnmount(() => dispose());

  return { show, hide, dispose, instance };
}
