import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/Login.vue'),
      meta: {
        title: '登录',
        requiresAuth: false
      }
    },
    {
      path: '/',
      component: () => import('@/views/Layout.vue'),
      redirect: '/dashboard',
      meta: {
        requiresAuth: true
      },
      children: [
        {
          path: 'dashboard',
          name: 'Dashboard',
          component: () => import('@/views/Dashboard.vue'),
          meta: {
            title: '仪表盘',
            requiresAuth: true
          }
        },
        {
          path: 'order',
          name: 'Order',
          redirect: '/order/list',
          meta: {
            title: '订单管理',
            requiresAuth: true
          },
          children: [
            {
              path: 'list',
              name: 'OrderList',
              component: () => import('@/views/order/OrderList.vue'),
              meta: {
                title: '订单列表',
                requiresAuth: true
              }
            },
            {
              path: 'statistics',
              name: 'OrderStatistics',
              component: () => import('@/views/order/OrderStatistics.vue'),
              meta: {
                title: '订单统计',
                requiresAuth: true
              }
            }
          ]
        },
        {
          path: 'user',
          name: 'User',
          redirect: '/user/list',
          meta: {
            title: '用户管理',
            requiresAuth: true
          },
          children: [
            {
              path: 'list',
              name: 'UserList',
              component: () => import('@/views/user/UserList.vue'),
              meta: {
                title: '用户列表',
                requiresAuth: true
              }
            },
            {
              path: 'pilot',
              name: 'PilotList',
              component: () => import('@/views/user/PilotList.vue'),
              meta: {
                title: '飞手管理',
                requiresAuth: true
              }
            }
          ]
        },
        {
          path: 'map',
          name: 'Map',
          component: () => import('@/views/Map.vue'),
          meta: {
            title: '地图监控',
            requiresAuth: true
          }
        },
        {
          path: 'payment',
          name: 'Payment',
          component: () => import('@/views/Payment.vue'),
          meta: {
            title: '支付管理',
            requiresAuth: true
          }
        },
        {
          path: 'system',
          name: 'System',
          redirect: '/system/config',
          meta: {
            title: '系统管理',
            requiresAuth: true
          },
          children: [
            {
              path: 'config',
              name: 'SystemConfig',
              component: () => import('@/views/system/SystemConfig.vue'),
              meta: {
                title: '系统配置',
                requiresAuth: true
              }
            },
            {
              path: 'log',
              name: 'SystemLog',
              component: () => import('@/views/system/SystemLog.vue'),
              meta: {
                title: '操作日志',
                requiresAuth: true
              }
            }
          ]
        }
      ]
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'NotFound',
      component: () => import('@/views/NotFound.vue'),
      meta: {
        title: '页面不存在'
      }
    }
  ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  // 设置页面标题
  if (to.meta?.title) {
    document.title = `${to.meta.title} - 无人机配送平台`
  }
  
  // 检查是否需要登录
  if (to.meta?.requiresAuth && !userStore.isLoggedIn) {
    next('/login')
  } else if (to.path === '/login' && userStore.isLoggedIn) {
    next('/dashboard')
  } else {
    next()
  }
})

export default router
