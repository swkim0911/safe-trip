import { createApp } from 'vue'
import './style.css'
import App from './App.vue'

import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap-icons/font/bootstrap-icons.css'

import { library } from '@fortawesome/fontawesome-svg-core'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import { createPinia } from 'pinia';

const pinia = createPinia();

// 사용할 아이콘
import { faUserLarge, faUserGear, faBullhorn, faPen, faCamera, faChevronLeft, faChevronRight, faArrowRightFromBracket, faMessage, faTriangleExclamation, faMapLocationDot, faSearch, faGlobe, faFileAlt, faMapMarkerAlt, faLocationDot, faShieldAlt, faShieldHalved } from '@fortawesome/free-solid-svg-icons'
import {} from '@fortawesome/free-regular-svg-icons'

library.add(faUserLarge, faUserGear, faBullhorn, faPen, faMessage, faCamera, faChevronLeft, faChevronRight, faArrowRightFromBracket, faTriangleExclamation, faMapLocationDot, faSearch, faGlobe, faFileAlt, faMapMarkerAlt, faLocationDot, faShieldAlt, faShieldHalved)

createApp(App)
  .component('font-awesome-icon', FontAwesomeIcon)
  .use(pinia)
  .mount('#app')
