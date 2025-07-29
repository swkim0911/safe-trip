import { defineStore } from 'pinia'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    accessToken: null, // in-memory 저장
    nickname: null,
  }),
  actions: {
    setAccessToken(token) {
      this.accessToken = token;
    },
    clearAccessToken() {
      this.accessToken = null;
    },
  },
});