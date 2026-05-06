<template>
  <div class="modal fade" ref="modalRef" id="authFormModal" data-bs-backdrop="static" data-bs-keyboard="false" tabindex="-1" aria-labelledby="staticBackdropLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered modal-dialog-scrollable modal-md">
      <div class="modal-content">
          <div class="modal-header border-bottom-0">
            <button type="button" class="btn-close" @click="hide" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <div class="auth-tabs">
              <button
                class="auth-tab"
                :class="{ active: mode === 'login' }"
                @click="mode = 'login'"
              >Log In</button>
              <button
                class="auth-tab"
                :class="{ active: mode === 'signup' }"
                @click="mode = 'signup'"
              >Sign Up</button>
            </div>

            <Transition name="fade" mode="out-in">
            <div v-if="mode === 'login'" key="login">
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
                  <div class="input-group">
                    <input
                      :type="showLoginPassword ? 'text' : 'password'"
                      type="password"
                      id="password"
                      class="form-control"
                      v-model="loginForm.password"
                      placeholder="Enter your password"
                      required
                    />
                    <button type="button" class="btn btn-outline-secondary" @click="toggleLoginPasswordVisibility">
                      <i :class="showLoginPassword ? 'bi bi-eye-slash' : 'bi bi-eye'"></i>
                    </button>
                  </div>
                </div>
                <p v-if="loginFormMessage" class="text-center text-danger fw-bold">
                      {{ loginFormMessage }}
                </p>
                
                <div class="mb-2">
                  <button type="submit" class="btn btn-primary auth-submit-btn w-100 py-2 mt-2" @click="submitLoginForm">Log In</button>
                </div>
              </form>
            </div>

            <div v-else key="signup">
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
                    <button :disabled="!isValidEmail" type="button" class="btn btn-outline-secondary auth-check-btn" @click="validateEmail">
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
                      :type="showSignupPassword ? 'text' : 'password'"
                      id="password"
                      class="form-control"
                      v-model="signupForm.password"
                      placeholder="Enter your password"
                      required
                    />
                    <button type="button" class="btn btn-outline-secondary" @click="toggleSignupPasswordVisibility">
                      <i :class="showSignupPassword ? 'bi bi-eye-slash' : 'bi bi-eye'"></i>
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
                    <button :disabled="!isValidNickname" type="button" class="btn btn-outline-secondary auth-check-btn" @click="validateNickname">
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
                <button :disabled="!isSignupFormValid() || isSignupSubmitting" type="submit" class="btn btn-primary auth-submit-btn w-100 py-2" @click="submitSignupForm">Sign Up</button>
              </form>
              <p class="text-center text-danger fw-bold" v-if="signupFailureMessage">{{ signupFailureMessage }}</p>
            </div>
            </Transition>
          </div>
        </div>  
    </div>
  </div>
</template>
<script setup>
import { useAuthStore } from '@/stores/auth';

import { ref, reactive, watch, onMounted, computed } from 'vue'
import { useBootstrapModal } from '@/composables/useBootstrapModal';
import { useToast } from '@/composables/useToast';
import apiClient from '@/api/apiClient';

const authStore = useAuthStore();

const modalRef = ref(null);
const { hide } = useBootstrapModal(modalRef);
const toast = useToast();

const mode = ref('login')
const signupFailureMessage = ref('');
const loginFormMessage = ref('');
const showLoginPassword = ref(false);
const showSignupPassword = ref(false);

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

const toggleLoginPasswordVisibility = () => {
  showLoginPassword.value = !showLoginPassword.value;
}

const toggleSignupPasswordVisibility = () => {
  showSignupPassword.value = !showSignupPassword.value;
}

