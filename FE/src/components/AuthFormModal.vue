<template>
  <div class="modal fade" ref="modalRef" id="authFormModal" data-bs-backdrop="static" data-bs-keyboard="false" tabindex="-1" aria-labelledby="staticBackdropLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered modal-dialog-scrollable modal-md">
      <div class="modal-content">
          <div class="modal-header border-bottom-0">
            <button type="button" class="btn-close" @click="hide" aria-label="Close"></button>
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
                  placeholder="user@example.com"
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
                <p v-if="loginFormMessage" class="text-center text-danger fw-bold">
                      {{ loginFormMessage }}
                </p>
                
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
                  <div class="input-group">
                    <input
                      type="email"
                      id="email"
                      :class="emailInputClass"
                      v-model="signupForm.email"
                      placeholder="user@example.com"
                      required
                    />
                    <button :disabled="!isValidEmail" type="button" class="btn btn-outline-secondary" @click="validateEmail">
                      Check
                    </button>
                  </div>
                  <div class="mt-1">
                    <p v-if="emailValidationMessage" :class="['small', emailValidationTextClass]">
                      {{ emailValidationMessage }}
                    </p>
                    <p v-if="emailValidationErrorMessage" class="text-danger small">
                      {{ emailValidationErrorMessage }}
                    </p>
                  </div>
                </div>

                <div class="mb-3">
                  <label for="password" class="form-label fw-bold">Password</label>
                  <div class="input-group">
                    <input
                      :type="showPassword ? 'text' : 'password'"
                      id="password"
                      class="form-control"
                      v-model="signupForm.password"
                      placeholder="Enter your password"
                      required
                    />
                    <button type="button" class="btn btn-outline-secondary" @click="togglePasswordVisibility">
                      <i :class="showPassword ? 'bi bi-eye-slash' : 'bi bi-eye'"></i>
                    </button>
                  </div>
                  <div v-if="signupForm.password">
                    <ul class="mt-2 small text-muted">
                      <li :class="isValidLength ? 'text-success' : 'text-danger'">
                        Must be between 8 and 20 characters
                      </li>
                      <li :class="hasLetter ? 'text-success' : 'text-danger'">
                        Must include at least one letter
                      </li>
                      <li :class="hasNumber ? 'text-success' : 'text-danger'">
                        Must include at least one number
                      </li>
                      <li :class="hasSpecial ? 'text-success' : 'text-danger'">
                        Must include at least one special character (!@#$%^&*()_+=-)
                      </li>
                    </ul>
                  </div>
                </div>
                <div class="mb-3">
                  <label for="nickname" class="form-label fw-bold">Nickname</label>
                  <div class="input-group">
                    <input
                      type="text"
                      id="nickname"
                      :class="nicknameInputClass"
                      v-model="signupForm.nickname"
                      placeholder="Enter your nickname"
                      required
                    />
                    <button :disabled="!isValidNickname" type="button" class="btn btn-outline-secondary" @click="validateNickname">
                      Check 
                    </button>
                  </div>
                  <div class="mt-1">
                    <p v-if="nicknameValidationMessage" :class="['small', nicknameValidationTextClass]">
                        {{ nicknameValidationMessage }}
                    </p>
                    <p v-if="nicknameValidationErrorMessage" class="text-danger small">
                        {{ nicknameValidationErrorMessage }}
                    </p>
                  </div>
                
                </div>
                <button :disabled="!isSignupFormValid()" type="submit" class="btn btn-primary w-100 py-2" @click="submitSignupForm">Sign Up</button>
              </form>
              <p class="text-center text-success fw-bold" v-if="signupSuccessMessage">{{ signupSuccessMessage }}</p>
              <p class="text-center text-danger fw-bold" v-if="signupFailureMessage">{{ signupFailureMessage }}</p>
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
import { ref, reactive, watch, onMounted, computed } from 'vue'
import { useAuthStore } from '@/stores/auth';
import apiClient from '@/lib/apiClient';
import { useBootstrapModal } from '@/composables/useBootstrapModal';

const authStore = useAuthStore();

const modalRef = ref(null);
const { hide } = useBootstrapModal(modalRef);

const mode = ref('login')
const serverURL = import.meta.env.VITE_API_URL;
const signupSuccessMessage = ref('');
const signupFailureMessage = ref('');
const loginFormMessage = ref('');
const showPassword = ref(false);

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
  signupFailureMessage.value = '';
  loginFormMessage.value = '';
}

const isEmailAvailable = ref(null);
const isNicknameAvailable = ref(null);
const isValidPassword = computed(() =>
  isValidLength.value &&
  hasLetter.value &&
  hasNumber.value &&
  hasSpecial.value
);

const isSignupFormValid = () => {
  return isEmailAvailable.value && isNicknameAvailable.value && isValidPassword.value;
}

const emailValidationErrorMessage = ref('');
const nicknameValidationErrorMessage = ref('');

const togglePasswordVisibility = () => {
  showPassword.value = !showPassword.value;
}

