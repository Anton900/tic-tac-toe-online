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

## Tech Stack

**Backend:** Java, Quarkus, WebSockets  
**Frontend:** Vue 3, Vite, Tailwind CSS (using daisyUI)

## Planned

- In-game chat

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

<img width="2560" height="1392" alt="1_welcome" src="https://github.com/user-attachments/assets/540a6de1-3b1c-47f4-87ad-609beca11130" />

<img width="2560" height="1392" alt="2_createjoin" src="https://github.com/user-attachments/assets/4c8241aa-dd68-49ec-bba7-5570ffe39a9a" />

<img width="2560" height="1392" alt="3_created_game" src="https://github.com/user-attachments/assets/57ec2ff8-9da5-4742-9881-6cfa62e49ea8" />

<img width="2560" height="1392" alt="4 2_gameinprogress" src="https://github.com/user-attachments/assets/8efc8092-70c8-40fd-bbab-80af48b17731" />

<img width="2560" height="1392" alt="5_game_won" src="https://github.com/user-attachments/assets/f806c096-aabe-4b80-8f68-608396f0b80f" />
