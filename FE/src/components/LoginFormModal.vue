<template>
  <div class="modal fade" id = "LoginFormModal" data-bs-backdrop="static" data-bs-keyboard="false" tabindex="-1" aria-labelledby="staticBackdropLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered modal-dialog-scrollable modal-md">
      <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title w-100 text-center" id="staticBackdropLabel">Log In</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>

          <div class="modal-body">
            <form @submit.prevent>
              <div class="mb-3">
                <label for="email" class="form-label">Email address</label>
                <input type="email" id="email" v-model="loginForm.email" class="form-control" required placeholder="email"/>
              </div>

              <div class="mb-3">
                <label for="password" class="form-label">Password</label>
                <input type="password" id="password" v-model="loginForm.password" class="form-control" required  placeholder="password"/>
              </div>
            </form>
            <div class="d-grid mb-2">
              <button type="button" class="btn btn-primary">Log In</button>
            </div>
            <div class="text-center">
              <button type="button" class="btn btn-link">Sign Up</button>
            </div>
          </div>
        </div>  
    </div>
  </div>
</template>
<script setup>

import { ref, reactive } from 'vue'

const loginForm = reactive({
  email: '',
  password: ''
})


</script>
<style scoped lang="scss">
  .modal-dialog.modal-fullsize {
  width: 50%;
  height: 50%;
  margin: 0;
  padding: 0;
}
</style>