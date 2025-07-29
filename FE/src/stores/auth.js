import { defineStore } from 'pinia'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    accessToken: null, // in-memory 저장
    user: null,
  }),
  actions: {
    setAccessToken(token) {
      this.accessToken = token;
    },
    clearAccessToken() {
      this.accessToken = null;
    },
    setUser(user) {
      this.user = user;
    },
  },
});