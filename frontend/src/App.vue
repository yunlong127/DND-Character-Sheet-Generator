<template>
  <div class="app">
    <h1>D&D 角色生成器</h1>

    <div class="tab-bar">
      <button :class="{ active: tab === 'generate' }" @click="tab = 'generate'">生成角色</button>
      <button :class="{ active: tab === 'library' }" @click="tab = 'library'">角色库</button>
    </div>

    <!-- ========== 生成角色 ========== -->
    <div v-if="tab === 'generate'">
      <div class="mode-switch">
        <button :class="{ active: mode === 'random' }" @click="mode = 'random'">随机生成</button>
        <button :class="{ active: mode === 'manual' }" @click="mode = 'manual'">手动创建</button>
      </div>

      <div class="settings">
        <div class="setting-row">
          <label>属性公式:</label>
          <input v-model="formula" placeholder="5d6d1d1" />
        </div>
        <div class="setting-row">
          <label>背景:</label>
          <select v-model="background">
            <option value="背景">背景</option>
            <option value="背景-强化">背景-强化</option>
          </select>
        </div>
      </div>

      <div v-if="mode === 'random'" class="mode-panel">
        <div class="setting-row">
          <label>生成数量:</label>
          <input v-model.number="count" type="number" min="1" max="20" />
        </div>
        <button class="btn-generate" @click="generateRandom" :disabled="generating">
          {{ generating ? '生成中...' : '随机生成' }}
        </button>
      </div>

      <div v-if="mode === 'manual'" class="mode-panel">
        <!-- Wizard steps -->
        <div v-if="wizardStep && wizardStep.stepType !== 'final' && wizardStep.stepType !== 'error'" class="wizard-step">
          <div class="step-title">✦ {{ wizardStep.stepTitle }}</div>
          <div v-if="wizardStep.rolledAttributes" class="step-attrs">
            属性：<span v-for="(v,i) in wizardStep.rolledAttributes" :key="i">{{ attrNames[i] }}{{v}}</span>
            <strong>总和 {{ wizardStep.attrsSum }}</strong>
          </div>
          <div v-if="wizardStep.attrsOptions && wizardStep.attrsOptions.length" class="attrs-grid">
            <div v-for="(opts, idx) in wizardStep.attrsOptions" :key="idx"
              :class="['attr-option', { selected: selectedCands[0] == String(idx) }]"
              @click="pickAttrSet(idx)"
            >
              <div class="attr-label">第 {{ idx + 1 }} 组 <strong>总和 {{ opts.reduce((a,b) => a+b, 0) }}</strong></div>
              <div class="attr-vals">
                <span v-for="(v,i) in opts" :key="i">{{ attrNames[i] }}{{v}}</span>
              </div>
            </div>
          </div>
          <div v-else-if="wizardStep.selectCount > 1" class="step-hint">请选择 {{ wizardStep.selectCount }} 项</div>
          <div v-if="wizardStep.stepType === 'pick_attrs_input'" class="attrs-input-grid">
            <div v-for="(n,i) in attrNames" :key="i" class="attr-input-row">
              <label>{{ n }}</label>
              <input v-model="attrInputs[i]" type="text" class="attr-val-input" />
            </div>
            <button class="btn-generate" @click="submitAttrInput">确认属性</button>
          </div>
          <div v-if="wizardStep.stepType === 'pick_money'" class="free-input-row">
            <input v-model="freeInputText" :placeholder="wizardStep.stepTitle" class="free-input" @keydown.enter="wizardNext" />
            <button class="btn-generate" @click="wizardNext">确认</button>
          </div>
          <div class="candidate-grid" v-if="wizardStep.candidates && wizardStep.candidates.length && !(wizardStep.attrsOptions && wizardStep.attrsOptions.length) && wizardStep.selectCount !== -1 && wizardStep.stepType !== 'pick_money' && wizardStep.stepType !== 'pick_attrs_input' && wizardStep.stepType !== 'pick_age'">
            <button
              v-for="c in wizardStep.candidates" :key="c"
              :class="['candidate-btn', { selected: selectedCands.includes(c) }]"
              @click="toggleCandidate(c)"
            >{{ c }}</button>
          </div>
          <div v-if="wizardStep.stepType === 'pick_age'" class="free-input-row">
            <span>年:</span> <input v-model="ageYearsInput" type="text" class="free-input" style="width:80px" @keydown.enter="wizardNext" />
            <span>月:</span> <input v-model="ageMonthsInput" type="text" class="free-input" style="width:60px" @keydown.enter="wizardNext" />
            <button class="btn-generate" @click="wizardNext">确认</button>
          </div>
          <div v-else-if="wizardStep.selectCount === -1" class="free-input-row">
            <input v-model="freeInputText" :placeholder="wizardStep.stepTitle" class="free-input" @keydown.enter="wizardNext" />
            <button class="btn-generate" @click="wizardNext">确认</button>
          </div>
          <button v-else-if="wizardStep.stepType !== 'pick_age' && wizardStep.stepType !== 'pick_money' && wizardStep.stepType !== 'pick_attrs_input' && wizardStep.selectCount !== -1" class="btn-generate" @click="wizardNext" :disabled="wizardStep.selectCount > 0 && selectedCands.length < wizardStep.selectCount">
            {{ selectedCands.length >= (wizardStep.selectCount||1) ? '确认 ('+selectedCands.length+'/'+(wizardStep.selectCount||1)+')' : '请选择' }}
          </button>
        </div>
        <div v-else-if="wizardStep && wizardStep.stepType === 'final'" class="wizard-step">
          <div class="step-title">✦ 角色创建成功！</div>
          <HeroCard :hero="wizardStep.preview" :attr-names="attrNames" />
          <button class="btn-generate" @click="resetWizard">再次创建</button>
        </div>
        <div v-else-if="wizardStep && wizardStep.stepType === 'error'" class="wizard-step">
          <div class="step-title" style="color:#a55">创建失败: {{ wizardStep.description }}</div>
          <button class="btn-generate" @click="resetWizard">重新开始</button>
        </div>
        <div v-else>
          <div class="setting-row">
            <label>模式:</label>
            <select v-model="wizardMode" class="formula-select">
              <option value="roll">投骰</option>
              <option value="input">输入</option>
            </select>
          </div>
          <div v-if="wizardMode === 'roll'" class="setting-row">
            <label>重骰数量:</label>
            <input v-model.number="rollCount" type="number" min="1" max="10" />
          </div>
          <button class="btn-generate" @click="startWizardFlow" :disabled="wizardLoading">
            {{ wizardLoading ? '加载中...' : '开始创建角色' }}
          </button>
        </div>
      </div>

      <!-- Generate Result -->
      <div v-if="result">
        <div class="result-status" :class="{ success: result.success, fail: !result.success }">
          {{ result.message }}
        </div>
        <StatsPanel v-if="result.stats" :stats="result.stats" />
        <HeroToolbar v-if="result.heroes && result.heroes.length" @search="genSearch = $event" @sort-change="genSort = $event" />
        <div v-if="sortedGenHeroes.length" class="hero-list">
          <HeroCard v-for="h in sortedGenHeroes" :key="h.id" :hero="h" :attr-names="attrNames" />
        </div>
        <div v-if="mode === 'manual' && result.hero" class="hero-list">
          <HeroCard :hero="result.hero" :attr-names="attrNames" />
        </div>
      </div>
    </div>

    <!-- ========== 角色库 ========== -->
    <div v-if="tab === 'library'">
      <HeroToolbar @search="libSearch = $event" @sort-change="handleLibSort" @fav-change="handleFavChange" />
      <button class="btn-generate" @click="loadHeroes" style="margin-bottom:12px">刷新</button>

      <StatsPanel v-if="libStats" :stats="libStats" />

      <div v-if="sortedLibHeroes.length" class="hero-list">
        <HeroCard v-for="h in sortedLibHeroes" :key="h.id" :hero="h" :attr-names="attrNames" />
      </div>
      <div v-else-if="loaded" class="empty">暂无角色</div>
    </div>
  </div>
