# tic-tac-toe-online

A real-time online Tic-Tac-Toe game built with WebSockets.
The goal of this project was to understand and learn how to work with websockets and implement real-time communication between clients and servers.

## Implemented

- Real-time multiplayer gameplay
- Multiple concurrent games
- Player roles:
  - First player: **X**
  - Second player: **O**
  - Additional connections: **spectators**
- Turn validation enforced on the server
- Rematch support
- Reconnect handling
- Player display names
- In-game chat

## Tech Stack

**Backend:** Java, Quarkus, WebSockets  
**Frontend:** Vue 3, Vite, Tailwind CSS (using daisyUI)

## Start Application Locally

### Start the Backend (Quarkus)

From the project root (or backend directory):

```bash
./mvnw quarkus:dev
```

This will:
- Start the backend in development mode

Runs on:

```
http://localhost:8080
```

---

### Start the Frontend

Navigate to the frontend directory:

```bash
cd frontend
```

Install dependencies (first time only):

```bash
npm install
```

Start the development server:

```bash
npm run dev
```

The frontend will usually be available at:

```
http://localhost:5173
```

(Check terminal output for the exact URL.)


## Images

<img width="2560" height="1392" alt="1_welcome" src="https://github.com/user-attachments/assets/e645704b-5e0d-4ef8-9168-bc7ed38fd96a" />

<img width="2560" height="1392" alt="2_lobby" src="https://github.com/user-attachments/assets/99d32920-4930-4f10-becc-493d969391e5" />

<img width="2560" height="1392" alt="3_new_game" src="https://github.com/user-attachments/assets/1cc8c06c-a143-42ac-ab33-d6d2c2531e6f" />

<img width="2560" height="1392" alt="4_game_inprogress" src="https://github.com/user-attachments/assets/90a2599c-35fb-44b4-a863-4e7af0bb3a8f" />

<img width="2560" height="1392" alt="5_game_won" src="https://github.com/user-attachments/assets/6a07687f-0e20-4611-9e2d-1a2febc3bd3f" />
