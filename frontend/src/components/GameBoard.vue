<template>
  <div class="text-center space-y-1">
    <h2>Game ID: {{ gameId }}</h2>
    <p v-if="status !== 'IN_PROGRESS'"><span class="font-semibold">{{ status }}</span></p>
    <p v-else>
      Turn: <span class="font-semibold">{{ currentTurn }}</span>
    </p>

    <div v-if="board && board.length" class="grid grid-cols-3 gap-3">
      <button
        v-for="(cell, index) in board"
        :key="index"
        @click="onClick(index)"
        :disabled="cell !== null || status !== 'IN_PROGRESS'"
        class="
          w-20 h-20
          border border-gray-700
          flex items-center justify-center
          text-2xl font-bold
          bg-yellow-600 text-white
          hover:bg-yellow-500
          disabled:bg-yellow-500 disabled:cursor-not-allowed
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
})

const emit = defineEmits(['cell-click'])

function onClick(index) {
  emit('cell-click', index)
}
</script>

<style scoped>
</style>
