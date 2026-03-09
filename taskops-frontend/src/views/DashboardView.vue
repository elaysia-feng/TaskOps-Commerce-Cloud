<template>
  <section class="card">
    <h1>控制台</h1>
    <p class="muted">当前已接入 Vue3 + Pinia + Element Plus + ECharts 模板。</p>

    <div class="kpi-grid">
      <article>
        <h3>角色</h3>
        <p>{{ roleText }}</p>
      </article>
      <article>
        <h3>链路</h3>
        <p>order.created -> pay.success</p>
      </article>
      <article>
        <h3>会员额度</h3>
        <p>FREE 20 / VIP 50 / SVIP 100</p>
      </article>
    </div>

    <div ref="chartRef" class="chart-box mt12"></div>
  </section>
</template>

<script setup>
import * as echarts from "echarts";
import { computed, onBeforeUnmount, onMounted, ref } from "vue";
import { getUser } from "../utils/auth";

const chartRef = ref(null);
let chartInstance;

const roleText = computed(() => {
  const roles = getUser()?.roles;
  return Array.isArray(roles) && roles.length ? roles.join(",") : "USER";
});

onMounted(() => {
  if (!chartRef.value) {
    return;
  }
  chartInstance = echarts.init(chartRef.value);
  chartInstance.setOption({
    tooltip: { trigger: "axis" },
    legend: { data: ["会员上限", "当前使用"] },
    xAxis: { type: "category", data: ["FREE", "VIP", "SVIP"] },
    yAxis: { type: "value", minInterval: 10 },
    series: [
      {
        name: "会员上限",
        type: "bar",
        data: [20, 50, 100],
        itemStyle: { color: "#2563eb" }
      },
      {
        name: "当前使用",
        type: "line",
        smooth: true,
        data: [12, 18, 26],
        itemStyle: { color: "#0ea5e9" }
      }
    ]
  });
  window.addEventListener("resize", handleResize);
});

onBeforeUnmount(() => {
  window.removeEventListener("resize", handleResize);
  if (chartInstance) {
    chartInstance.dispose();
    chartInstance = null;
  }
});

function handleResize() {
  if (chartInstance) {
    chartInstance.resize();
  }
}
</script>
