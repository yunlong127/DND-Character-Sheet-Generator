import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 30000
})

export function generateCharacter(params) {
  return api.post('/generate', params)
}

export function listHeroes(params = {}) {
  return api.get('/heroes', { params })
}

export function getHeroStats(params = {}) {
  return api.get('/heroes/stats', { params })
}

export function startWizard(params) {
  return api.post('/wizard/start', params)
}

export function wizardChoose(params) {
  return api.post('/wizard/choose', params)
}

export function pingServer() {
  return api.get('/ping')
}

export function toggleFavorite(id) {
  return api.post('/heroes/' + id + '/favorite')
}
