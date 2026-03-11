import type { RouteRecordStringComponent } from '@vben/types';

import { $t } from '@vben/locales';

const localRoutes: RouteRecordStringComponent[] = [
  {
    component: '/_core/profile/index',
    meta: {
      icon: 'mingcute:profile-line',
      title: $t('ui.widgets.profile'),
      hideInMenu: true,
      requireHomeRedirect: true,
    },
    name: 'Profile',
    path: '/profile',
  },
  {
    component: '/data-center/nephrology/ledger/detail/index',
    meta: {
      title: '台账详情',
      hideInMenu: true,
      keepAlive: true,
      activePath: '/data-center/nephrology/ledger',
    },
    // Menu seed: menu_id=2137, path='detail' -> 'Detail2137'
    name: 'Detail2137',
    path: '/data-center/nephrology/ledger/detail',
  },
  {
    component: '/data-center/hospital-qc/report/index',
    meta: {
      title: '医院质控报表',
      hideInMenu: true,
      keepAlive: true,
      activePath: '/data-center/hospital-qc/dashboard',
    },
    // Menu seed: menu_id=2264, path='report' -> 'Report2264'
    name: 'Report2264',
    path: '/data-center/hospital-qc/report',
  },
  {
    component: '/data-center/hospital-qc/detail/index',
    meta: {
      title: '台账明细数据',
      hideInMenu: true,
      keepAlive: true,
      activePath: '/data-center/hospital-qc/dashboard',
    },
    // Menu seed: menu_id=2264, path='detail' -> 'Detail2264'
    name: 'Detail2264',
    path: '/data-center/hospital-qc/detail',
  },
];

export const localMenuList: RouteRecordStringComponent[] = [
  {
    component: 'BasicLayout',
    meta: {
      order: -1,
      title: 'page.dashboard.title',
      noBasicLayout: true,
    },
    name: 'Dashboard',
    path: '/',
    redirect: '/analytics',
    children: [
      {
        name: 'Analytics',
        path: '/analytics',
        component: '/dashboard/analytics/index',
        meta: {
          affixTab: true,
          title: 'page.dashboard.analytics',
        },
      },
      {
        name: 'Workspace',
        path: '/workspace',
        component: '/dashboard/workspace/index',
        meta: {
          title: 'page.dashboard.workspace',
        },
      },
    ],
  },
  {
    component: '/_core/about/index',
    meta: {
      icon: 'lucide:copyright',
      order: 9999,
      title: $t('demos.vben.about'),
    },
    name: 'About',
    path: '/vben-admin/about',
  },
  ...localRoutes,
];
