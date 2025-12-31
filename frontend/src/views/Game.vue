<template>
  <div class="min-h-screen flex items-center justify-center gap-8 bg-gray-900 text-gray-100 p-4">
    <!-- Game Column -->
    <div class="flex flex-col items-center gap-6">
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
          :disabled="status === 'IN_PROGRESS' || status === 'WAITING_FOR_PLAYERS'"
        >
          Rematch
        </button>
      </div>
    </div>

    <div class="card bg-gray-800 w-80 h-[400px] flex flex-col mt-30">
      <div class="card-body p-4 flex flex-col h-full">
        <h3 class="card-title text-lg mb-3">Chat</h3>
        
        <div class="flex-1 overflow-y-auto space-y-1 mb-4 px-2" ref="chatContainer">
          <div
            v-for="msg in messages"
            :key="msg.id"
            class="py-1 text-sm"
          >
            <span class="font-semibold" :class="msg.isMe ? 'text-yellow-400' : 'text-blue-400'">{{ msg.displayName }}:</span>
            <span class="text-gray-200 ml-1">{{ msg.chatMessage }}</span>
          </div>
        </div>

        <div class="flex gap-2">
          <input
            v-model="chatMessage"
            @keyup.enter="sendMessage"
            type="text"
            placeholder="Type a message..."
            class="input input-bordered flex-1 bg-gray-700"
          />
          <button @click="sendMessage" class="btn btn-primary">Send</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
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

// Chat state
const messages = ref([])
const chatMessage = ref('')
const chatContainer = ref(null)

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
    console.log('Received message:', message)

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
      case 'CHAT_MESSAGE':
        messages.value.push({
          displayName: message.displayName,
          chatMessage: message.chatMessage,
          isMe: message.displayName === playerName
        })
        scrollToBottom()
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

function sendMessage() {
  if (!chatMessage.value.trim()) return
  if (!socket || socket.readyState !== WebSocket.OPEN) return

  socket.send(JSON.stringify({
    actionType: 'SEND_CHAT',
    gameId: gameId.value,
    chatMessage: chatMessage.value,
  }))

  chatMessage.value = ''
}

function scrollToBottom() {
  nextTick(() => {
    if (chatContainer.value) {
      chatContainer.value.scrollTop = chatContainer.value.scrollHeight
    }
  })
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
