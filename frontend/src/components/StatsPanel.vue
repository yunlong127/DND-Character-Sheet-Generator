<template>
  <div class="stats-panel" v-if="stats">
    <!-- 阵营统计 -->
    <div class="stats-section">
      <h3>阵营分布</h3>
      <table class="align-table">
        <thead>
          <tr><th></th><th>守序</th><th>中立</th><th>混乱</th><th>总和</th></tr>
        </thead>
        <tbody>
          <tr v-for="(row, cat) in stats.alignmentStats" :key="cat">
            <td><strong>{{ cat }}</strong></td>
            <td>{{ row.守序 }}</td>
            <td>{{ row.中立 }}</td>
            <td>{{ row.混乱 }}</td>
            <td><strong>{{ row.总和 }}</strong></td>
          </tr>
          <tr class="total-row">
            <td><strong>总和</strong></td>
            <td>{{ totalCol('守序') }}</td>
            <td>{{ totalCol('中立') }}</td>
            <td>{{ totalCol('混乱') }}</td>
            <td><strong>{{ totalAll }}</strong></td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 属性统计 -->
    <div class="stats-section">
      <h3>属性统计</h3>
      <table class="attr-table">
        <thead>
          <tr><th>属性</th><th>平均值</th><th>最高</th><th>最低</th></tr>
        </thead>
        <tbody>
          <tr v-for="a in stats.attributeStats" :key="a.name" :class="{ highlight: a.name === '总属性' }">
            <td>{{ a.name }}</td>
            <td>{{ a.avg.toFixed(1) }}</td>
            <td>{{ a.max }}</td>
            <td>{{ a.min }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 职业统计 -->
    <div class="stats-section">
      <h3>职业分布</h3>
      <div class="tag-list">
        <span v-for="(cnt, name) in stats.careerStats" :key="name" class="tag">
          {{ name }}: {{ cnt }}
        </span>
      </div>
    </div>

    <!-- 种族统计 -->
    <div class="stats-section">
      <h3>种族分布</h3>
      <div class="tag-list">
        <span v-for="(cnt, name) in stats.raceStats" :key="name" class="tag">
          {{ name }}: {{ cnt }}
        </span>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  props: { stats: Object },
  computed: {
    totalAll() {
      if (!this.stats) return 0
      return Object.values(this.stats.alignmentStats).reduce((s, r) => s + (r.总和 || 0), 0)
    }
  },
  methods: {
    totalCol(col) {
      if (!this.stats) return 0
      return Object.values(this.stats.alignmentStats).reduce((s, r) => s + (r[col] || 0), 0)
    }
  }
}
</script>

<style>
.stats-panel { background: #16213e; padding: 16px; border-radius: 8px; margin-bottom: 16px; }
.stats-section { margin-bottom: 16px; }
.stats-section h3 { color: #c0a060; margin-bottom: 8px; font-size: 1rem; }
.align-table, .attr-table { width: 100%; border-collapse: collapse; }
.align-table th, .align-table td, .attr-table th, .attr-table td {
  padding: 4px 8px; text-align: center; border: 1px solid #2a2a4e;
}
.align-table th, .attr-table th { background: #1a1a2e; color: #c0a060; }
.total-row td { font-weight: bold; background: #1a1a2e; }
.highlight td { background: #1e1e3a; font-weight: bold; }
.tag-list { display: flex; flex-wrap: wrap; gap: 6px; }
.tag { background: #1a1a2e; padding: 4px 10px; border-radius: 4px; font-size: 0.85rem; }
</style>
