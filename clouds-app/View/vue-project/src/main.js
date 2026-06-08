// import './assets/main.css'
// import './assets/custom.css';

import { createApp } from 'vue'
import App from './App.vue'
import { Amplify } from 'aws-amplify';
import {createBootstrap} from 'bootstrap-vue-next'
import 'bootstrap/dist/css/bootstrap.css'
import 'bootstrap-vue-next/dist/bootstrap-vue-next.css'
import amplifyconfig from './amplifyconfiguration';
import Toast from 'vue-toastification';
import "vue-toastification/dist/index.css";
import { library } from '@fortawesome/fontawesome-svg-core'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'



import { faDownload, faSignOutAlt, faArrowUpFromBracket, faList } from '@fortawesome/free-solid-svg-icons'
Amplify.configure(amplifyconfig);


const existingConfig = Amplify.getConfig();

// Add existing resource to the existing configuration.
Amplify.configure({
  ...existingConfig,
  API: {
    ...existingConfig.API,
    REST: {
      ...existingConfig.API?.REST,
      YourAPIName: {
        endpoint:
          'https://tnvswpmu22.execute-api.eu-north-1.amazonaws.com/Stage1',
        region: 'ue-north-1' // Optional
      }
    }
  }
});


library.add(faDownload, faSignOutAlt, faArrowUpFromBracket, faList)

const app = createApp(App)
app.use(createBootstrap()).use(Toast)
app.component('font-awesome-icon', FontAwesomeIcon)
app.mount('#app')
