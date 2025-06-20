<template>
  <div class="modal fade" id = "AuthModal" data-bs-backdrop="static" data-bs-keyboard="false" tabindex="-1" aria-labelledby="staticBackdropLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered modal-dialog-scrollable modal-md">
      <div class="modal-content">
          <div class="modal-header border-bottom-0">
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <div v-if="mode === 'login'">
              <form @submit.prevent class="px-3 py-3" style="max-width: 400px; margin: 0 auto;">
              <h3 class="text-center mb-3 fw-bold">Welcome</h3>

                <div class="mb-3">
                  <input type="email" v-model="loginForm.email" class="form-control" placeholder="example@gmail.com" required />
                </div>
                <div class="mb-4">
                  <input type="password" v-model="loginForm.password" class="form-control" placeholder="Enter your Password" required />
                </div>
              </form>
              <div class="d-grid mb-2">
                <button type="button" class="btn btn-primary">Log In</button>
              </div>
              <div class="text-center">
                <button type="button" class="btn btn-link" @click="mode = 'signup'">Sign Up</button>
              </div>
            </div>
            <div v-else>
              <form @submit.prevent class="px-3 py-3" style="max-width: 400px; margin: 0 auto;">
              <h3 class="text-center mb-3 fw-bold">Create Your Account</h3>

              <div class="mb-3">
                <label for="email" class="form-label fw-bold">Email</label>
                <input
                  type="email"
                  id="email"
                  class="form-control"
                  v-model="signUpForm.email"
                  placeholder="example@gmail.com"
                  required
                />
              </div>

              <div class="mb-3">
                <label for="password" class="form-label fw-bold">Password</label>
                <input
                  type="password"
                  id="password"
                  class="form-control"
                  v-model="signUpForm.password"
                  placeholder="Enter your password"
                  required
                />
              </div>

              <div class="mb-4">
                <label for="nickname" class="form-label fw-bold">Nickname</label>
                <input
                  type="text"
                  id="nickname"
                  class="form-control"
                  placeholder="Enter your nickname"
                  v-model="signUpForm.nickname"
                  required
                />
              </div>
              <button type="submit" class="btn btn-primary w-100 py-2" @click="submitSignUpForm">Sign Up</button>
            </form>
            <p class="text-center text-success fw-bold" v-if="successMessage">{{ successMessage }}</p>
            <p class="text-center mt-3 mb-0">
              Already have an account?
              <a href="#" class="text-decoration-none" role="button" @click.prevent="mode = 'login'">Log In</a>
            </p>
              
            </div>
          </div>
        </div>  
    </div>
  </div>
</template>
<script setup>

import { ref, reactive } from 'vue'
import axios from 'axios'

const mode = ref('login')
const serverURL = import.meta.env.VITE_API_URL;
const successMessage = ref('')

const loginForm = reactive({
  email: '',
  password: ''
})

const signUpForm = reactive({
  email: '',
  password: '',
  nickname: ''
})



const resetSignUpForm = () => {
  signUpForm.email = ''
  signUpForm.password = ''
  signUpForm.nickname = ''
}

const submitSignUpForm = async () => {
  
  try {
    const response = await axios.post(`${serverURL}/users`, {
      email: signUpForm.email,
      password: signUpForm.password,
      nickname: signUpForm.nickname
    })

    resetSignUpForm()
    successMessage.value = '회원가입이 완료되었습니다. 로그인 해주세요.';
    console.log('회원가입 성공:', response.data)
  } catch (error) {
    console.error('회원가입 실패:', error.response?.data || error.message)
  }
}

</script>
<style scoped lang="scss">

</style>