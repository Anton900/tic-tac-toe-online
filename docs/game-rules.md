# TicTacToe - Game Rules

## Board
- 3x3 grid
- Represented internally as 9 positions (0-8)

## Players
- Two players per match
- Symbols: X and O
- X always starts

## Turn rules
- Players alternate turns
- A move is only valid if the target cell is empty
- Invalid moves are rejected by the backend

## Win conditions
A player wins if they occupy:
- Any full row
- Any full column
- Either diagonal

## Draw
- If all cells are filled
- And no win condition is met

## Game lifecycle
1. Match created
2. Players assigned
3. Game starts
4. Players make moves
5. Game ends (win or draw)
6. Result is persisted