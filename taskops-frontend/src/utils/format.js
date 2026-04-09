export function formatMoney(amount) {
  const value = Number(amount ?? 0);
  return `¥${Number.isFinite(value) ? value.toFixed(2) : "0.00"}`;
}

export function formatDateTime(value) {
  if (!value) {
    return "未设置";
  }

  const normalized = typeof value === "string" ? value.replace(" ", "T") : value;
  const date = normalized instanceof Date ? normalized : new Date(normalized);
  if (Number.isNaN(date.getTime())) {
    return String(value).replace("T", " ").slice(0, 16);
  }

  return new Intl.DateTimeFormat("zh-CN", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
    hour12: false
  })
    .format(date)
    .replace(/\//g, "-");
}

export function formatCompactNumber(value) {
  const amount = Number(value ?? 0);
  if (!Number.isFinite(amount)) {
    return "0";
  }
  return new Intl.NumberFormat("zh-CN").format(amount);
}

export function humanize(value) {
  if (!value) {
    return "-";
  }

  return String(value)
    .replace(/_/g, " ")
    .toLowerCase()
    .replace(/\b\w/g, (char) => char.toUpperCase());
}

export function taskStatusLabel(status) {
  const map = {
    OPEN: "待接单",
    TAKEN: "进行中",
    SUBMITTED: "待验收",
    REJECTED: "已驳回",
    APPROVED: "已通过",
    SETTLEMENT_PENDING: "结算中",
    SETTLED: "已结算",
    CANCELLED: "已取消"
  };
  return map[status] || status || "未知状态";
}

export function taskCategoryLabel(category) {
  const map = {
    ERRAND: "跑腿",
    DESIGN: "设计",
    TECH: "技术",
    CONSULT: "咨询",
    GENERAL: "综合"
  };
  return map[category] || category || "综合";
}

export function membershipLabel(level) {
  const map = {
    FREE: "免费版",
    VIP: "VIP",
    SVIP: "SVIP"
  };
  return map[level] || level || "未开通";
}

export function orderStatusLabel(status) {
  const map = {
    PENDING_PAY: "待支付",
    PAID: "已支付",
    DONE: "已完成",
    CANCELLED: "已取消"
  };
  return map[status] || status || "未知状态";
}

export function payStatusLabel(status) {
  const map = {
    CREATED: "已创建",
    WAIT_BUYER_PAY: "待支付",
    CLOSED: "已关闭",
    SUCCESS: "支付成功",
    TRADE_SUCCESS: "交易成功"
  };
  return map[status] || status || "未知状态";
}

export function walletStatusLabel(status) {
  const map = {
    NORMAL: "正常",
    FROZEN: "冻结"
  };
  return map[status] || status || "未知状态";
}

export function withdrawStatusLabel(status) {
  const map = {
    PENDING: "待审核",
    APPROVED: "已通过",
    REJECTED: "已驳回",
    PAID: "已打款",
    DONE: "已完成"
  };
  return map[status] || status || "未知状态";
}

export function flowTypeLabel(flowType) {
  const map = {
    INCOME: "收入",
    EXPENSE: "支出",
    WITHDRAW: "提现",
    FREEZE: "冻结",
    UNFREEZE: "解冻"
  };
  return map[flowType] || humanize(flowType);
}

export function flowDirectionLabel(direction) {
  const map = {
    IN: "入账",
    OUT: "出账"
  };
  return map[direction] || direction || "-";
}

export function hasRole(user, role) {
  return Array.isArray(user?.roles) && user.roles.includes(role);
}

export function isAdmin(user) {
  return hasRole(user, "ADMIN");
}

export function taskCanCancel(status) {
  return !["CANCELLED", "SETTLED"].includes(status);
}