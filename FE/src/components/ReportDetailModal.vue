<template>
  <div class="modal fade" ref="modalRef" id = "reportDetailModal" data-bs-backdrop="static" data-bs-keyboard="false" tabindex="-1" aria-labelledby="staticBackdropLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered modal-dialog-scrollable modal-lg">
      <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="staticBackdropLabel">Travel Scam Report</h5>
            <button type="button" class="btn-close" @click="hide" aria-label="Close"></button>
          </div>

<div class="modal-body">
  <div class="d-flex justify-content-between align-items-start mb-3">
    <h5 class="fw-bold mb-0">{{ report.title }}</h5>

    <div class="text-end small ms-3">
      <div>
        <span v-if="report.source === 'safetrip'" class="fw-bold">
          by {{ report.nickname }}
        </span>
        <span v-else>
          <span class="text-muted">by {{ report.author }}</span>
          <span class="fw-bold text-primary ms-1">on {{ report.source }}</span>
        </span>
      </div>
      <div v-if="report.sourceUrl">
        <a :href="report.sourceUrl" target="_blank" class="small text-primary text-decoration-none">
          🔗 Original
        </a>
      </div>
    </div>

  </div>

  <div class="mb-3">
    <span class="badge bg-danger me-1">{{ report.scamAction }}</span>
    <span class="badge bg-warning">{{ report.scamContext }}</span>
  </div>

  <div class="mb-3">
    <span class="fw-bold">Summary</span>
    <p class="text-body mt-1">{{ report.summary }}</p>
  </div>

  <div class="mb-3">
    <span class="fw-bold">Location</span>
    <p class="text-body">
      {{ [report.countryName, report.stateName, report.cityName].filter(Boolean).join(', ') }}
    </p>
  </div>

  <div class="d-flex flex-column text-end text-muted small mt-2">
    <div>
      Posted: {{ report.postedAt }}
    </div>
  </div>
</div>


        </div>    
    </div>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { useBootstrapModal } from '@/composables/useBootstrapModal';

const modalRef = ref(null);
const { hide } = useBootstrapModal(modalRef);

defineProps({
  report: {
    type: Object,
    required: true
  }
});

const setupModalEventListener = () => {
  const modal = document.getElementById('reportDetailModal');

  if (modal) {
    modal.addEventListener('hide.bs.modal', () => {
      document.activeElement.blur();
    });
  }
}

onMounted(() => {
  setupModalEventListener();
})

</script>
<style scoped lang="scss">

</style>