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
- Rematch support
- Reconnect handling
- Player display names

## Tech Stack

**Backend:** Java, Quarkus, WebSockets  
**Frontend:** Vue 3, Vite, Tailwind CSS

## Planned

- In-game chat

## Start the Backend (Quarkus)

From the project root (or backend directory):
mvn quarkus:dev

This will:
Start the backend in development mode
Enable hot reload

Run on:
http://localhost:8080

## Start the Frontend

Navigate to the frontend directory:
- cd frontend

Install dependencies (first time only):
- npm install

Start the development server:
- npm run dev

The frontend will usually be available at:
- http://localhost:5173

(Check terminal output for the exact URL.)

## Images

<img width="1097" height="805" alt="page1_enterPlayerName" src="https://github.com/user-attachments/assets/f981142f-5e77-4ae8-aab6-501116d9ef5a" />

<img width="1040" height="738" alt="page2_lobby" src="https://github.com/user-attachments/assets/da537199-2144-432a-9067-0a5da7f06ee0" />

<img width="815" height="782" alt="page3_new_game_created" src="https://github.com/user-attachments/assets/b8133b66-7082-4887-9c77-3dc986605bb1" />

<img width="724" height="772" alt="page4_game_in_progress" src="https://github.com/user-attachments/assets/8268de90-8d17-46c4-b08b-876d1a8f9beb" />

<img width="709" height="786" alt="page5_game_won" src="https://github.com/user-attachments/assets/db031079-7294-4265-813a-091f0511f8f2" />


