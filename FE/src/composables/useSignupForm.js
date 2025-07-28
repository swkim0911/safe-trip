
import { reactive, computed, ref, watch } from 'vue';
import apiClient from '@/api/apiClient';

const EMAIL_REGEX = /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,6}$/;
const NICKNAME_REGEX = /^[a-zA-Z0-9가-힣_-]+$/;

export function useSignupForm() {
  const signupForm = reactive({
    email: '',
    password: '',
    nickname: ''
  });

  const resetSignupForm = () => {
    signupForm.email = '';
    signupForm.password = '';
    signupForm.nickname = '';
  }

  const signupSuccessMessage = ref('');
  const signupFailureMessage = ref('');

  const getValidationInputClass = (status) => {
    if (status === true) return 'form-control border border-2 border-primary';
    if (status === false) return 'form-control border border-2 border-danger';
    return 'form-control';
  }
  // email
  const isEmailAvailable = ref(null); // 이메일 중복 여부 (사용가능 여부)
  const emailValidationErrorMessage = ref(''); // 이메일 검증시 발생한 에러 메시지

  const isValidEmail = computed(() => EMAIL_REGEX.test(signupForm.email)); // 형식에 맞는지

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

  const emailInputClass = computed(() =>
    getValidationInputClass(isEmailAvailable.value)
  );

  // password
  const showPassword = ref(false);

  const togglePasswordVisibility = () => {
    showPassword.value = !showPassword.value;
  };

  const isValidLength = computed(() => signupForm.password.length >= 8 && signupForm.password.length <= 20);
  const hasLetter = computed(() => /[A-Za-z]/.test(signupForm.password));
  const hasNumber = computed(() => /\d/.test(signupForm.password));
  const hasSpecial = computed(() => /[!@#$%^&*()_+=-]/.test(signupForm.password));

  const isValidPassword = computed(() =>
    isValidLength.value &&
    hasLetter.value &&
    hasNumber.value &&
    hasSpecial.value
  );

  // nickname
  const isNicknameAvailable = ref(null);
  const validateNicknameErrorMessage = ref('');

  const nicknameInputClass = computed(() =>
    getValidationInputClass(isNicknameAvailable.value)
  );

  const isValidNicknameLength = computed(() => {
    const length = signupForm.nickname.length;
    return length >= 2 && length <= 15;
  });

  const isValidNicknamePattern = computed(() =>
    NICKNAME_REGEX.test(signupForm.nickname)
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

  const isSignupFormValid = computed(() =>
  isEmailAvailable.value &&
  isNicknameAvailable.value &&
  isValidPassword.value
  );

  // submit
  const validateEmail = async () => {
    try {
      const response = await apiClient.get(`${serverURL}/users/validate-email`, {
        params: { email: signupForm.email }
      });
      emailValidationErrorMessage.value = '';
      const result = response.data.result;
      isEmailAvailable.value = result.available;

    } catch (error) {
      // 네트워크 에러 (response 없음)
      if (!error.response) {
        emailValidationErrorMessage.value = 'Network error occurred.Please check the Internet connection.';
        isEmailAvailable.value = null;
        return;
      }
      
      isEmailAvailable.value = null; 
      emailValidationErrorMessage.value = 'There was a problem checking your email. Please try again.'
    }
  };

  const validateNickname = async () => {
    try {
      const response = await apiClient.get(`${serverURL}/users/validate-nickname`, {
        params: { nickname: signupForm.nickname }
      });
      validateNicknameErrorMessage.value = '';
      const result = response.data.result;
      isNicknameAvailable.value = result.available;

    } catch (error) {
      isNicknameAvailable.value = null; 
      validateNicknameErrorMessage.value = 'There was a problem checking your nickname. Please try again.'
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

  // 회원가입란에 email이 변경되면 다시 검증이 필요하다고 판단
  watch(() => signupForm.email, () => {
    isEmailAvailable.value = null;
  });

  // 회원가입란에 nickname이 변경되면 다시 검증이 필요하다고 판단
  watch(() => signupForm.nickname, () => {
    isNicknameAvailable.value = null;
  });  

  return {
    signupForm,
    resetSignupForm,
    isSignupFormValid,
    signupSuccessMessage,
    signupFailureMessage,

    email: {
      isValid: isValidEmail,
      validationMessage: emailValidationMessage,
      validationTextClass: emailValidationTextClass,
      inputClass: emailInputClass,
      validationErrorMessage: emailValidationErrorMessage,
    },
    password: {
      show: showPassword,
      toggle: togglePasswordVisibility,
      isValidLength,
      hasLetter,
      hasNumber,
      hasSpecial,
    },
    nickname: {
      isValid: isValidNickname,
      validationMessage: nicknameValidationMessage,
      validationTextClass: nicknameValidationTextClass,
      inputClass: nicknameInputClass,
      validationErrorMessage: validateNicknameErrorMessage,
    },
    submit: {
      validateEmail: validateEmail,
      validateNickname: validateNickname, 
      submitSignupForm: submitSignupForm,
    }

  };
}
