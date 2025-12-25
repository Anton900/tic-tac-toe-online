import { createRouter, createWebHistory } from 'vue-router'
import Game from '../views/Game.vue'
import CreateJoin from '../views/CreateJoin.vue'
import Welcome from '../views/Welcome.vue'

const routes = [
  {
    path: '/',
    name: 'Welcome',
    component: Welcome
  },
  {
    path: '/createJoin',
    name: 'CreateJoin',
    component: CreateJoin
  },
  {
    path: '/game/:gameId',
    name: 'Game',
    component: Game
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
