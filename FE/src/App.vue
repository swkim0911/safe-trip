<script setup>
import SideBar from './components/SideBar.vue';
import MapView from './components/MapView.vue';
import ScamStatsPanel from './components/ScamStatsPanel.vue';
import WelcomeModal from './components/WelcomeModal.vue';
import PrivacyPolicyModal from './components/PrivacyPolicyModal.vue';
import TermsOfServiceModal from './components/TermsOfServiceModal.vue';
import CookieConsentBanner from './components/CookieConsentBanner.vue';
</script>


<template>
  <WelcomeModal/>
  <PrivacyPolicyModal/>
  <TermsOfServiceModal/>
  <ScamStatsPanel/>
  <SideBar/>
  <MapView/>
  <CookieConsentBanner/>
</template>

<style scoped lang="scss">

</style>
