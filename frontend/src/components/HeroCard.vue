<template>
  <div class="hero-card">
    <div class="card-header">
      <button class="fav-btn" :class="{ favd: hero.favorite }" @click="toggleFav" :title="hero.favorite ? '取消收藏' : '收藏'">
        {{ hero.favorite ? '★' : '☆' }}
      </button>
      <h3>{{ hero.name }}</h3>
      <span class="id">#{{ hero.id }}</span>
      <button class="export-btn" @click="exportHero" title="导出XLSX">📥</button>
    </div>
    <div class="card-grid">
      <div><strong>性别:</strong> {{ hero.sex }}</div>
      <div v-if="hero.age"><strong>年龄:</strong> {{ hero.age }}</div>
      <div><strong>阵营:</strong> {{ hero.camp }}</div>
      <div><strong>种族:</strong> {{ hero.race }}</div>
      <div><strong>职业:</strong> {{ hero.career }}</div>
      <div><strong>资金:</strong> {{ hero.money }} GP</div>
      <div v-if="hero.live"><strong>生活:</strong> {{ hero.live }}</div>
      <div><strong>属性总和:</strong> {{ hero.sum }}</div>
    </div>

    <div class="card-section">
      <strong>属性:</strong>
      <div class="attr-display">
        <span v-for="(v, i) in hero.attributes" :key="i">{{ attrNames[i] }}: {{ v }}</span>
      </div>
    </div>

    <div v-if="hero.save" class="card-section"><strong>豁免:</strong> {{ hero.save }}</div>
    <div v-if="hero.skills && hero.skills.length" class="card-section"><strong>技能:</strong> {{ hero.skills.join(', ') }}</div>
    <div v-if="hero.equips && hero.equips.length" class="card-section"><strong>装备:</strong> {{ hero.equips.join(', ') }}</div>
    <div v-if="hero.tools && hero.tools.length" class="card-section"><strong>工具:</strong> {{ hero.tools.join(', ') }}</div>
    <div v-if="hero.languages && hero.languages.length" class="card-section"><strong>语言:</strong> {{ hero.languages.join(', ') }}</div>
    <div v-if="hero.feats && hero.feats.length" class="card-section"><strong>专长:</strong> {{ hero.feats.join(', ') }}</div>
    <div v-if="hero.spells0 && hero.spells0.length" class="card-section"><strong>戏法:</strong> {{ hero.spells0.join(', ') }}</div>
    <div v-if="hero.spells1 && hero.spells1.length" class="card-section"><strong>1环法术:</strong> {{ hero.spells1.join(', ') }}</div>
    <div v-if="hero.spells2 && hero.spells2.length" class="card-section"><strong>2环法术:</strong> {{ hero.spells2.join(', ') }}</div>
    <div v-if="hero.pray" class="card-section"><strong>魔能祈唤:</strong> {{ hero.pray }}</div>
    <div v-if="hero.style" class="card-section"><strong>战斗风格:</strong> {{ hero.style }}</div>
    <div v-if="hero.superspell" class="card-section"><strong>超魔法:</strong> {{ hero.superspell }}</div>
    <div v-if="hero.fight" class="card-section"><strong>战技:</strong> {{ hero.fight }}</div>
    <div v-if="hero.other" class="card-section"><strong>其它:</strong> {{ hero.other }}</div>
  </div>
</template>

<script>
import { toggleFavorite } from '../api/index.js'

export default {
  props: { hero: Object, attrNames: Array },
  emits: ['favorite-toggled'],
  methods: {
    exportHero() {
      if (this.hero && this.hero.id != null) {
        window.open('/api/heroes/' + this.hero.id + '/export', '_blank')
      }
    },
    async toggleFav() {
      try {
        const resp = (await toggleFavorite(this.hero.id)).data
        this.hero.favorite = resp.favorite
        this.$emit('favorite-toggled', { id: this.hero.id, favorite: resp.favorite })
      } catch (e) {}
    }
  }
}
</script>

<style>
.hero-card { background: #16213e; padding: 16px; border-radius: 8px; border-left: 4px solid #c0a060; }
.card-header { display: flex; align-items: center; gap: 10px; margin-bottom: 10px; }
.card-header h3 { color: #c0a060; font-size: 1.15rem; flex: 1; }
.id { color: #666; font-size: 0.85rem; }
.export-btn { padding: 4px 8px; background: transparent; border: 1px solid #c0a060; color: #c0a060; border-radius: 4px; cursor: pointer; font-size: 0.9rem; }
.export-btn:hover { background: #c0a060; color: #1a1a2e; }
.fav-btn { padding: 2px 6px; background: transparent; border: none; color: #666; cursor: pointer; font-size: 1.2rem; line-height: 1; }
.fav-btn:hover { color: #e0a000; }
.fav-btn.favd { color: #e0a000; }
.card-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 5px; }
.card-grid div { padding: 3px 0; font-size: 0.95rem; }
.card-section { margin-top: 10px; padding-top: 8px; border-top: 1px solid #2a2a4e; font-size: 0.95rem; }
.card-section strong { color: #c0a060; }
.attr-display { display: flex; gap: 10px; flex-wrap: wrap; margin-top: 4px; }
.attr-display span { background: #1a1a2e; padding: 3px 8px; border-radius: 4px; font-size: 0.9rem; }
</style>