</template>

<script>
import { ref, computed, watch } from 'vue'
import { generateCharacter, listHeroes, getHeroStats, startWizard, wizardChoose } from './api/index.js'
import HeroCard from './components/HeroCard.vue'
import StatsPanel from './components/StatsPanel.vue'
import HeroToolbar from './components/HeroToolbar.vue'

export default {
  components: { HeroCard, StatsPanel, HeroToolbar },
  setup() {
    const tab = ref('generate')
    const mode = ref('random')
    const formula = ref('5d6d1d1')
    const background = ref('背景-强化')
    const count = ref(1)
    const generating = ref(false)
    const result = ref(null)
    const attrNames = ['力量','敏捷','体质','智力','感知','魅力']

    // Generate page search/sort
    const genSearch = ref('')
    const genSort = ref(null)
    const allGenHeroes = ref([])

    // Library page
    const libSearch = ref('')
    const libSort = ref(null)
    const libFavOnly = ref(false)
    const allLibHeroes = ref([])
    const libStats = ref(null)
    const loaded = ref(false)

    // Wizard state
    const wizardLoading = ref(false)
    const wizardStep = ref(null)
    const selectedCands = ref([])
    const freeInputText = ref('')
    const ageYearsInput = ref('')
    const ageMonthsInput = ref('')
    const attrInputs = ref(['','','','','',''])
    const wizardMode = ref('roll')
    const rollCount = ref(3)

    function filterAndSort(heroes, search, sort) {
      let list = [...heroes]
      if (search) {
        const s = search.toLowerCase()
        list = list.filter(h =>
          (h.name && h.name.toLowerCase().includes(s)) ||
          (h.race && h.race.toLowerCase().includes(s)) ||
          (h.career && h.career.toLowerCase().includes(s))
        )
      }
      if (sort && sort.sort) {
        const idxMap = { str:0, dex:1, con:2, int:3, wis:4, cha:5 }
        const key = sort.sort
        list.sort((a, b) => {
          let va, vb
          if (key === 'sum') { va = a.sum; vb = b.sum }
          else if (key === 'money') { va = a.money; vb = b.money }
          else if (key === 'age') { va = parseInt(a.age) || 0; vb = parseInt(b.age) || 0 }
          else { va = a.attributes[idxMap[key]]; vb = b.attributes[idxMap[key]] }
          return sort.asc ? va - vb : vb - va
        })
      }
      return list
    }

    const sortedGenHeroes = computed(() => filterAndSort(allGenHeroes.value, genSearch.value, genSort.value))
    const sortedLibHeroes = computed(() => filterAndSort(allLibHeroes.value, libSearch.value, libSort.value))

    async function generateRandom() {
      generating.value = true; result.value = null
      try {
        const resp = (await generateCharacter({ formula: formula.value, background: background.value, count: count.value })).data
        result.value = resp
        allGenHeroes.value = resp.heroes || []
      } catch (e) {
        result.value = { success: false, message: '请求失败' }
      } finally { generating.value = false }
    }

    // ---------- Wizard ----------
    async function startWizardFlow() {
      wizardLoading.value = true
      try {
        const resp = (await startWizard({ formula: formula.value, background: background.value, rollCount: rollCount.value, mode: wizardMode.value })).data
        wizardStep.value = resp
        selectedCands.value = []
        attrInputs.value = ['','','','','','']
      } catch (e) {
        wizardStep.value = { stepType: 'error', description: '请求失败' }
      } finally { wizardLoading.value = false }
    }

    async function submitAttrInput() {
      if (!wizardStep.value || !wizardStep.value.sessionId) return
      const vals = attrInputs.value.map(v => v.trim())
      if (vals.some(v => !/^\d+$/.test(v))) { alert('所有属性必须输入数字'); return }
      wizardLoading.value = true
      try {
        const resp = (await wizardChoose({ sessionId: wizardStep.value.sessionId, selected: [vals.join(',')] })).data
        wizardStep.value = resp
        selectedCands.value = []
        attrInputs.value = ['','','','','','']
      } catch (e) {
        wizardStep.value = { stepType: 'error', description: '请求失败' }
      } finally { wizardLoading.value = false }
    }

    async function wizardNext() {
      if (!wizardStep.value || !wizardStep.value.sessionId) return
      const isAgeStep = wizardStep.value.stepType === 'pick_age'
      if (isAgeStep) {
        const y = parseInt(ageYearsInput.value.trim())
        const m = parseInt(ageMonthsInput.value.trim())
        if (!/^\d+$/.test(ageYearsInput.value.trim()) || !/^\d+$/.test(ageMonthsInput.value.trim())) {
          alert('年龄必须输入数字')
          return
        }
        const min = wizardStep.value.ageMin || 0
        const max = wizardStep.value.ageMax || 1000
        if (y < min || y > max || (y === max && m > 0)) {
          alert('年龄需在 ' + min + ',0~' + max + ',0 之间')
          return
        }
        if (m < 0 || m > 11) {
          alert('月份需在 0~11 之间')
          return
        }
      }
      const isMoneyStep = wizardStep.value.stepType === 'pick_money'
      if (isMoneyStep && !/^\d+$/.test(freeInputText.value.trim())) {
        alert('金钱必须输入数字')
        return
      }
      wizardLoading.value = true
      try {
        const sel = isAgeStep ? [ageYearsInput.value.trim() + ',' + ageMonthsInput.value.trim()]
                  : isMoneyStep ? [freeInputText.value.trim()]
                  : (wizardStep.value.selectCount === -1 ? [freeInputText.value] : selectedCands.value)
        const resp = (await wizardChoose({ sessionId: wizardStep.value.sessionId, selected: sel })).data
        wizardStep.value = resp
        selectedCands.value = []
        freeInputText.value = ''
        ageYearsInput.value = ''
        ageMonthsInput.value = ''
      } catch (e) {
        wizardStep.value = { stepType: 'error', description: '请求失败' }
      } finally { wizardLoading.value = false }
    }

    function toggleCandidate(c) {
      if (!wizardStep.value) return
      const max = wizardStep.value.selectCount || 1
      const idx = selectedCands.value.indexOf(c)
      if (idx >= 0) selectedCands.value.splice(idx, 1)
      else if (selectedCands.value.length < max) selectedCands.value.push(c)
    }

    function pickAttrSet(idx) {
      selectedCands.value = [String(idx)]
    }

    function resetWizard() {
      wizardStep.value = null
      selectedCands.value = []
    }

    function handleLibSort(ev) { libSort.value = ev; loadHeroes() }
    function handleFavChange(v) { libFavOnly.value = v; loadHeroes() }

    async function loadHeroes() {
      loaded.value = false
      try {
        const params = {}
        if (libSort.value?.sort) { params.sort = libSort.value.sort; params.asc = libSort.value.asc }
        if (libSearch.value) params.search = libSearch.value
        if (libFavOnly.value) params.favoriteOnly = true
        const [heroesRes, statsRes] = await Promise.all([
          listHeroes(params),
          getHeroStats({ search: libSearch.value })
        ])
        allLibHeroes.value = heroesRes.data
        libStats.value = statsRes.data.stats
        loaded.value = true
      } catch (e) { loaded.value = true }
    }

    // Auto-load heroes when switching to library tab
    watch(tab, (v) => { if (v === 'library') loadHeroes() })

    return {
      tab, mode, formula, background, count, generating, result,
      attrNames,
      genSearch, genSort, sortedGenHeroes,
      libSearch, libStats, loaded, handleLibSort, handleFavChange, loadHeroes, sortedLibHeroes,
      generateRandom,
      wizardLoading, wizardStep, selectedCands, freeInputText, ageYearsInput, ageMonthsInput, wizardMode, attrInputs, startWizardFlow, submitAttrInput, wizardNext, toggleCandidate, pickAttrSet, resetWizard,
      rollCount
    }
  }
}
</script>

