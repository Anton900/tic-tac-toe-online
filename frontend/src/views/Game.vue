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
      >
        Rematch
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import GameBoard from '../components/GameBoard.vue'

const route = useRoute()
const router = useRouter()

const gameId = ref(route.params.gameId)
let InitActionType = sessionStorage.getItem('initActionType') ?? 'RECONNECT'
let playerName = sessionStorage.getItem('playerName')
const BACKEND_URL = 'http://localhost:8080'
let socket = null
let oldGameId = null
let reconnectToken = sessionStorage.getItem('reconnectToken') ?? ''

const board = ref([])
const currentTurn = ref(null)
const status = ref(null)
const playerX = ref('')
const playerO = ref('')

function setupSocketHandlers(ws, id) {
  ws.onopen = () => {
    console.log('ONOPEN WebSocket connected to', ws.url)

     ws.send(JSON.stringify({
        actionType: 'CONNECT',
        displayName: playerName,
        reconnectToken: reconnectToken,
      }))

    if(InitActionType)
    {
      console.log("InitActionType:", InitActionType)
      ws.send(JSON.stringify({
        actionType: InitActionType,
        gameId: id,
        previousGameId: InitActionType === 'CREATE_REMATCH' ? oldGameId : null,
        reconnectToken: reconnectToken
      }))
    sessionStorage.setItem('initActionType', 'RECONNECT')
    }
    console.log("ON OPEN: RECONNECTTOKEN:", reconnectToken)
  }
  ws.onmessage = (event) => {
    console.log('ONMESSAGE WebSocket message received,', ws.url)

    const message = JSON.parse(event.data)

    switch (message.type) {
      case 'CONNECTED':
        reconnectToken = message.reconnectToken
        sessionStorage.setItem('reconnectToken', reconnectToken)
        break
      case 'GAME_STATE':
        updateGameState(message.gameState)
        break
      case 'REMATCH_CREATED':
        router.push(`/game/${message.rematchGameId}`)
        break
      case 'ERROR':
        alert('Error from server: ' + message.errorMessage)
        goBack()
        break
      default:
        console.warn('Unknown type from server:', message.type)
    }
  }
  ws.onclose = () => {
    console.log('ONCLOSE WebSocket closed:', ws.url)
  }
  ws.onerror = (e) => {
    console.error('ONERROR WebSocket error', e)
  }
}

function updateGameState(newState) {
  board.value = newState.board
  currentTurn.value = newState.currentTurn
  status.value = newState.status
  playerX.value = newState.playerX
  playerO.value = newState.playerO
}

function connectSocket(id) {
  console.log("Attempting to connect new socket with gameId:", id)
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
    const res = await fetch(`${BACKEND_URL}/game/createGameId`, { method: 'GET' })
    if (!res.ok) throw new Error(`HTTP ${res.status}`)
    const id = await res.text()

    oldGameId = gameId.value

    // Clean up current socket and wait for it to close
    if (socket) {
      await new Promise((resolve) => {
        if (socket.readyState === WebSocket.CLOSED) {
          resolve()
        } else {
          socket.onclose = () => resolve()
          socket.close()
        }
      })
      socket = null
    }

    // Navigate to new game with CREATE_REMATCH action
    sessionStorage.setItem('initActionType', 'CREATE_REMATCH')
    router.push(`/game/${id}`)
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

// Watch for route param changes (e.g., rematch navigation)
watch(() => route.params.gameId, (newGameId) => {
  if (newGameId && newGameId !== gameId.value) {
    console.log('Game ID changed from', gameId.value, 'to', newGameId)
    gameId.value = newGameId
    InitActionType = sessionStorage.getItem('initActionType') ?? 'RECONNECT'
    connectSocket(newGameId)
  }
})
</script>

<style scoped>
</style>
