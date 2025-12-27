<template>
  <div class="min-h-screen flex flex-col items-center justify-center gap-6 bg-gray-900 text-gray-100">
    <h1 class="text-5xl font-bold mb-10">Tic Tac Toe Online</h1>
    
    <p class="text-xl text-gray-300 mb-6">Hiya, <span class="font-bold text-yellow-400">{{ playerName }}</span>, create a new game or join an existing one to start playing!</p>

    <div class="flex flex-col gap-4 items-center">
      <button
        class="btn btn-primary btn-lg"
        @click="createGame"
      >
        Create New Game
      </button>

      <div class="divider">OR</div>

      <div class="flex gap-2">
        <input 
          v-model="gameIdInput" 
          type="text" 
          placeholder="Enter game ID" 
          class="input input-bordered w-64" 
          @keyup.enter="joinGame"
        />
        <button
          class="btn btn-secondary"
          @click="joinGame"
          :disabled="!gameIdInput.trim()"
        >
          Join Game
        </button>
      </div>

    <div class="mt-10">
        <button
          class="btn btn-accent"
          style="opacity:0.7"
          @click="goBack"
        >
          Enter new player name
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const gameIdInput = ref('')
const playerName = sessionStorage.getItem('playerName') || 'Guest'

const BACKEND_URL = 'http://localhost:8080'
const WS_TIMEOUT = 5000

async function createGame() {
  try {
    const res = await fetch(`${BACKEND_URL}/game/createGameId`, { method: 'GET' })
    if (!res.ok) throw new Error(`HTTP ${res.status}`)
    const id = await res.text()
    
    sessionStorage.setItem('initActionType', 'CREATE_GAME')
    router.push(`/game/${id}`)
  } catch (e) {
    console.error('Failed to create game:', e)
    alert('Failed to create game: ' + e.message)
  }
}

async function joinGame() {
  const id = gameIdInput.value.trim()
  if (!id) {
    alert('Enter a game ID to join')
    return
  }

  try {
    await validateGameExists(id)
    sessionStorage.setItem('initActionType', 'JOIN_GAME')
    router.push(`/game/${id}`)
  } catch (e) {
    console.error('Failed to join game:', e)
    alert('Failed to join game: ' + e.message)
  }
}

function validateGameExists(gameId) {
  return new Promise((resolve, reject) => {
    const ws = new WebSocket(`${BACKEND_URL.replace('http', 'ws')}/game/${gameId}`)
    
    const timeout = setTimeout(() => {
      cleanup()
      reject(new Error('Connection timeout'))
    }, WS_TIMEOUT)

    const cleanup = () => {
      clearTimeout(timeout)
      ws.close()
    }

    ws.onopen = () => {
      ws.send(JSON.stringify({
        actionType: 'JOIN_GAME',
        gameId
      }))
    }

    ws.onmessage = (event) => {
      const message = JSON.parse(event.data)
      cleanup()
      
      if (message.type === 'ERROR') {
        reject(new Error(message.errorMessage || 'Game not found'))
      } else if (message.type === 'GAME_STATE') {
        resolve()
      } else {
        reject(new Error('Unexpected response from server'))
      }
    }

    ws.onerror = () => {
      cleanup()
      reject(new Error('Failed to connect to game server'))
    }
  })
}

function goBack() {
  router.push('/')
}
</script>

<style scoped>
</style>
