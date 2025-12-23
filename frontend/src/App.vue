<template>
  <div class="min-h-screen flex flex-col items-center justify-center gap-6 bg-gray-900 text-gray-100">

    <h1 class="text-3xl font-bold mb-10">Tic Tac Toe</h1>

    <GameBoard v-if="gameId"
      :gameId="gameId"
      :status="status"
      :currentTurn="currentTurn"
      :board="board"
      @cell-click="handleClickCell"
    />

    <div class="flex gap-4">
      <button
        class="btn btn-primary"
        @click="createGame"
      >
        Create new game
      </button>
      <button v-if="gameId"
        class="btn btn-secondary"
      >
        Rematch
      </button>
    </div>

    <div class="flex gap-2">
      <input v-model="gameIdInput" type="text" placeholder="Enter game id here" class="input" />
      <button
        class="btn btn-secondary"
        @click="joinGame"
      >
        Join game
      </button>
    </div>

  </div>
</template>

<script setup>
import { ref } from 'vue'
import { onMounted } from 'vue'
import GameBoard from './components/GameBoard.vue'

const gameId = ref('')
const gameIdInput = ref('')
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

    board.value = gameState.board;
    currentTurn.value = gameState.currentTurn;
    status.value = gameState.status;
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

async function createGame() {
  try {
    const res = await fetch('http://localhost:8080/game')
    if (!res.ok) throw new Error(`HTTP ${res.status}`)
    // backend returns a string like "ABCDE"
    const id = await res.text()
    gameId.value = id
    connectSocket(id)
  } catch (e) {
    console.error('Failed to create game', e)
    alert('Failed to create game: ' + e.message)
  }
}

function joinGame() {
  const id = gameIdInput.value && gameIdInput.value.trim()
  if (!id) {
    alert('Enter a game id to join')
    return
  }
  gameId.value = id
  connectSocket(id)
  gameIdInput.value = ''
}

onMounted(() => {
  // no default socket; user will create or join
})

function handleClickCell(index)
{
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

</script>

<style scoped>
</style>
