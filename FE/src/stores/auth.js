import { defineStore } from 'pinia'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    accessToken: null, // kept in-memory only (not persisted to storage)
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
    clearUser() {
      this.user = null;
    }
  },
});