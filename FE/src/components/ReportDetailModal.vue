<template>
  <div class="modal fade" id = "reportDetailModal" data-bs-backdrop="static" data-bs-keyboard="false" tabindex="-1" aria-labelledby="staticBackdropLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered modal-dialog-scrollable modal-lg">
      <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="staticBackdropLabel">{{ report.title }}</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <div class="d-flex justify-content-between mb-3">
              <div>
                <strong>사기 유형</strong><br>
                {{ report.scam }}
              </div>
              <div>
                <small class="text-muted">작성일시</small><br>
                <span>{{ report.createdAt }}</span>
              </div>
            </div>
            <div class="mb-3">
              <span class="fw-bold">설명</span>
              <p class="text-body mt-1">{{ report.description }}</p>
            </div>

            <div class="mb-3" >
              <span class="fw-bold">조언</span>
              <p class="text-body mt-1">{{ report.advice }}</p>
            </div>

            <!-- 장소 -->
            <div class="mb-3">
              <span class="fw-bold">장소</span>
              <p class="text-body">{{ report.address }}</p>
            </div>

            <!-- 사진 -->
            <div class="mb-3">
              <span class="fw-bold">사진</span>
              <img 
                v-if="report.image" 
                :src="report.image" 
                alt="사기 관련 사진" 
                class="img-fluid rounded border mt-2" 
                style="max-height: 300px;"
              />
              <p v-else class="text-muted mt-1">첨부된 이미지 없음</p>
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