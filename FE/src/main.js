import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap/dist/js/bootstrap.bundle.min'
import 'bootstrap-icons/font/bootstrap-icons.css'

import { library } from '@fortawesome/fontawesome-svg-core'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import { createPinia } from 'pinia';

const pinia = createPinia();

// 사용할 아이콘
import { faUserLarge, faBullhorn, faPen, faCamera, faChevronLeft } from '@fortawesome/free-solid-svg-icons'
import { faMessage, faMap} from '@fortawesome/free-regular-svg-icons'

library.add(faUserLarge, faBullhorn, faPen, faMessage, faMap, faCamera, faChevronLeft)

createApp(App)
  .component('font-awesome-icon', FontAwesomeIcon)
  .use(pinia)
  .mount('#app')
