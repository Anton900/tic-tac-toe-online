# tic-tac-toe-online

A real-time online Tic-Tac-Toe game built with WebSockets.

## Implemented

- Real-time multiplayer gameplay
- Multiple concurrent games (`gameId`)
- Player roles:
  - First player: **X**
  - Second player: **O**
  - Additional connections: **spectators**
- Turn validation enforced on the server
- Vue + Tailwind CSS frontend with live updates

## Tech Stack

**Backend:** Java, Quarkus, WebSockets  
**Frontend:** Vue 3, Vite, Tailwind CSS

## Planned

- In-game chat
- Rematch support
- Reconnect handling
- Optional player display names
- Improved lobby / join flow
