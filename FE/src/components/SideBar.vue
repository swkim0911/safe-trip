<template>
  <div class="sidebar-container">
    <!-- 사이드바 -->
    <div :class="['sidebar', {'open': isOpen}]">
      <div class="sidebar-header">
        <h5>사이드바 헤더</h5>
        <button @click="toggleSidebar" class="close-btn">✖
        </button>
      </div>
      <div class="sidebar-body">
        <p>content</p>
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
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  margin-bottom: 10px;
}

/* 닫기 버튼 */
.close-btn {
  background: none;
  border: none;
  color: white;
  font-size: 20px;
  cursor: pointer;
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
}

/* 사이드바가 열렸을 때 토글 버튼 위치 조정 */
.sidebar.open + .toggle-btn {
  left: 500px;
}
</style>
