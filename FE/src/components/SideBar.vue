<template>
  <div class="sidebar-container">
    <!-- 사이드바 -->
    <div :class="['sidebar', {'open': isOpen}]">
      <div class="sidebar-header form-group">
        <input
        type="text"
        v-model="searchText"
        placeholder="국가, 도시 검색"
        class="form-control form-control-lg mb-3"
        />
      
      </div>
      <div class="sidebar-body">
        <ul class="list-group">
          <li class="list-group-item d-flex justify-content-between align-items-start">
            <div class="ms-2 me-auto">
              <div class="fw-bold">크로아티아</div>
            </div>
            <span class="badge text-bg-primary rounded-pill">14</span>
          </li>
          <li class="list-group-item d-flex justify-content-between align-items-start">
            <div class="ms-2 me-auto">
              <div class="fw-bold">스페인</div>
            </div>
            <span class="badge text-bg-primary rounded-pill">14</span>
          </li>
          <li class="list-group-item d-flex justify-content-between align-items-start">
            <div class="ms-2 me-auto">
              <div class="fw-bold">프랑스</div>
            </div>
            <span class="badge text-bg-primary rounded-pill">14</span>
          </li>
          <li class="list-group-item d-flex justify-content-between align-items-start">
            <div class="ms-2 me-auto">
              <div class="fw-bold">인도</div>
            </div>
            <span class="badge text-bg-primary rounded-pill">14</span>
          </li>
          <li class="list-group-item d-flex justify-content-between align-items-start">
            <div class="ms-2 me-auto">
              <div class="fw-bold">대한민국</div>
            </div>
            <span class="badge text-bg-primary rounded-pill">14</span>
          </li>
          <li class="list-group-item d-flex justify-content-between align-items-start">
            <div class="ms-2 me-auto">
              <div class="fw-bold">브라질</div>
            </div>
            <span class="badge text-bg-primary rounded-pill">14</span>
          </li>
          <li class="list-group-item d-flex justify-content-between align-items-start">
            <div class="ms-2 me-auto">
              <div class="fw-bold">코트디부아르</div>
            </div>
            <span class="badge text-bg-primary rounded-pill">14</span>
          </li>
          <li class="list-group-item d-flex justify-content-between align-items-start">
            <div class="ms-2 me-auto">
              <div class="fw-bold">케냐</div>
            </div>
            <span class="badge text-bg-primary rounded-pill">14</span>
          </li>
          <li class="list-group-item d-flex justify-content-between align-items-start">
            <div class="ms-2 me-auto">
              <div class="fw-bold">러시아</div>
            </div>
            <span class="badge text-bg-primary rounded-pill">14</span>
          </li>
          <li class="list-group-item d-flex justify-content-between align-items-start">
            <div class="ms-2 me-auto">
              <div class="fw-bold">오스트레일리아</div>
            </div>
            <span class="badge text-bg-primary rounded-pill">14</span>
          </li>
          <li class="list-group-item d-flex justify-content-between align-items-start">
            <div class="ms-2 me-auto">
              <div class="fw-bold">오스트리아</div>
            </div>
            <span class="badge text-bg-primary rounded-pill">14</span>
          </li>
          <li class="list-group-item d-flex justify-content-between align-items-start">
            <div class="ms-2 me-auto">
              <div class="fw-bold">독일</div>
            </div>
            <span class="badge text-bg-primary rounded-pill">14</span>
          </li>
        </ul>
      </div>
    </div>

    <!-- 토글 버튼 (사이드바 열기) -->
    <button @click="toggleSidebar" class="toggle-btn">
      <span v-if="!isOpen">▶</span>
      <span v-else>◀</span>
    </button>
  </div>
</template>

<script setup>
import { ref } from 'vue';

const isOpen = ref(false);
const searchText = ref('');


const toggleSidebar = () => {
  isOpen.value = !isOpen.value;
};
</script>

<style scoped lang="scss">

/* 사이드바 컨테이너 */
.sidebar-container {
  position: fixed;
  display: flex;
  height: 100vh;
  z-index: 1000;
}

/* 사이드바 */
.sidebar {
  position: fixed;
  display: flex;
  top: 0;
  left: -400px; /* 기본적으로 숨김 */
  width: 400px;
  height: 100vh;
  background-color: #e0e0e0bd;
  transition: left 0.3s ease;
  padding: 20px;
  flex-direction: column;
}

/* 사이드바가 열릴 때 */
.sidebar.open {
  left: 0;
}

/* 사이드바 헤더 */
.sidebar-header {
  align-items: center;
  font-size: 18px;
  margin-bottom: 20px;
}

.sidebar-body {
  padding-left: 4px;
  padding-right: 4px;
  overflow-y: auto;
  overscroll-behavior: contain; /* 스크롤 범위를 벗어나면 움직이지 않게 */
}

/* 사이드바가 열렸을 때 토글 버튼 위치 조정 */
.sidebar.open + .toggle-btn {
  left: 400px;
}

.list-group-item {
  background-color: #f8f9fa; /* 배경색 */
  color: black;
  border-radius: 8px; /* 모서리를 둥글게 */
  padding: 12px; /* 내부 여백 */
  margin-bottom: 12px; /* 아래 여백 */
  margin-top: 12px;
  border: 1px solid #ddd; /* 테두리 */
  box-shadow: 2px 2px 5px rgba(0, 0, 0, 0.1); /* 그림자 효과 */
}

/* 토글 버튼 (사이드바 여닫기 버튼) */
.toggle-btn {
  position: fixed;
  top: 50%;
  left: 0;
  transform: translateY(-50%);
  background-color: #0d6efd;
  color: white;
  border: none;
  padding: 10px 15px;
  font-size: 18px;
  cursor: pointer;
  transition: all 0.3s;
}
</style>
