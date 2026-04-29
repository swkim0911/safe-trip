<template>
  <!-- 패널 본체 -->
  <div v-if="isExpanded" class="stats-panel">
    <div class="panel-body">
      <div class="stats-section">
        <div class="section-title">Scam Action</div>
        <div class="rings-row">
          <div v-for="item in actionItems" :key="item.name" class="ring-item">
            <svg width="60" height="60" viewBox="0 0 60 60">
              <circle cx="30" cy="30" r="24" fill="none" stroke="#e9ecef" stroke-width="5"/>
              <circle
                cx="30" cy="30" r="24"
                fill="none" stroke="#D97757" stroke-width="5"
                stroke-linecap="round"
                :stroke-dasharray="circumference"
                :stroke-dashoffset="getDashOffset(item.percentage)"
                transform="rotate(-90 30 30)"
              />
              <text x="30" y="35" text-anchor="middle" font-size="11" font-weight="600" fill="#333">
                {{ item.percentage }}%
              </text>
            </svg>
            <div class="ring-label">{{ item.name }}</div>
          </div>
        </div>
      </div>

      <div class="section-divider" />

      <div class="stats-section">
        <div class="section-title">Scam Context</div>
        <div class="rings-row">
          <div v-for="item in contextItems" :key="item.name" class="ring-item">
            <svg width="60" height="60" viewBox="0 0 60 60">
              <circle cx="30" cy="30" r="24" fill="none" stroke="#e9ecef" stroke-width="5"/>
              <circle
                cx="30" cy="30" r="24"
                fill="none" stroke="#2A9D8F" stroke-width="5"
                stroke-linecap="round"
                :stroke-dasharray="circumference"
                :stroke-dashoffset="getDashOffset(item.percentage)"
                transform="rotate(-90 30 30)"
              />
              <text x="30" y="35" text-anchor="middle" font-size="11" font-weight="600" fill="#333">
                {{ item.percentage }}%
              </text>
            </svg>
            <div class="ring-label">{{ item.name }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- 토글 버튼 -->
  <button class="toggle-btn" @click="isExpanded = !isExpanded">
    <font-awesome-icon :icon="['fas', isExpanded ? 'chevron-up' : 'chart-pie']" class="me-1" />
    Travel Insights
  </button>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import apiClient from '@/api/apiClient';

const isExpanded = ref(false);
const actionStats = ref([]);
const contextStats = ref([]);

const circumference = 2 * Math.PI * 24;

const toPercentageItems = (stats) => {
  const total = stats.reduce((sum, item) => sum + item.count, 0);
  if (total === 0) return [];
  return stats.map(item => ({
    name: item.name,
    percentage: Math.round(item.count / total * 100),
  }));
};

const actionItems = computed(() => toPercentageItems(actionStats.value));
const contextItems = computed(() => toPercentageItems(contextStats.value));

const getDashOffset = (percentage) => circumference * (1 - percentage / 100);

onMounted(async () => {
  const [actionRes, contextRes] = await Promise.all([
    apiClient.get('/scam-actions/statistics'),
    apiClient.get('/scam-contexts/statistics'),
  ]);
  actionStats.value = actionRes.data.result;
  contextStats.value = contextRes.data.result;
});
</script>

<style scoped lang="scss">
.stats-panel {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  z-index: 998;
  background: rgba(255, 255, 255, 0.96);
  border-bottom: 1px solid var(--safetrip-border);
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.08);
}

.panel-body {
  display: flex;
  align-items: flex-start;
  justify-content: center;
  gap: 32px;
  padding: 16px 24px 20px;
  overflow-x: auto;
}

.stats-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.section-title {
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  color: #888;
  letter-spacing: 0.05em;
}

.rings-row {
  display: flex;
  gap: 16px;
  flex-wrap: nowrap;
}

.ring-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  width: 72px;
}

.ring-label {
  font-size: 11px;
  color: #555;
  text-align: center;
  line-height: 1.3;
  word-break: break-word;
}

.section-divider {
  width: 1px;
  background: #e9ecef;
  align-self: stretch;
  flex-shrink: 0;
  margin-top: 24px;
}

.toggle-btn {
  position: fixed;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  z-index: 999;
  background: var(--safetrip-primary);
  color: white;
  border: none;
  padding: 8px 16px;
  font-size: 14px;
  cursor: pointer;
  border-radius: 0 0 10px 10px;
  box-shadow: 0 3px 12px rgba(42, 157, 143, 0.26);
  transition: background 0.2s ease, box-shadow 0.2s ease;

  &:hover {
    background: var(--safetrip-primary-hover);
    box-shadow: 0 4px 18px rgba(42, 157, 143, 0.34);
  }
}
</style>
