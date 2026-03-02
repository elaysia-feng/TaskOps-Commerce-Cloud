# Day5 - Sentinel 控制台（按左侧菜单 + 截图讲解）

> 日期：2026-02-26
> 目标：看懂 Sentinel 左侧每个功能，知道“什么时候用”。

## 一、控制台左侧菜单总览

![左侧菜单截图](./images/sentinel-sidebar.png)

- 核心思路：
  - 先看“资源有没有流量”（簇点链路、实时监控）
  - 再配“保护规则”（流控、熔断、热点、系统）
  - 最后看“权限/来源控制”（授权规则）

---

## 二、簇点链路（Cluster Link）

![簇点链路截图](./images/sentinel-cluster-link.png)

### 作用
- 查看资源（接口/方法）的实时统计：QPS、线程数、响应时间等。
- 规则配置前先确认“资源名是否出现”。

### 什么时候用
- 新接口刚上线，先确认 Sentinel 是否识别到资源。
- 想判断哪个接口最热，先看这里。

---

## 三、流控规则（Flow Rule）

![流控规则截图](./images/sentinel-flow-rule.png)

### 作用
- 限流（最常用）。
- 防止某接口瞬时流量过高打爆服务。

### 常见配置
- 阈值类型：QPS / 并发线程数
- 流控模式：直接 / 关联 / 链路
- 控制效果：快速失败 / Warm Up / 匀速排队

### 什么时候用
- 登录接口防爆破：`/api/auth/login`
- 下单接口防突发：`POST /api/orders`
- 支付回调防风暴：`POST /api/pay/callback/mock`

---

## 四、熔断规则（Degrade Rule）

![熔断规则截图](./images/sentinel-degrade-rule.png)

### 作用
- 当接口慢、错误多时，临时熔断，防止故障扩散。

### 常见策略
- 慢调用比例
- 异常比例
- 异常数

### 什么时候用
- `pay-service -> order-service` 的 Feign 调用不稳定时。
- 数据库抖动导致接口 RT 飙升时。

---

## 五、热点规则（Param Flow Rule）

![热点规则截图](./images/sentinel-hotspot-rule.png)

### 作用
- 针对“参数值”做限流（不是整体接口限流）。

### 什么时候用
- 某个热门任务 ID、热门关键词被反复请求。
- 某个用户/某个 SKU 请求异常集中。

---

## 六、系统规则（System Rule）

![系统规则截图](./images/sentinel-system-rule.png)

### 作用
- 按系统整体负载保护：CPU、入口QPS、平均RT、并发线程等。

### 什么时候用
- 机器层面接近瓶颈，想做“全局刹车”。
- 大促/压测期间兜底保护。

---

## 七、授权规则（Authority Rule）

![授权规则截图](./images/sentinel-authority-rule.png)

### 作用
- 按调用来源（origin）做白名单/黑名单。

### 什么时候用
- 只允许网关调用内部接口。
- 阻断某来源服务的异常流量。

---

## 八、你这个项目里的优先落地顺序

1. `auth-service` 登录接口先配流控规则（你今天已在测）。
2. `order-service` 下单接口配流控 + 熔断。
3. `pay-service` 支付回调接口配流控。
4. `pay -> order` 的 Feign 调用配熔断 + fallback。
5. 最后再看热点规则和系统规则。

---

## 九、今天压测验证结论

- 已用 JMeter 验证登录限流：
  - 脚本：`E:\develop\Jmeter\sentinel-login-limit-test.jmx`
- 结果判定：
  - `code=0`：正常通过
  - `code=429`：触发限流（生效）

---

## 十、截图命名建议（便于文档自动显示）

把你的 Sentinel 截图放到：`day5/images/`，文件名按下面命名：

- `sentinel-sidebar.png`
- `sentinel-cluster-link.png`
- `sentinel-flow-rule.png`
- `sentinel-degrade-rule.png`
- `sentinel-hotspot-rule.png`
- `sentinel-system-rule.png`
- `sentinel-authority-rule.png`

这样 `Day5.md` 会直接渲染图片。