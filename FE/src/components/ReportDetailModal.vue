<template>
  <div class="modal fade" id = "reportDetailModal" data-bs-backdrop="static" data-bs-keyboard="false" tabindex="-1" aria-labelledby="staticBackdropLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered modal-dialog-scrollable modal-lg">
      <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="staticBackdropLabel">{{ report.title }}</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <!-- <div class="modal-body">
            <p><strong>사기 유형: </strong>{{ report.scam }}</p>
            <p><strong>주소: </strong>{{ report.address }}</p>
            <p><strong>사진: </strong>{{ report.image }}</p>
            <p><strong>설명:</strong></p>
            <p class="text-muted">{{ report.description }}</p>
            <p><strong>팁/조언:</strong></p>
            <p class="text-muted">{{ report.advice }}</p>
            <p><strong>작성일시: </strong> {{ report.createdAt }}</p>
          </div>       -->
          <div class="modal-body space-y-3">
            <div>
              <strong>사기 유형:</strong>
              <span class="text-muted ms-2">{{ report.scam }}</span>
            </div>

            <div>
              <strong>주소:</strong>
              <span class="text-muted ms-2">{{ report.address }}</span>
            </div>

            <div>
              <strong>사진:</strong><br />
              <img 
                v-if="report.image" 
                :src="report.image" 
                alt="사기 관련 사진" 
                class="img-fluid rounded border mt-2" 
                style="max-height: 300px;"
              />
              <p v-else class="text-muted mt-1">첨부된 이미지 없음</p>
            </div>

            <div>
              <strong>설명:</strong>
              <p class="text-muted mt-1 mb-0">{{ report.description }}</p>
            </div>

            <div>
              <strong>팁/조언:</strong>
              <p class="text-muted mt-1 mb-0">{{ report.advice }}</p>
            </div>

            <div>
              <strong>작성일시:</strong>
              <span class="text-muted ms-2">{{ report.createdAt }}</span>
            </div>
          </div>
        </div>    
    </div>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'

defineProps({
  report: {
    type: Object,
    required: true
  }
})



const setupModalEventListener = () => {
  const modal = document.getElementById('reportDetailModal');

  if (modal) {
    modal.addEventListener('hide.bs.modal', () => {
      document.activeElement.blur();
    });
  }
}

onMounted(() => {
  setupModalEventListener()
})

</script>
<style scoped lang="scss">

</style>