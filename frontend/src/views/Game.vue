<template>
  <div class="min-h-screen flex flex-col items-center justify-center gap-6 bg-gray-900 text-gray-100">
    <h1 class="text-3xl font-bold mb-10">Tic Tac Toe</h1>

    <GameBoard
      :gameId="gameId"
      :status="status"
      :currentTurn="currentTurn"
      :board="board"
      @cell-click="handleClickCell"
    />

    <div class="flex gap-4">
      <button
        class="btn btn-secondary"
        @click="goHome"
      >
        Leave Game
      </button>
      <button
        class="btn btn-primary"
        @click="createRematch"
        :disabled="status === 'IN_PROGRESS'"
      >
        Rematch
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import GameBoard from '../components/GameBoard.vue'

const route = useRoute()
const router = useRouter()

const gameId = ref(route.params.gameId)
let socket = null

const board = ref([])
const currentTurn = ref(null)
const status = ref(null)

function setupSocketHandlers(ws, id) {
  ws.onopen = () => {
    console.log('WebSocket connected to', id)
  }
  ws.onmessage = (event) => {
    console.log('Received:', event.data)
    let gameState = JSON.parse(event.data)

    board.value = gameState.board
    currentTurn.value = gameState.currentTurn
    status.value = gameState.status
  }
  ws.onclose = () => {
    console.log('WebSocket closed')
  }
  ws.onerror = (e) => {
    console.error('WebSocket error', e)
  }
}

function connectSocket(id) {
  if (!id) return
  if (socket) {
    try { socket.close() } catch(e) {}
    socket = null
  }
  socket = new WebSocket(`ws://localhost:8080/game/${id}`)
  setupSocketHandlers(socket, id)
}

async function createRematch() {
  try {
    const res = await fetch('http://localhost:8080/game', { method: 'POST' })
    if (!res.ok) throw new Error(`HTTP ${res.status}`)
    const id = await res.text()
    
    // Clean up current socket
    if (socket) {
      socket.close()
      socket = null
    }
    
    // Navigate to new game
    router.push(`/game/${id}`)
  } catch (e) {
    console.error('Failed to create rematch', e)
    alert('Failed to create rematch: ' + e.message)
  }
}

function goHome() {
  router.push('/')
}

function handleClickCell(index) {
  if (status.value !== 'IN_PROGRESS') return
  if (board.value[index] !== null) return

  if (!socket || socket.readyState !== WebSocket.OPEN) {
    alert('Not connected to game server')
    return
  }

  socket.send(JSON.stringify({
    actionType: 'MAKE_MOVE',
    position: index
  }))
}

onMounted(() => {
  connectSocket(gameId.value)
})

onUnmounted(() => {
  if (socket) {
    socket.close()
    socket = null
  }
})
</script>

<style scoped>
</style>
