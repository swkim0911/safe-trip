<template>
  <div class="modal fade" id = "LoginFormModal" data-bs-backdrop="static" data-bs-keyboard="false" tabindex="-1" aria-labelledby="staticBackdropLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered modal-dialog-scrollable modal-md">
      <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title w-100 text-center" id="staticBackdropLabel">Log In</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>

          <div class="modal-body">
            <form @submit.prevent class="px-3 py-3">
              <div class="mb-3">
                <input type="email" v-model="loginForm.email" class="form-control" placeholder="Email" required />
              </div>
              <div class="mb-4">
                <input type="password" v-model="loginForm.password" class="form-control" placeholder="Password" required />
              </div>
            </form>
            <div class="d-grid mb-2">
              <button type="button" class="btn btn-primary">Log In</button>
            </div>
            <div class="text-center">
              <button type="button" class="btn btn-link"  @click="$emit('switch-to-signup')" >Sign Up</button>
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

</style>