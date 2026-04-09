# TaskOps V2 库表设计说明

这份设计稿是你后续重构的目标库表，不是在旧表上继续打补丁。

## 分库建议

- `taskops_auth`
  负责账号、登录凭证、角色、会话、登录日志。
- `taskops_member`
  负责会员套餐、用户会员权益、会员变更流水。
- `taskops_task`
  负责任务发布、接单关系、提交材料、审核、结算请求。
- `taskops_trade`
  负责交易订单、支付单、支付回调、退款单、交易出站消息。
- `taskops_wallet`
  负责钱包账户、流水、冻结、提现账户、提现申请。
- `taskops_ai`
  负责 AI 会话、消息、记忆、工具调用日志。

## 这次重构的核心取舍

- 不再把“任务业务单”和“支付单”混成一个表。
- 不再把“会员等级”只放在 Redis 里。
- 不再把“提交材料”塞到一个 `proofUrls` 字符串字段里。
- 不再用 `t_stock` 这种库存表去承载会员商品。
- 不使用跨库外键，保持微服务边界清晰。

## 旧表到新表的主要映射

- `sys_user` -> `taskops_auth.user_account`
- `sys_role` -> `taskops_auth.auth_role`
- `sys_user_role` -> `taskops_auth.auth_user_role`
- `login_log` -> `taskops_auth.auth_login_log`
- `internship_task` -> `taskops_task.task_post`
- `t_task_submission` -> `taskops_task.task_submission`
- `proofUrls` -> `taskops_task.task_submission_attachment`
- `t_task_settlement` -> `taskops_task.task_settlement_request`
- `t_order` -> `taskops_trade.trade_order`
- `t_payment` -> `taskops_trade.payment_order`
- `t_refund` -> `taskops_trade.refund_order`
- `t_wallet` -> `taskops_wallet.wallet_account`
- `t_wallet_flow` -> `taskops_wallet.wallet_ledger`
- `t_withdraw` -> `taskops_wallet.withdraw_request`

## 为什么会员要单独拆出来

会员不是登录信息，也不是支付流水本身，它是用户已经获得的权益结果。

一笔会员购买成功以后，正常链路应该是：

1. `taskops_trade.trade_order` 变成 `PAID`
2. `taskops_trade.payment_order` 变成 `SUCCESS`
3. `taskops_member.user_membership` 新增或续期
4. `task-service` 再通过会员域读取发布额度，而不是只读 Redis 临时值

## 为什么任务提交材料要单独建表

真实项目里，一次提交通常不止一个材料，可能会有：

- 图片
- PDF
- 压缩包
- 视频链接

如果继续用 `proofUrls VARCHAR(...)`：

- 多文件结构不清晰
- 不方便记录文件大小、类型、原始文件名
- 接阿里云 OSS、CDN、鉴权访问时会越来越乱

所以建议直接拆成：

- `task_submission`
- `task_submission_attachment`

## 建议的代码迁移顺序

第一阶段先做最值钱的部分：

1. 在 `task-service` 新增上传接口，把提交材料传到 OSS。
2. 把 `SubmitTaskRequest` 从 `proofUrls:String` 升级成 `proofs:List<TaskProofItem>`。
3. 在任务域落 `task_submission_attachment`。
4. 把会员购买逻辑从 `t_stock` 迁到 `trade + member` 这套结构。
5. 把任务发布额度从 Redis 迁到 `taskops_member.user_membership`。

第二阶段再做：

1. 真正的支付回调。
2. 退款单和取消订单闭环。
3. 发布任务时生成托管交易单。
4. 审核通过后通过结算请求驱动钱包入账。

## 为什么没有直接覆盖旧的 `init.sql`

因为现在的服务代码还绑定在旧表名和旧字段上。

如果直接把旧表替换掉：

- `auth-service`
- `task-service`
- `order-service`
- `pay-service`
- `wallet-service`

这些服务大概率会直接启动失败。

更稳的做法是：

1. 先定目标库表
2. 再按服务逐步改代码
3. 最后再切正式初始化脚本和迁移脚本