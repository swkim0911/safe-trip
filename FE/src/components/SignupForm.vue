<template >
  <form @submit.prevent class="px-3 py-3" style="max-width: 400px; margin: 0 auto;">
    <h3 class="text-center mb-3 fw-bold">Create Your Account</h3>

    <div class="mb-3">
      <label for="email" class="form-label fw-bold">Email</label>
      <div class="input-group">
        <input
          type="email"
          id="email"
          :class="email.emailInputClass.value"
          v-model="signupForm.email"
          placeholder="user@example.com"
          required
        />
        <button :disabled="!email.isValid" type="button" class="btn btn-outline-secondary" @click="submit.validateEmail">
          Check
        </button>
      </div>
      <div class="mt-1">
        <p v-if="email.validationMessage" :class="['small', email.validationTextClass]">
          {{ email.validationMessage }}
        </p>
        <p v-if="email.validationErrorMessage" class="text-danger small">
          {{ email.validationErrorMessage }}
        </p>
      </div>
    </div>

    <div class="mb-3">
      <label for="password" class="form-label fw-bold">Password</label>
      <div class="input-group">
        <input
          :type="password.show ? 'text' : 'password'"
          id="password"
          class="form-control"
          v-model="signupForm.password"
          placeholder="Enter your password"
          required
        />
        <button type="button" class="btn btn-outline-secondary" @click="password.toggle">
          <i :class="password.show ? 'bi bi-eye-slash' : 'bi bi-eye'"></i>
        </button>
      </div>
      <div v-if="signupForm.password">
        <ul class="mt-2 small text-muted">
          <li :class="password.isValidLength ? 'text-success' : 'text-danger'">
            Must be between 8 and 20 characters
          </li>
          <li :class="password.hasLetter ? 'text-success' : 'text-danger'">
            Must include at least one letter
          </li>
          <li :class="password.hasNumber ? 'text-success' : 'text-danger'">
            Must include at least one number
          </li>
          <li :class="password.hasSpecial ? 'text-success' : 'text-danger'">
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
          :class="nickname.inputClass"
          v-model="signupForm.nickname"
          placeholder="Enter your nickname"
          required
        />
        <button :disabled="!nickname.isValid" type="button" class="btn btn-outline-secondary" @click="submit.validateNickname">
          Check 
        </button>
      </div>
      <div class="mt-1">
        <p v-if="nickname.validationMessage" :class="['small', nickname.validationTextClass]">
            {{ nickname.validationMessage }}
        </p>
        <p v-if="nickname.validationErrorMessage" class="text-danger small">
            {{ nickname.validationErrorMessage }}
        </p>
      </div>
    
    </div>
    <button :disabled="!isSignupFormValid" type="submit" class="btn btn-primary w-100 py-2" @click="submit.signupForm">Sign Up</button>
  </form>
  <p class="text-center text-success fw-bold" v-if="signupSuccessMessage">{{ signupSuccessMessage }}</p>
  <p class="text-center text-danger fw-bold" v-if="signupFailureMessage">{{ signupFailureMessage }}</p>
  <p class="text-center mt-3 mb-0">
    Already have an account?
    <a href="#" class="text-decoration-none" role="button" @click.prevent="mode = 'login'">Log In</a>
  </p>
  <!-- <form @submit.prevent class="px-3 py-3" style="max-width: 400px; margin: 0 auto;">
    <h3 class="text-center mb-3 fw-bold">Create Your Account</h3>

    <div class="mb-3">
      <label for="email" class="form-label fw-bold">Email</label>
      <div class="input-group">
        <input
          type="email"
          id="email"
          :class="email.inputClass"
          v-model="signupForm.email"
          placeholder="user@example.com"
          required
        />
        <button :disabled="!email.isValid" type="button" class="btn btn-outline-secondary" @click="submit.validateEmail">
          Check
        </button>
      </div>
      <div class="mt-1">
        <p v-if="email.validationMessage" :class="['small', email.validationTextClass]">
          {{ email.validationMessage }}
        </p>
        <p v-if="email.validationErrorMessage" class="text-danger small">
          {{ email.validationErrorMessage }}
        </p>
      </div>
    </div>

    <div class="mb-3">
      <label for="password" class="form-label fw-bold">Password</label>
      <div class="input-group">
        <input
          :type="password.show ? 'text' : 'password'"
          id="password"
          class="form-control"
          v-model="signupForm.password"
          placeholder="Enter your password"
          required
        />
        <button type="button" class="btn btn-outline-secondary" @click="password.toggle">
          <i :class="showPassword ? 'bi bi-eye-slash' : 'bi bi-eye'"></i>
        </button>
      </div>
      <div v-if="signupForm.password">
        <ul class="mt-2 small text-muted">
          <li :class="password.isValidLength ? 'text-success' : 'text-danger'">
            Must be between 8 and 20 characters
          </li>
          <li :class="password.hasLetter ? 'text-success' : 'text-danger'">
            Must include at least one letter
          </li>
          <li :class="password.hasNumber ? 'text-success' : 'text-danger'">
            Must include at least one number
          </li>
          <li :class="password.hasSpecial ? 'text-success' : 'text-danger'">
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
          :class="nickname.inputClass"
          v-model="signupForm.nickname"
          placeholder="Enter your nickname"
          required
        />
        <button :disabled="!nickname.isValid" type="button" class="btn btn-outline-secondary" @click="submit.validateNickname">
          Check 
        </button>
      </div>
      <div class="mt-1">
        <p v-if="nickname.validationMessage" :class="['small', nickname.validationTextClass]">
            {{ nickname.validationMessage }}
        </p>
        <p v-if="nickname.validationErrorMessage" class="text-danger small">
            {{ nickname.validationErrorMessage }}
        </p>
      </div>
    
    </div>
    <button :disabled="!isSignupFormValid" type="submit" class="btn btn-primary w-100 py-2" @click="subit.signupForm">Sign Up</button>
  </form>
  <p class="text-center text-success fw-bold" v-if="signupSuccessMessage">{{ signupSuccessMessage }}</p>
  <p class="text-center text-danger fw-bold" v-if="signupFailureMessage">{{ signupFailureMessage }}</p>
  <p class="text-center mt-3 mb-0">
    Already have an account?
    <a href="#" class="text-decoration-none" role="button" @click.prevent="mode = 'login'">Log In</a>
  </p> -->
</template>
<script setup>
  import { useSignupForm } from '@/composables/useSignupForm';
  const { signupForm, resetSignupForm, isSignupFormValid, signupSuccessMessage, signupFailureMessage, email, password, nickname, submit } = useSignupForm();

</script>
<style lang="">
  
</style>