# taskops-frontend

对应 `TaskOps Cloud` 的前端项目（Vue3 + Vite + JavaScript + CSS）。

## 本地启动

```bash
cd taskops-frontend
npm install
npm run dev
```

默认地址：`http://localhost:5173`

## 网关联调

开发环境已配置代理：
- 前端请求 `/api/**`
- 代理到 `http://localhost:8080`（gateway-service）

请先确保后端网关和微服务已启动。

## 页面功能

- 登录：`/login`
- 注册：`/register`
- 任务列表/搜索：`/tasks`
- 创建任务（需要登录）：`/tasks/create`
- 下单与支付联调：`/orders`

## 目录

- `src/api`：接口封装（axios）
- `src/router`：路由与登录守卫
- `src/views`：页面组件
- `src/utils/auth.js`：token 本地存储
