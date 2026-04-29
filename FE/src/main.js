import { createApp } from 'vue'
import App from './App.vue'

import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap-icons/font/bootstrap-icons.css'
import './style.css'

import { library } from '@fortawesome/fontawesome-svg-core'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import { createPinia } from 'pinia';

const pinia = createPinia();

// 사용할 아이콘
import { faUserLarge, faUserGear, faUserShield, faBullhorn, faPen, faCamera, faChevronLeft, faChevronRight, faArrowRightFromBracket, faArrowUpRightFromSquare, faCircleCheck, faDatabase, faMessage, faTriangleExclamation, faMapLocationDot, faSearch, faGlobe, faFileAlt, faMapMarkerAlt, faLocationDot, faShieldAlt, faShieldHalved } from '@fortawesome/free-solid-svg-icons'
import {} from '@fortawesome/free-regular-svg-icons'

library.add(faUserLarge, faUserGear, faUserShield, faBullhorn, faPen, faCamera, faChevronLeft, faChevronRight, faArrowRightFromBracket, faArrowUpRightFromSquare, faCircleCheck, faDatabase, faMessage, faTriangleExclamation, faMapLocationDot, faSearch, faGlobe, faFileAlt, faMapMarkerAlt, faLocationDot, faShieldAlt, faShieldHalved)

window.__initGA = () => {
  if (window.__gaInitialized) return;
  window.__gaInitialized = true;
  const script = document.createElement('script');
  script.async = true;
  script.src = 'https://www.googletagmanager.com/gtag/js?id=G-2D9DY6K13V';
  document.head.appendChild(script);
  window.dataLayer = window.dataLayer || [];
  function gtag() { window.dataLayer.push(arguments); }
  window.gtag = gtag;
  gtag('js', new Date());
  gtag('config', 'G-2D9DY6K13V');
};

if (import.meta.env.PROD && localStorage.getItem('safetrip_cookie_consent') === 'accepted') {
  window.__initGA();
}

createApp(App)
  .component('font-awesome-icon', FontAwesomeIcon)
  .use(pinia)
  .mount('#app')