<style>
* { margin: 0; padding: 0; box-sizing: border-box; }
body { font-family: 'Segoe UI', sans-serif; background: #1a1a2e; color: #e0e0e0; min-height: 100vh; }
.app { max-width: 900px; margin: 0 auto; padding: 20px; }
h1 { text-align: center; color: #c0a060; margin-bottom: 16px; }
.tab-bar { display: flex; gap: 10px; margin-bottom: 16px; justify-content: center; }
.tab-bar button { padding: 8px 24px; border: 2px solid #c0a060; background: transparent; color: #c0a060; border-radius: 4px; cursor: pointer; font-size: 1rem; }
.tab-bar button.active { background: #c0a060; color: #1a1a2e; }
.mode-switch { display: flex; gap: 10px; margin-bottom: 12px; justify-content: center; }
.mode-switch button { padding: 6px 18px; border: 1px solid #666; background: transparent; color: #999; border-radius: 4px; cursor: pointer; }
.mode-switch button.active { border-color: #c0a060; color: #c0a060; }
.settings { background: #16213e; padding: 12px; border-radius: 8px; margin-bottom: 12px; }
.setting-row { display: flex; align-items: center; gap: 10px; margin-bottom: 8px; flex-wrap: wrap; }
.setting-row label { min-width: 70px; color: #c0a060; }
.setting-row input, .setting-row select { padding: 6px 10px; background: #1a1a2e; border: 1px solid #444; color: #e0e0e0; border-radius: 4px; }
.mode-panel { background: #16213e; padding: 12px; border-radius: 8px; margin-bottom: 12px; }
.btn-generate { display: block; width: 100%; padding: 10px; background: #c0a060; color: #1a1a2e; border: none; border-radius: 6px; font-size: 1.05rem; cursor: pointer; margin-top: 8px; }
.btn-generate:disabled { opacity: 0.5; cursor: not-allowed; }
.btn-generate:hover:not(:disabled) { background: #d4b870; }
.result-status { padding: 10px 16px; border-radius: 6px; margin-bottom: 12px; text-align: center; }
.result-status.success { background: #1a3a1a; color: #5a5; }
.result-status.fail { background: #3a1a1a; color: #a55; }
.hero-list { display: flex; flex-direction: column; gap: 12px; }
.empty { text-align: center; color: #666; padding: 40px; }

/* Wizard */
.wizard-step { }
.step-title { font-size: 1.1rem; color: #c0a060; margin-bottom: 10px; font-weight: bold; }
.step-attrs { margin-bottom: 10px; color: #aaa; font-size: 0.9rem; }
.step-attrs span { margin-right: 8px; background: #1a1a2e; padding: 2px 6px; border-radius: 3px; }
.step-hint { color: #888; font-size: 0.85rem; margin-bottom: 8px; }
.step-empty { color: #666; padding: 10px; text-align: center; }
.candidate-grid { display: flex; flex-wrap: wrap; gap: 6px; margin-bottom: 12px; }
.candidate-btn { padding: 6px 14px; background: #1a1a2e; border: 1px solid #444; color: #e0e0e0; border-radius: 4px; cursor: pointer; font-size: 0.9rem; }
.candidate-btn:hover { border-color: #c0a060; }
.candidate-btn.selected { background: #c0a060; color: #1a1a2e; border-color: #c0a060; }
.attrs-grid { display: flex; flex-direction: column; gap: 8px; margin-bottom: 12px; }
.attr-option { padding: 10px; background: #1a1a2e; border: 1px solid #444; border-radius: 6px; cursor: pointer; }
.attr-option:hover { border-color: #c0a060; }
.attr-option.selected { border-color: #c0a060; background: #2a2a4e; }
.attr-label { color: #c0a060; margin-bottom: 4px; font-size: 0.9rem; }
.attr-vals { display: flex; flex-wrap: wrap; gap: 6px; }
.attr-vals span { font-size: 0.85rem; color: #aaa; background: #111; padding: 2px 6px; border-radius: 3px; }
</style>
