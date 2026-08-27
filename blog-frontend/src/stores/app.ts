import { defineStore } from 'pinia'
import { ref } from 'vue'

/**
 * 应用级状态：站点全局配置
 * M4 里程碑接入后端 GET /api/config/public 后，改为从接口拉取
 */
export const useAppStore = defineStore('app', () => {
  const siteConfig = ref({
    siteName: '我的博客',
    slogan: '记录与分享',
  })

  return { siteConfig }
})
