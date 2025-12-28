# tic-tac-toe-online

A real-time online Tic-Tac-Toe game built with WebSockets.
The goal of this project was to understand and learn how to work with websockets and implement real-time communication between clients and servers.

## Implemented

- Real-time multiplayer gameplay
- Multiple concurrent games (`gameId`)
- Player roles:
  - First player: **X**
  - Second player: **O**
  - Additional connections: **spectators**
- Turn validation enforced on the server
- Vue + Tailwind CSS frontend with live updates
- Rematch support
- Reconnect handling
- Player display names

## Tech Stack

**Backend:** Java, Quarkus, WebSockets  
**Frontend:** Vue 3, Vite, Tailwind CSS

## Planned

- In-game chat
