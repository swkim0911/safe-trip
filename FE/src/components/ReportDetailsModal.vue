<template>
  <div class="modal fade" id = "reportDetailsModal" data-bs-backdrop="static" data-bs-keyboard="false" tabindex="-1" aria-labelledby="staticBackdropLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered modal-dialog-scrollable modal-lg">
      <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="staticBackdropLabel">title</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <p><strong>사기 유형:</strong>사기</p>
            <p><strong>주소:</strong>주소</p>
            <p><strong>위치:</strong> 위도, 경도</p>
            <p><strong>설명:</strong></p>
            <p class="text-muted">description</p>
            <p><strong>팁/조언:</strong></p>
            <p class="text-muted">advice</p>
            <p><strong>작성일시:</strong> createdAt</p>

          <!-- <div v-if="report.urls.length > 0" class="mt-3"> -->
            <strong>첨부 이미지:</strong>
            <div class="d-flex flex-wrap gap-2 mt-2">
              <!-- <img
                v-for="(url, index) in report.urls"
                :key="index"
                :src="url"
                alt="첨부 이미지"
                class="img-thumbnail"
                style="max-width: 150px;"
              /> -->
            </div>
          </div>      
        </div>    
    </div>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'

const setupModalEventListener = () => {
  const modal = document.getElementById('reportDetailsModal');

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