const isValidLength = computed(() => signupForm.password.length >= 8 && signupForm.password.length <= 20);
const hasLetter = computed(() => /[A-Za-z]/.test(signupForm.password));
const hasNumber = computed(() => /\d/.test(signupForm.password));
const hasSpecial = computed(() => /[!@#$%^&*()_+=-]/.test(signupForm.password));

const emailPattern = /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,6}$/;
const nicknamePattern = /^[a-zA-Z0-9가-힣_-]+$/;

const isValidEmail = computed(() =>
  emailPattern.test(signupForm.email)
);

const emailValidationMessage = computed(() => {
  if (!signupForm.email) return '';
  if (!isValidEmail.value) return 'Invalid email format. (e.g., user@example.com)';
  if (isEmailAvailable.value === false) return 'Email is already in use.';
  if (isEmailAvailable.value === true) return 'Email is available.';
  return '';
});

const emailValidationTextClass = computed(() => {
  if (!signupForm.email || !isValidEmail.value) return 'text-danger';
  return isEmailAvailable.value ? 'text-success' : 'text-danger';
});

const getValidationInputClass = (status) => {
  if (status === true) return 'form-control border border-2 border-primary';
  if (status === false) return 'form-control border border-2 border-danger';
  return 'form-control';
}

const emailInputClass = computed(() =>
  getValidationInputClass(isEmailAvailable.value)
);

const nicknameInputClass = computed(() =>
  getValidationInputClass(isNicknameAvailable.value)
);

const isValidNicknameLength = computed(() => {
  const length = signupForm.nickname.length;
  return length >= 2 && length <= 15;
});

const isValidNicknamePattern = computed(() =>
  nicknamePattern.test(signupForm.nickname)
);

const isValidNickname = computed(() =>
  isValidNicknameLength.value && isValidNicknamePattern.value
);

const nicknameValidationMessage = computed(() => {
  if (!signupForm.nickname) return '';
  if (!isValidNicknameLength.value) return 'Nickname must be between 2 and 15 characters.';
  if (!isValidNicknamePattern.value) return 'Only Korean, English letters, digits, underscores (_), and hyphens (-) are allowed in nickname.';
  if (isNicknameAvailable.value === false) return 'Nickname is already in use.';
  if (isNicknameAvailable.value === true) return 'Nickname is available.';
  return '';
});

const nicknameValidationTextClass = computed(() => {
  if (!signupForm.nickname || !isValidNickname.value) return 'text-danger';
  return isNicknameAvailable.value ? 'text-success' : 'text-danger';
});



const validateEmail = async () => {
  try {
    const response = await apiClient.get(`${serverURL}/users/validate-email`, {
      params: { email: signupForm.email }
    });
    emailValidationErrorMessage.value = '';
    const result = response.data.result;
    isEmailAvailable.value = result.available;

  } catch (error) {
    isEmailAvailable.value = null; 
    emailValidationErrorMessage.value = 'There was a problem checking your email. Please try again.'
  }
};

const validateNickname = async () => {
  try {
    const response = await apiClient.get(`${serverURL}/users/validate-nickname`, {
      params: { nickname: signupForm.nickname }
    });
    nicknameValidationErrorMessage.value = '';
    const result = response.data.result;
    isNicknameAvailable.value = result.available;

  } catch (error) {
    isNicknameAvailable.value = null; 
    nicknameValidationErrorMessage.value = 'There was a problem checking your nickname. Please try again.'
  }
}

const submitSignupForm = async () => {
  if (!isSignupFormValid()) return;
  try {
    const response = await apiClient.post(`${serverURL}/users`, {
      email: signupForm.email,
      password: signupForm.password,
      nickname: signupForm.nickname
    })
    signupSuccessMessage.value = 'Sign-up completed successfully. Please log in.';
  } catch (error) {
    const status = error.response?.status;
    if (status === 400) {
      signupFailureMessage.value = 'Sign-up failed due to an already existing email or nickname.';
    } else {
      signupFailureMessage.value = 'Server error. Please try again later.';
    }
  }
  resetSignupForm();
}

const validateLoginForm = () => {
  if (!loginForm.email) {
    loginFormMessage.value = "Please enter your email.";
    return false;
  }
  if (!loginForm.password) {
    loginFormMessage.value = "Please enter your password.";
    return false;
  }
  loginFormMessage.value = '';
  return true;
}



const submitLoginForm = async () => {
  if (!validateLoginForm()) return;
  try {
    const response = await apiClient.post(`${serverURL}/auth/login`, {
      email: loginForm.email,
      password: loginForm.password,
    }, { withCredentials: true })

    const accessToken = response.data.result.accessToken;
    authStore.setAccessToken(accessToken);
    hide();
  } catch (error) {
    const status = error.response?.status;
    if (status === 401 || status === 400) {
      loginFormMessage.value = 'Login failed';
    } else {
      signupFailureMessage.value = 'Server error. Please try again later.';
    }
  }
}

const setupModalEventListener = () => {
  const modal = document.getElementById('authFormModal');

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
  setupModalEventListener();
})

watch(mode, (newMode) => {
  if (newMode === 'login') {
    resetSignupForm();
    signupSuccessMessage.value = '';
    signupFailureMessage.value = '';
  } else if (newMode == 'signup') {
    resetLoginForm();
    loginFormMessage.value = '';
  }
});

// 회원가입란에 email이 변경되면 다시 검증이 필요하다고 판단
watch(() => signupForm.email, () => {
  isEmailAvailable.value = null;
});

// 회원가입란에 nickname이 변경되면 다시 검증이 필요하다고 판단
watch(() => signupForm.nickname, () => {
  isNicknameAvailable.value = null;
});


</script>
<style scoped lang="scss">

</style>