# Tic Tac Toe Online - Frontend

## Overview
Vue 3 SPA for an online multiplayer tic-tac-toe game. Uses WebSocket for real-time gameplay with a backend server (assumed at `localhost:8080`).

## Tech Stack
- **Vue 3** with Composition API (`<script setup>` SFC syntax)
- **Vite** for dev server and builds
- **Tailwind CSS v4** + **DaisyUI** for styling
- **WebSocket** for game state synchronization

## Architecture
- **Single root component**: [src/App.vue](src/App.vue) manages game lifecycle, WebSocket connection, and state
- **Presentation component**: [src/components/GameBoard.vue](src/components/GameBoard.vue) renders the 3x3 grid and emits click events
- **State management**: Reactive refs in `App.vue` (`board`, `currentTurn`, `status`, `gameId`)
- **Communication**: Parent passes props down to `GameBoard`, receives `cell-click` events up

## WebSocket Protocol
- **Connect**: `ws://localhost:8080/game/{gameId}` 
- **Server → Client**: Full game state JSON (`{ board: [], currentTurn: "X"|"O", status: "WAITING"|"IN_PROGRESS"|"X_WINS"|etc }`)
- **Client → Server**: Move action JSON (`{ actionType: "MAKE_MOVE", position: 0-8 }`)
- See [App.vue](src/App.vue#L48-L63) for connection setup and message handling

## Key Workflows
**Development**: `npm run dev` (Vite dev server, likely port 5173)
**Build**: `npm run build` → `dist/`
**Preview**: `npm run preview` (serves production build)

## Component Patterns
- Use Vue 3 `<script setup>` for all components
- Props use `defineProps()` with type/default objects: `{ type: String, default: '' }`
- Events use `defineEmits()` and `emit('event-name', payload)`
- Reactive state with `ref()` from `vue`

## Styling Conventions
- Tailwind utility classes directly in templates
- DaisyUI components: `btn`, `btn-primary`, `btn-secondary`, `input`
- Dark theme colors: `bg-gray-900`, `text-gray-100`, `border-gray-700`
- Game cells use `bg-yellow-600` with `hover:bg-yellow-500`

## Known Issues
- There's a duplicate function definition in [App.vue](src/App.vue#L106-L119) (stray `createRematchMatch` implementation with `handleClickCell` logic)
- Backend URL hardcoded to `localhost:8080` (no environment config)
- No error handling for WebSocket disconnections or failed moves

## Backend Integration
- **Create game**: `POST http://localhost:8080/game` → returns plain text gameId (e.g., "ABCDE")
- **Join game**: Connect WebSocket with existing gameId
- Assumes backend validates moves and broadcasts state to all connected players
