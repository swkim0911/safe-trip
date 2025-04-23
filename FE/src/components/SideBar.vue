<template>
  <div class="sidebar-container">
    <!-- 사이드바 -->
    <div :class="['sidebar', {'open': isOpen}]">
      <div class="sidebar-header form-group">
        <input
        type="text"
        v-model="searchText"
        placeholder="검색어를 입력하세요"
        class="form-control form-control-lg mb-3"
        />
      
      </div>
      <div class="sidebar-body">
        <ul>
          <li> item</li>
          <li> item</li>
          <li> item</li>
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

const toggleSidebar = () => {
  isOpen.value = !isOpen.value;
};
</script>

<style scoped >
/* 사이드바 컨테이너 */
.sidebar-container {

}

/* 사이드바 스타일 */
.sidebar {
  position: fixed;
  top: 0;
  left: -500px; /* 기본적으로 숨김 */
  width: 500px;
  height: 100vh;
  color: white;
  background-color: #343a40;
  transition: left 0.3s ease;
  padding: 20px;
}

/* 사이드바가 열릴 때 */
.sidebar.open {
  left: 0;
}

/* 사이드바 헤더 */
.sidebar-header {
  display: flex;
  align-items: center;
  font-size: 18px;
  margin-bottom: 10px;
}

/* 사이드바가 열렸을 때 토글 버튼 위치 조정 */
.sidebar.open + .toggle-btn {
  left: 500px;
}

.sidebar-body ul {
  list-style: none; /* 기본 리스트 스타일 제거 */
  padding: 0;
}

.sidebar-body li {
  background-color: #f8f9fa; /* 배경색 */
  border-radius: 10px; /* 모서리를 둥글게 */
  padding: 12px; /* 내부 여백 */
  margin-bottom: 8px; /* 아래 여백 */
  border: 1px solid #ddd; /* 테두리 */
  box-shadow: 2px 2px 5px rgba(0, 0, 0, 0.1); /* 그림자 효과 */
  transition: transform 0.2s ease-in-out;
}

.sidebar-body li:hover {
  background-color: #e9ecef; /* 마우스 오버 시 색 변경 */
  transform: scale(1.05); /* 약간 확대 */
}

/* 토글 버튼 (사이드바 여닫기 버튼) */
.toggle-btn {
  position: fixed;
  top: 50%;
  left: 0;
  transform: translateY(-50%);
  background-color: #007bff;
  color: white;
  border: none;
  padding: 10px 15px;
  font-size: 18px;
  cursor: pointer;
  transition: all 0.3s;

  z-index: 1000; 
}
</style>
