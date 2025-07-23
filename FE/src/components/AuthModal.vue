<template>
  <div class="modal fade" id = "authModal" data-bs-backdrop="static" data-bs-keyboard="false" tabindex="-1" aria-labelledby="staticBackdropLabel" aria-hidden="true">
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
                <label for="email" class="form-label fw-bold">Email</label>
                <input
                  type="email"
                  id="email"
                  class="form-control"
                  v-model="loginForm.email"
                  placeholder="email@example.com"
                  required
                />
                </div>

                <div class="mb-3">
                  <label for="password" class="form-label fw-bold">Password</label>
                  <input
                    type="password"
                    id="password"
                    class="form-control"
                    v-model="loginForm.password"
                    placeholder="Enter your password"
                    required
                  />
                </div>
                <div class="mb-2">
                  <button type="submit" class="btn btn-primary w-100 py-2 mt-2" @click="submitLoginForm">Log In</button>
                </div>
              </form>
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
                  v-model="signupForm.email"
                  placeholder="email@example.com"
                  required
                />
              </div>

              <div class="mb-3">
                <label for="password" class="form-label fw-bold">Password</label>
                <input
                  type="password"
                  id="password"
                  class="form-control"
                  v-model="signupForm.password"
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
                  v-model="signupForm.nickname"
                  required
                />
              </div>
              <button type="submit" class="btn btn-primary w-100 py-2" @click="submitSignupForm">Sign Up</button>
            </form>
            <p class="text-center text-success fw-bold" v-if="signupSuccessMessage">{{ signupSuccessMessage }}</p>
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

import { ref, reactive, watch, onMounted } from 'vue'
import axios from 'axios'

const mode = ref('login')
const serverURL = import.meta.env.VITE_API_URL;
const signupSuccessMessage = ref('')

const loginForm = reactive({
  email: '',
  password: ''
})

const signupForm = reactive({
  email: '',
  password: '',
  nickname: ''
})

const resetSignupForm = () => {
  signupForm.email = ''
  signupForm.password = ''
  signupForm.nickname = ''
}

const resetLoginForm = () => {
  loginForm.email = '';
  loginForm.password = '';
}

function resetForm() {
  mode.value = 'login';
  resetSignupForm();
  resetLoginForm();
  signupSuccessMessage.value = '';
}


const submitLoginForm = async () => {
  try {
    const response = await axios.post(`${serverURL}/auth/login`, {
      email: loginForm.email,
      password: loginForm.password,
    })
    console.log('로그인 성공:', response.data)
  } catch (error) {
    console.error('로그인 실패:', error.response?.data || error.message)
  }
}

const submitSignupForm = async () => {
  
  try {
    const response = await axios.post(`${serverURL}/users`, {
      email: signupForm.email,
      password: signupForm.password,
      nickname: signupForm.nickname
    })

    resetSignupForm();
    signupSuccessMessage.value = '회원가입이 완료되었습니다. 로그인 해주세요.';
    console.log('회원가입 성공:', response.data)
  } catch (error) {
    console.error('회원가입 실패:', error.response?.data || error.message)
  }
}

const setupModalEventListener = () => {
  const modal = document.getElementById('authModal');

  if (modal) {
    modal.addEventListener('hide.bs.modal', () => {
      document.activeElement.blur(); 
    });

    modal.addEventListener('hidden.bs.modal', () => {
      resetForm(); // 모달이 닫힐 때 form에 입력된 값들 모두 지움.
    });
  }
}

onMounted(() => {
  setupModalEventListener()
})

watch(mode, (newMode) => {
  if (newMode === 'login') {
    // 회원가입 폼 초기화
    resetSignupForm();
    signupSuccessMessage.value = '';
  }
});



</script>
<style scoped lang="scss">

</style>