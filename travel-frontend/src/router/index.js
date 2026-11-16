import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const routes = [
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    children: [
      { path: '', name: 'Home', component: () => import('@/views/Home.vue') },
      { path: 'ai/planner', name: 'AiPlanner', component: () => import('@/views/ai/AiPlanner.vue') },
      { path: 'weather', name: 'Weather', component: () => import('@/views/Weather.vue') },
      { path: 'line/:id', name: 'LineDetail', component: () => import('@/views/LineDetail.vue') },
      { path: 'search', name: 'Search', component: () => import('@/views/Search.vue') },
      { path: 'scenic-search', name: 'ScenicSearch', component: () => import('@/views/ScenicSearch.vue') },
      { path: 'hotels', name: 'HotelList', component: () => import('@/views/hotel/HotelList.vue') },
      { path: 'hotels/:id', name: 'HotelDetail', component: () => import('@/views/hotel/HotelDetail.vue') },
      { path: 'travel-notes', name: 'TravelNoteList', component: () => import('@/views/travel/TravelNoteList.vue') },
      { path: 'travel-notes/:id', name: 'TravelNoteDetail', component: () => import('@/views/travel/TravelNoteDetail.vue') },
      { path: 'travel-notes/create', name: 'TravelNoteCreate', component: () => import('@/views/travel/TravelNoteCreate.vue'), meta: { requiresAuth: true } },
      { path: 'companion', name: 'CompanionList', component: () => import('@/views/companion/CompanionList.vue') },
      { path: 'companion/publish', name: 'CompanionPublish', component: () => import('@/views/companion/CompanionPublish.vue'), meta: { requiresAuth: true } },
      { path: 'companion/:id', name: 'CompanionDetail', component: () => import('@/views/companion/CompanionDetail.vue') },
      { path: 'companion/:id/group-chat', name: 'GroupChat', component: () => import('@/views/companion/GroupChat.vue'), meta: { requiresAuth: true } },
      {
        path: 'order/confirm/:lineId',
        name: 'OrderConfirm',
        component: () => import('@/views/OrderConfirm.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'order/result/:orderNo',
        name: 'OrderResult',
        component: () => import('@/views/OrderResult.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'user',
        component: () => import('@/views/user/UserCenter.vue'),
        meta: { requiresAuth: true },
        children: [
          { path: '', name: 'UserCenter', redirect: '/user/orders' },
          { path: 'orders', name: 'MyOrders', component: () => import('@/views/user/MyOrders.vue') },
          { path: 'favorites', name: 'MyFavorites', component: () => import('@/views/user/MyFavorites.vue') },
          { path: 'route', name: 'MyRoute', component: () => import('@/views/user/MyRoute.vue') },
          { path: 'profile', name: 'MyProfile', component: () => import('@/views/user/MyProfile.vue') },
          { path: 'complaints', name: 'MyComplaints', component: () => import('@/views/user/MyComplaints.vue') },
          { path: 'coupons', name: 'MyCoupons', component: () => import('@/views/user/MyCoupons.vue') },
          { path: 'student-auth', name: 'StudentAuth', component: () => import('@/views/user/StudentAuth.vue') }
        ]
      }
    ]
  },
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      { path: '', name: 'AdminDashboard', component: () => import('@/views/admin/Dashboard.vue') },
      { path: 'scenic-spot', name: 'AdminScenicSpot', component: () => import('@/views/admin/ScenicSpotManage.vue') },
      { path: 'lines', name: 'AdminLines', component: () => import('@/views/admin/LineManage.vue') },
      { path: 'orders', name: 'AdminOrders', component: () => import('@/views/admin/OrderManage.vue') },
      { path: 'users', name: 'AdminUsers', component: () => import('@/views/admin/UserManage.vue') },
      { path: 'statistics', name: 'AdminStatistics', component: () => import('@/views/admin/Statistics.vue') },
      { path: 'price', name: 'AdminPrice', component: () => import('@/views/admin/PriceManage.vue') },
      { path: 'complaints', name: 'AdminComplaints', component: () => import('@/views/admin/ComplaintManage.vue') },
      { path: 'login-log', name: 'AdminLoginLog', component: () => import('@/views/admin/LoginLog.vue') },
      { path: 'companion', name: 'AdminCompanion', component: () => import('@/views/admin/CompanionManage.vue') },
      { path: 'campus-leader', name: 'AdminCampusLeader', component: () => import('@/views/admin/CampusLeaderManage.vue') },
      { path: 'coupon', name: 'AdminCoupon', component: () => import('@/views/admin/CouponManage.vue') },
      { path: 'activity', name: 'AdminActivity', component: () => import('@/views/admin/ActivityManage.vue') },
      { path: 'student-auth', name: 'AdminStudentAuth', component: () => import('@/views/admin/StudentAuthManage.vue') },
      { path: 'content-review', name: 'AdminContentReview', component: () => import('@/views/admin/ContentReview.vue') }
    ]
  },
  { path: '/login', name: 'Login', component: () => import('@/views/Login.vue') },
  { path: '/register', name: 'Register', component: () => import('@/views/Register.vue') }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()

  // 检查是否需要登录
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
    return
  }

  // 检查是否需要管理员权限
  if (to.meta.requiresAdmin) {
    console.log('检查管理员权限:', {
      userInfo: userStore.userInfo,
      role: userStore.userInfo?.role,
      isAdmin: userStore.userInfo?.role === 'ADMIN'
    })

    if (userStore.userInfo?.role !== 'ADMIN') {
      ElMessage.warning('无权限访问，需要管理员权限')
      next('/')
      return
    }
  }

  // 管理员也可以浏览前台页面，后台入口由顶部导航提供，避免登录后“困”在后台找不到前台页面
  next()
})

export default router
