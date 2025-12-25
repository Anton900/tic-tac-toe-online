<template>
  <div class="min-h-screen flex flex-col items-center justify-center gap-6 bg-gray-900 text-gray-100">
    <h1 class="text-5xl font-bold mb-10">Tic Tac Toe Online</h1>
    
      <div class="flex gap-2">
        <input 
          v-model="playerNameInput" 
          type="text" 
          placeholder="Enter player name" 
          class="input input-bordered w-64" 
          @keyup.enter="goToLobby"
        />
        <button
          class="btn btn-primary"
          @click="goToLobby"
          :disabled="!playerNameInput.trim()"
        >
          Enter
        </button>
      </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const playerNameInput = ref('')

function goToLobby() {
  const playerName = playerNameInput.value.trim()
  if (!playerName) {
    alert('Enter a player name to proceed')
    return
  }
  router.push(`/CreateJoin`)
  sessionStorage.setItem('playerName', playerName)
}
</script>

<style scoped>
</style>
