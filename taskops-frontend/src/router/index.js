import { createRouter, createWebHistory } from "vue-router";
import AuthLayout from "../layouts/AuthLayout.vue";
import MainLayout from "../layouts/MainLayout.vue";
import LoginView from "../views/LoginView.vue";
import RegisterView from "../views/RegisterView.vue";
import TaskListView from "../views/TaskListView.vue";
import CreateTaskView from "../views/CreateTaskView.vue";
import DashboardView from "../views/DashboardView.vue";
import OrderPayView from "../views/OrderPayView.vue";
import { getToken } from "../utils/auth";

const routes = [
  {
    path: "/",
    component: MainLayout,
    meta: { requiresAuth: true },
    children: [
      { path: "", redirect: "/dashboard" },
      { path: "dashboard", component: DashboardView },
      { path: "tasks", component: TaskListView },
      { path: "tasks/create", component: CreateTaskView },
      { path: "orders", component: OrderPayView }
    ]
  },
  {
    path: "/",
    component: AuthLayout,
    meta: { guestOnly: true },
    children: [
      { path: "login", component: LoginView },
      { path: "register", component: RegisterView }
    ]
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

router.beforeEach((to, from, next) => {
  const token = getToken();
  if (to.meta.requiresAuth && !token) {
    next("/login");
    return;
  }
  if (to.meta.guestOnly && token) {
    next("/dashboard");
    return;
  }
  next();
});

export default router;
