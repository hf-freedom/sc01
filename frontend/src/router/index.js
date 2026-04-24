import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('../views/Layout.vue'),
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/Dashboard.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'system/user',
        name: 'UserManagement',
        component: () => import('../views/system/UserManagement.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'system/role',
        name: 'RoleManagement',
        component: () => import('../views/system/RoleManagement.vue'),
        meta: { title: '角色管理' }
      },
      {
        path: 'system/organization',
        name: 'OrganizationManagement',
        component: () => import('../views/system/OrganizationManagement.vue'),
        meta: { title: '组织机构' }
      },
      {
        path: 'system/resource',
        name: 'ResourceManagement',
        component: () => import('../views/system/ResourceManagement.vue'),
        meta: { title: '资源管理' }
      },
      {
        path: 'app/list',
        name: 'ApplicationList',
        component: () => import('../views/app/ApplicationList.vue'),
        meta: { title: '应用管理' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  if (!userStore.isLoggedIn) {
    userStore.restoreState()
  }

  if (to.meta.requiresAuth !== false && !userStore.isLoggedIn) {
    next('/login')
  } else if (to.path === '/login' && userStore.isLoggedIn) {
    next('/dashboard')
  } else {
    next()
  }
})

export default router
