import { createRouter, createWebHistory } from "vue-router";
import AuthLayout from "../layouts/AuthLayout.vue";
import MainLayout from "../layouts/MainLayout.vue";
import LoginView from "../views/LoginView.vue";
import RegisterView from "../views/RegisterView.vue";
import TaskListView from "../views/TaskListView.vue";
import CreateTaskView from "../views/CreateTaskView.vue";
import DashboardView from "../views/DashboardView.vue";
import CreateOrderView from "../views/CreateOrderView.vue";
import PaymentView from "../views/PaymentView.vue";
import MyOrdersView from "../views/MyOrdersView.vue";
import MyAcceptedTasksView from "../views/MyAcceptedTasksView.vue";
import TaskReviewView from "../views/TaskReviewView.vue";
import AiConsoleView from "../views/AiConsoleView.vue";
import WalletView from "../views/WalletView.vue";
import { getToken } from "../utils/auth";

const routes = [
  {
    path: "/",
    component: MainLayout,
    meta: { requiresAuth: true },
    children: [
      { path: "", redirect: "/dashboard" },
      {
        path: "dashboard",
        component: DashboardView,
        meta: {
          eyebrow: "Overview",
          title: "总览看板",
          description: "任务、订单、支付、AI 和钱包能力在这里汇总。"
        }
      },
      {
        path: "tasks",
        component: TaskListView,
        meta: {
          eyebrow: "Marketplace",
          title: "任务大厅",
          description: "筛选、查看详情并直接接单。"
        }
      },
      {
        path: "tasks/create",
        component: CreateTaskView,
        meta: {
          eyebrow: "Studio",
          title: "发布与管理",
          description: "创建任务、切换会员等级，并管理我发布的任务。"
        }
      },
      {
        path: "tasks/accepted",
        component: MyAcceptedTasksView,
        meta: {
          eyebrow: "Execution",
          title: "我的接单",
          description: "跟进进行中的任务并提交完成内容。"
        }
      },
      {
        path: "tasks/review",
        component: TaskReviewView,
        meta: {
          eyebrow: "Review",
          title: "待我验收",
          description: "查看提交结果并执行通过或驳回。"
        }
      },
      { path: "orders", redirect: "/orders/create" },
      {
        path: "orders/create",
        component: CreateOrderView,
        meta: {
          eyebrow: "Commerce",
          title: "创建订单",
          description: "从商品目录快速创建订单并进入支付流程。"
        }
      },
      {
        path: "payments",
        component: PaymentView,
        meta: {
          eyebrow: "Payments",
          title: "支付中心",
          description: "创建、查询和关闭支付单。"
        }
      },
      {
        path: "account/orders",
        component: MyOrdersView,
        meta: {
          eyebrow: "Orders",
          title: "我的订单",
          description: "查看订单详情、状态流转和待支付订单。"
        }
      },
      {
        path: "wallet",
        component: WalletView,
        meta: {
          eyebrow: "Wallet",
          title: "钱包中心",
          description: "余额、流水、提现，以及管理员审核都集中在这里。"
        }
      },
      {
        path: "ai",
        component: AiConsoleView,
        meta: {
          eyebrow: "AI Console",
          title: "AI 助手",
          description: "管理会话、长期记忆和流式对话。"
        }
      }
    ]
  },
  {
    path: "/",
    component: AuthLayout,
    meta: { guestOnly: true },
    children: [
      {
        path: "login",
        component: LoginView,
        meta: {
          eyebrow: "Login",
          title: "进入 TaskOps",
          description: "登录后即可进入统一工作台。"
        }
      },
      {
        path: "register",
        component: RegisterView,
        meta: {
          eyebrow: "Register",
          title: "创建账号",
          description: "先注册，再进入任务协作工作台。"
        }
      }
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