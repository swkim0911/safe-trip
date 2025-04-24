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
        <ul>
          <li class="list-item"> 
            <div class="country-name">그리스</div>
            <faceTear class="icon" style= "height: 20px; width: 20px;"/>
            <div class="scam-count">10</div>
          </li>
          <li> item</li>
          <li> item</li>
          <li> item</li>
          <li> item</li>
          <li> item</li>
          <li> item</li>
          <li> item</li>
          <li> item</li>
          <li> item</li>
          <li> item</li>
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
import faceTear from "../assets/face-sad-tear-regular.svg";

const isOpen = ref(false);


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
  color: white;
  background-color: #343a40;
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
  padding: 16px;
  overflow-y: auto;
  overscroll-behavior: contain; /* 스크롤 범위를 벗어나면 움직이지 않게 */
}

/* 사이드바가 열렸을 때 토글 버튼 위치 조정 */
.sidebar.open + .toggle-btn {
  left: 400px;
}

.sidebar-body ul {
  list-style: none; /* 기본 리스트 스타일 제거 */
  padding: 0;
}

.sidebar-body li {
  background-color: #f8f9fa; /* 배경색 */
  color: black;
  border-radius: 10px; /* 모서리를 둥글게 */
  padding: 12px; /* 내부 여백 */
  margin-bottom: 8px; /* 아래 여백 */
  border: 1px solid #ddd; /* 테두리 */
  box-shadow: 2px 2px 5px rgba(0, 0, 0, 0.1); /* 그림자 효과 */
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

.country-name {
  font-size: 16px;
  font-weight: bold;
  margin-right: 12px; /* 숫자와 텍스트 사이 여백 */
  color: #333; /* 글자색 */
}

/* 숫자 스타일 */
.number {
  font-size: 14px;
  color: #007bff; /* 숫자 색상 */
}

.list-item {
  display: flex;
  align-items: center; /* 세로 중앙 정렬 */
  width: 100%; /* 전체 너비 */
}

.country-name {
  flex: 0 0 70%; /* 70% 너비 */
  padding-right: 10px; /* 이름과 아이콘 간의 간격 */
  align-items: center;        // 수직 가운데
  justify-content: center;    // 수평 가운데
  text-align: center;
}

.icon {
  flex: 0 0 15%; /* 15% 너비 */
  display: flex;
  justify-content: center; /* 아이콘 중앙 정렬 */
}

.scam-count {
  flex: 0 0 15%; /* 15% 너비 */
  text-align: center; /* 숫자 중앙 정렬 */
}
</style>
