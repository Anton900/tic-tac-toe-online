<template>
  <div class="text-center space-y-4">
    <div class="mb-6">

      <div>
        <!-- Player Display -->
        <div class="flex justify-center gap-8 my-4">
          <div class="px-4 py-2 rounded-lg" :class="currentTurn === 'X' ? 'bg-yellow-600' : 'bg-gray-700'">
            <p class="text-sm text-gray-300">Mark X</p>
            <p class="font-bold text-lg">{{ playerX || 'Guest' }}</p>
          </div>
          <div class="px-4 py-2 rounded-lg" :class="currentTurn === 'O' ? 'bg-yellow-600' : 'bg-gray-700'">
            <p class="text-sm text-gray-300">Mark O</p>
            <p class="font-bold text-lg">{{ playerO || 'Guest' }}</p>
          </div>
        </div>
      </div>
      <!-- Game Status -->
      <div class="mt-4">
        <p v-if="status == 'DRAW'" class="text-2xl font-bold text-yellow-400">{{ status }}</p>
        <p v-if="status == 'X_WON'" class="text-2xl font-bold text-yellow-400">{{ playerX }} won!</p>
        <p v-if="status == 'O_WON'" class="text-2xl font-bold text-yellow-400">{{ playerO }} won!</p>
      </div>
    </div>

    <!-- Game Board -->
    <div v-if="board && board.length" class="grid grid-cols-3 gap-3 mx-auto" style="max-width: 300px;">
      <button
        v-for="(cell, index) in board"
        :key="index"
        @click="onClick(index)"
        :disabled="cell !== null || status !== 'IN_PROGRESS'"
        class="
          w-20 h-20
          border-2 border-gray-700
          flex items-center justify-center
          text-3xl font-bold
          bg-yellow-600 text-white
          hover:bg-yellow-500
          disabled:bg-yellow-700 disabled:cursor-not-allowed
          transition-colors
          rounded
        "
      >
        {{ cell }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue'

const props = defineProps({
  gameId: { type: String, default: '' },
  status: { type: String, default: null },
  currentTurn: { type: String, default: null },
  board: { type: Array, default: () => [] },
  playerX: { type: String, default: '' },
  playerO: { type: String, default: '' },
})

const emit = defineEmits(['cell-click'])

function onClick(index) {
  emit('cell-click', index)
}

function pickPlayer(player) {
  emit('cell-click', { pickPlayer: player })
}
</script>

<style scoped>
</style>
