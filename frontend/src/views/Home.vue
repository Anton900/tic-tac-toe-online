<template>
  <div class="min-h-screen flex flex-col items-center justify-center gap-6 bg-gray-900 text-gray-100">
    <h1 class="text-5xl font-bold mb-10">Tic Tac Toe Online</h1>
    
    <p class="text-xl text-gray-300 mb-6">Play tic-tac-toe with friends in real-time!</p>

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
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const gameIdInput = ref('')

async function createGame() {
  try {
    const res = await fetch('http://localhost:8080/game', { method: 'GET' })
    if (!res.ok) throw new Error(`HTTP ${res.status}`)
    const id = await res.text()
    router.push(`/game/${id}`)
  } catch (e) {
    console.error('Failed to create game', e)
    alert('Failed to create game: ' + e.message)
  }
}

function joinGame() {
  const id = gameIdInput.value.trim()
  if (!id) {
    alert('Enter a game ID to join')
    return
  }
  router.push(`/game/${id}`)
}
</script>

<style scoped>
</style>
