import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap/dist/js/bootstrap.bundle.min'

import { library } from '@fortawesome/fontawesome-svg-core'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'

// 사용할 아이콘
import { faUserLarge, faBullhorn, faPen, faCamera } from '@fortawesome/free-solid-svg-icons'
import { faMessage, faMap} from '@fortawesome/free-regular-svg-icons'

library.add(faUserLarge, faBullhorn, faPen, faMessage, faMap, faCamera)

createApp(App)
  .component('font-awesome-icon', FontAwesomeIcon)
  .mount('#app')
