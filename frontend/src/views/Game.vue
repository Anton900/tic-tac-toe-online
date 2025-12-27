<template>
  <div class="min-h-screen flex flex-col items-center justify-center gap-6 bg-gray-900 text-gray-100">
    <h1 class="text-3xl font-bold">Tic Tac Toe</h1>
    <h2 class="text-xl font-semibold mb-2">Game ID: <span class="text-yellow-400">{{ gameId }}</span></h2>

    <GameBoard
      :gameId="gameId"
      :status="status"
      :currentTurn="currentTurn"
      :board="board"
      :playerX="playerX"
      :playerO="playerO"
      @cell-click="handleClickCell"
    />

    <div class="flex gap-4">
      <button
        class="btn btn-secondary"
        @click="goBack"
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
const InitActionType = sessionStorage.getItem('initActionType') ?? 'RECONNECT'
let socket = null

const board = ref([])
const currentTurn = ref(null)
const status = ref(null)
const playerX = ref('')
const playerO = ref('')

function setupSocketHandlers(ws, id) {
  ws.onopen = () => {
    console.log('WebSocket connected to', id)

    console.log("InitActionType:", InitActionType.value)
    ws.send(JSON.stringify({
        actionType: InitActionType,
        gameId: id
    }))

    sessionStorage.setItem('initActionType', 'RECONNECT')

  }
  ws.onmessage = (event) => {
    console.log('Received:', event.data)
    const message = JSON.parse(event.data)

    console.log("Message from backend:", message)
    console.log("Message from backend type:", message.type)
    switch (message.type) {
      case 'GAME_STATE':
        updateGameState(message.gameState)
        break
      case 'ERROR':
        alert('Error from server: ' + message.message)
        break
      default:
        console.warn('Unknown type from server:', message.type)
    }

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

function updateGameState(newState) {
  board.value = newState.board
  currentTurn.value = newState.currentTurn
  status.value = newState.status
  playerX.value = newState.playerX || ''
  playerO.value = newState.playerO || ''
}

function connectSocket(id) {
  if (!id) return
  if (socket) {
    try { socket.close() } catch(e) {}
    socket = null
  }
  try
  {
      socket = new WebSocket(`ws://localhost:8080/game/${id}`)
      setupSocketHandlers(socket, id)
  } catch(e) {
    console.error('Failed to connect WebSocket', e)
    alert('Failed to connect to game server: ' + e.message)
  }
}

async function createRematch() {
  try {
    const res = await fetch('http://localhost:8080/game/createGameId', { method: 'GET' })
    if (!res.ok) throw new Error(`HTTP ${res.status}`)
    const id = await res.text()
    
    // Clean up current socket
    if (socket) {
      socket.close()
      socket = null
    }
    
    // Navigate to new game with CREATE_GAME action
    router.push({ path: `/game/${id}`, state: { actionType: 'CREATE_GAME' } })
  } catch (e) {
    console.error('Failed to create rematch', e)
    alert('Failed to create rematch: ' + e.message)
  }
}

function goBack() {
  router.push('/CreateJoin')
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
    position: index,
    gameId: gameId.value
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
