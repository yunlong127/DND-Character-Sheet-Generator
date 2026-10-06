<template>
  <div class="toolbar">
    <input v-model="searchText" placeholder="搜索名称/种族/职业..." class="search-input" @input="$emit('search', $event.target.value)" />
    <div class="sort-group">
      <button class="fav-filter-btn" :class="{ active: favOnly }" @click="toggleFavOnly" :title="favOnly ? '显示全部' : '只显示收藏'">
        {{ favOnly ? '★ 全部' : '☆ 收藏' }}
      </button>
      <select v-model="sortBy" @change="emitSort" class="sort-select">
        <option value="">默认排序</option>
        <option value="sum">总属性</option>
        <option value="str">力量</option>
        <option value="dex">敏捷</option>
        <option value="con">体质</option>
        <option value="int">智力</option>
        <option value="wis">感知</option>
        <option value="cha">魅力</option>
        <option value="money">金钱</option>
        <option value="age">年龄</option>
      </select>
      <button @click="toggleAsc" class="sort-btn" :title="asc ? '升序' : '降序'">
        {{ asc ? '↑' : '↓' }}
      </button>
    </div>
  </div>
</template>

<script>
export default {
  emits: ['sort-change', 'fav-change'],
  data() {
    return { searchText: '', sortBy: '', asc: false, favOnly: false }
  },
  methods: {
    emitSort() {
      this.$emit('sort-change', { sort: this.sortBy, asc: this.asc })
    },
    toggleAsc() {
      this.asc = !this.asc
      this.emitSort()
    },
    toggleFavOnly() {
      this.favOnly = !this.favOnly
      this.$emit('fav-change', this.favOnly)
    }
  }
}
</script>

<style>
.toolbar { display: flex; gap: 10px; align-items: center; margin-bottom: 12px; flex-wrap: wrap; }
.search-input { flex: 1; min-width: 180px; padding: 6px 10px; background: #1a1a2e; border: 1px solid #444; color: #e0e0e0; border-radius: 4px; }
.sort-group { display: flex; gap: 4px; align-items: center; }
.sort-select { padding: 6px 10px; background: #1a1a2e; border: 1px solid #444; color: #e0e0e0; border-radius: 4px; }
.sort-btn { padding: 6px 12px; background: #c0a060; color: #1a1a2e; border: none; border-radius: 4px; cursor: pointer; font-size: 1rem; }
.sort-btn:hover { background: #d4b870; }
.fav-filter-btn { padding: 6px 12px; background: transparent; border: 1px solid #444; color: #888; border-radius: 4px; cursor: pointer; font-size: 0.9rem; }
.fav-filter-btn:hover { border-color: #e0a000; color: #e0a000; }
.fav-filter-btn.active { border-color: #e0a000; color: #e0a000; background: #1a1a2e; }
</style>
