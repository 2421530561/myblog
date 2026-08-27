import { createApp } from 'vue'
import { createPinia } from 'pinia'
import naive from 'naive-ui'
import App from './App.vue'
import router from './router'

const app = createApp(App)

app.use(createPinia())
app.use(router)
// M0 阶段全量引入 Naive UI，后续可改为按需引入（unplugin-vue-components）以减小体积
app.use(naive)

app.mount('#app')
