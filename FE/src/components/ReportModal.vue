<template>
  <div class="modal fade" ref="modal" data-bs-backdrop="static" data-bs-keyboard="false" tabindex="-1" aria-labelledby="staticBackdropLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered modal-dialog-scrollable">
      <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="staticBackdropLabel">제보를 남겨주세요</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <div class="mb-3">
              <label for="report-title" class="col-form-label">
                제목
              </label>
              <input type="text" class="form-control" id="report-title" placeholder="제목을 입력하세요">
            </div>
            <div class="btn-group" role="group">
              <input type="radio" class="btn-check" name="scamType" id="scam1" autocomplete="off">
              <label class="btn btn-outline-primary" for="scam1">교통수단</label>

              <input type="radio" class="btn-check" name="scamType" id="scam2" autocomplete="off">
              <label class="btn btn-outline-primary" for="scam2">소매치기</label>

              <input type="radio" class="btn-check" name="scamType" id="scam3" autocomplete="off">
              <label class="btn btn-outline-primary" for="scam3">투어 사기</label>

              <input type="radio" class="btn-check" name="scamType" id="scam4" autocomplete="off">
              <label class="btn btn-outline-primary" for="scam4">강매 / 바가지</label>

              <input type="radio" class="btn-check" name="scamType" id="scam5" autocomplete="off">
              <label class="btn btn-outline-primary" for="scam5">기타</label>
            </div>
            <div class="mb-3">
              <p>위치 선택</p>
            </div>
            <div class="mb-3">
              <label for="photo" class="form-label">사진 업로드</label>
              <input class="form-control" type="file" id="photo" accept="image/*" multiple>
            </div>

            <div class="mb-3">
              <label for="report-description" class="col-form-label">내용</label>
              <textarea class="form-control" id="report-description"></textarea>
            </div>
            <div class="mb-3">
              <label for="report-advice" class="col-form-label">조언</label>
              <textarea class="form-control" id="report-advice"></textarea>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">close</button>
            <button type="button" class="btn btn-primary">send</button>
          </div>
        </div>    
    </div>
  </div>
</template>
<script >

</script>
<style lang="scss">
  
</style>