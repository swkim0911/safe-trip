import { ref } from 'vue';

const message = ref('');
const visible = ref(false);
const status = ref('success');
let timer = null;

export function useToast() {
  const show = (msg, type = 'success', duration = 2500) => {
    message.value = msg;
    status.value = type;
    visible.value = true;
    if (timer) clearTimeout(timer);
    timer = setTimeout(() => { visible.value = false; }, duration);
  };

  return { message, visible, status, show };
}