const isValidLength = computed(() => signupForm.password.length >= 8 && signupForm.password.length <= 20);
const hasLetter = computed(() => /[A-Za-z]/.test(signupForm.password));
const hasNumber = computed(() => /\d/.test(signupForm.password));
const hasSpecial = computed(() => /[!@#$%^&*()_+=-]/.test(signupForm.password));

const emailPattern = /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,6}$/;
const nicknamePattern = null;

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

const isValidNickname = computed(() => isValidNicknameLength.value);

const nicknameValidationMessage = computed(() => {
  if (!signupForm.nickname) return '';
  if (!isValidNicknameLength.value) return 'Nickname must be between 2 and 15 characters.';
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
    const response = await apiClient.get('/users/validate-email', {
      params: { email: signupForm.email }
    });
    emailValidationErrorMessage.value = '';
    const result = response.data.result;
    isEmailAvailable.value = result.isAvailable;

  } catch (error) {
    console.error(error);
    isEmailAvailable.value = null; 

    if (error.response) {
      const status = error.response.status;

      if (status === 404) {
        emailValidationErrorMessage.value = 'Validation endpoint not found.';
      } else if (status === 500) {
        emailValidationErrorMessage.value = 'Server error. Please try again later.';
      } else {
        emailValidationErrorMessage.value = `Unexpected error (code ${status}).`;
      }

    } else if (error.request) {
      emailValidationErrorMessage.value = 'No response from server. Please check your network connection.';
    } else {
      emailValidationErrorMessage.value = 'An unexpected error occurred.';
    }
  }
};

const validateNickname = async () => {
  try {
    const response = await apiClient.get('/users/validate-nickname', {
      params: { nickname: signupForm.nickname }
    });
    nicknameValidationErrorMessage.value = '';
    const result = response.data.result;
    isNicknameAvailable.value = result.isAvailable;

  } catch (error) {
    console.error(error);

    isNicknameAvailable.value = null; 
    if (error.response) {
      const status = error.response.status;

      if (status === 404) {
        nicknameValidationErrorMessage.value = 'Validation endpoint not found.';
      } else if (status === 500) {
        nicknameValidationErrorMessage.value = 'Server error. Please try again later.';
      } else {
        nicknameValidationErrorMessage.value = `Unexpected error (code ${status}).`;
      }

    } else if (error.request) {
      nicknameValidationErrorMessage.value = 'No response from server. Please check your network connection.';
    } else {
      nicknameValidationErrorMessage.value = 'An unexpected error occurred.';
    }
  }
}

const isSignupSubmitting = ref(false);

const submitSignupForm = async () => {
  if (!isSignupFormValid() || isSignupSubmitting.value) return;
  isSignupSubmitting.value = true;
  try {
    const response = await apiClient.post('/users', {
      email: signupForm.email,
      password: signupForm.password,
      nickname: signupForm.nickname
    })
    mode.value = 'login';
    toast.show('Sign-up complete! Please log in.');
  } catch (error) {
    console.error(error);

    if (error.response) {
      const status = error.response.status;
      if (status === 400) {
        signupFailureMessage.value = 'Sign-up failed due to an already existing email or nickname.';
      } else if (status === 404){
        signupFailureMessage.value = 'Validation endpoint not found.';
      } else if (status === 500) {
        signupFailureMessage.value = 'Server error. Please try again later.';
      } else {
        signupFailureMessage.value = `Unexpected error (code ${status}).`;
      }
    } else if (error.request) {
      signupFailureMessage.value = 'No response from server. Please check your network connection.';
    } else {
      signupFailureMessage.value = 'An unexpected error occurred.';
    }
  } finally {
    isSignupSubmitting.value = false;
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

const isLoginSubmitting = ref(false);

const submitLoginForm = async () => {
  if (!validateLoginForm() || isLoginSubmitting.value) return;
  isLoginSubmitting.value = true;

  try {
    const { data: signupResponse } = await apiClient.post('/auth/login', {
      email: loginForm.email,
      password: loginForm.password,
    }, { withCredentials: true })

    const accessToken = signupResponse.result.accessToken;
    authStore.setAccessToken(accessToken);

    const { data: userInfoResponse } = await apiClient.get('/users/me');
    authStore.setUser(userInfoResponse.result);
      hide();
  } catch (error) {
    console.error(error);
    if (error.response) {
      const status = error.response.status;
      if (status === 400) {
        loginFormMessage.value = 'Please check your input.';
      } else if (status === 401) {
        loginFormMessage.value = 'Invalid email or password.';
      }
    } else if (error.request) {
      loginFormMessage.value = 'No response from server. Please check your network connection.';
    } else {
      loginFormMessage.value = 'An unexpected error occurred.';
    }
  } finally {
    isLoginSubmitting.value = false;
  }
}

const setupModalEventListener = () => {
  const modal = document.getElementById('authFormModal');

  if (modal) {
    modal.addEventListener('hide.bs.modal', () => {
      document.activeElement.blur(); 
    });

    modal.addEventListener('hidden.bs.modal', () => {
      resetForm();
    });
  }
}

onMounted(() => {
  setupModalEventListener();
})

watch(mode, (newMode) => {
  if (newMode === 'login') {
    resetSignupForm();
    signupFailureMessage.value = '';
  } else if (newMode == 'signup') {
    resetLoginForm();
    loginFormMessage.value = '';
    }
});

// re-validate when signup email changes
watch(() => signupForm.email, () => {
  isEmailAvailable.value = null;
});

// re-validate when signup nickname changes
watch(() => signupForm.nickname, () => {
  isNicknameAvailable.value = null;
});


</script>
<style scoped lang="scss">
.auth-tabs {
  display: flex;
  border-bottom: 2px solid #e9ecef;
  margin-bottom: 8px;
}

.auth-tab {
  flex: 1;
  background: none;
  border: none;
  padding: 12px 0;
  font-size: 15px;
  font-weight: 500;
  color: #adb5bd;
  cursor: pointer;
  position: relative;
  transition: color 0.2s;

  &.active {
    color: var(--safetrip-primary);

    &::after {
      content: '';
      position: absolute;
      bottom: -2px;
      left: 0;
      right: 0;
      height: 2px;
      background: var(--safetrip-primary);
      border-radius: 2px 2px 0 0;
    }
  }

  &:hover:not(.active) {
    color: #6c757d;
  }
}

.auth-submit-btn {
  background: var(--safetrip-primary);
  border-color: var(--safetrip-primary);

  &:hover,
  &:focus {
    background: var(--safetrip-primary-hover);
    border-color: var(--safetrip-primary-hover);
  }

  &:active {
    background: var(--safetrip-primary-active) !important;
    border-color: var(--safetrip-primary-active) !important;
  }

  &:disabled {
    background: #a7cfc8;
    border-color: #a7cfc8;
    opacity: 1;
  }
}

.auth-check-btn {
  color: var(--safetrip-primary);
  border-color: #b9d8d2;

  &:hover,
  &:focus {
    color: #fff;
    background: var(--safetrip-primary);
    border-color: var(--safetrip-primary);
  }

  &:disabled {
    color: #9aa8a6;
    background: #f7f5ef;
    border-color: var(--safetrip-border);
  }
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.15s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
