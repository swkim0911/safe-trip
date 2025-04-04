<template>
  <div class="mapview-container">
    <button class="btn btn-primary shadow-sm login-btn">
      <font-awesome-icon :icon="['fas', 'user-large']" class="login-icon" />
      LOGIN
    </button>
  </div>
</template>
<script setup>
</script>
<style scoped lang="scss">
  .login-icon {
    font-size: 95%;
    margin-right: 5px;
  }
  .login-btn {
    position: fixed;
    top: 20px;
    right: 20px;
    z-index: 1000; /* 다른 요소보다 위에 뜨도록 */
    border: none;
    padding: 10px 14px;
    border-radius: 10px;
    font-size: 18px;
  }
</style>