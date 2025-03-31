# Project 2 - Final Report

## Table of Contents
1. [System Design](#system-design)
   - [Architecture Overview](#architecture-overview)
   - [Server Design](#server-design)
   - [Client Design](#client-design)
   - [Network Protocol](#network-protocol)
2. [Implementation Details](#implementation-details)
   - [Concurrency Control](#concurrency-control)
   - [TCP/UDP Communication](#tcpudp-communication)
   - [GUI Implementation](#gui-implementation)
   - [Game Logic](#game-logic)

## System Design

### Architecture Overview

```
┌───────────────────────────────────────────────────────────────┐
│                       GAME SERVER                             │
│  ┌─────────────┐    ┌─────────────┐      ┌─────────────────┐  │
│  │ Main Server │───▶│ UDP Listener│      │  Question Bank  │  │
│  └─────────────┘    └─────────────┘      └─────────────────┘  │
│         ▲                                                     │
│         │                                                     │
│         ▼                                                     │
│  ┌─────────────┐                                              │
│  │ TCP Listener│                                              │
│  └─────────────┘                                              │
└───────────────────────────────────────────────────────────────┘
                   ▲                     ▲
                   │                     │
           TCP     │             UDP     │
        ┌──────────┘        ┌────────────┘
        │                   │
        ▼                   ▼
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Player 1  │    │   Player 2  │    │   Player 3  │
└─────────────┘    └─────────────┘    └─────────────┘
```

The system follows a client-server architecture with:

- **Centralized Server**: Manages game state, questions, and player interactions
- **Multiple Clients**: Each player connects via their own client instance
- **Dual Protocol Communication**:
    - TCP for reliable question/answer transmission
    - UDP for fast buzzer responses

### Server Design

#### Components

1. **Main Server Thread**
- Accepts new client connections
- Manages game lifecycle (20 questions)
- Coordinates between all components
2. **ClientThread (Per Client)**
- Dedicated thread per connected client
- Handles all TCP communication:
    - Question delivery
    - Answer collection
    - Score updates
    - Game state notifications
3. **UDPThread (Single Instance)**
- Listens for buzzer presses from all clients
- Maintains synchronized buzz queue
- Handles out-of-order UDP packets
4. **QuestionBank**
- Manages pool of 20 questions
- Loads questions from text file
- Tracks current question

#### Key Data Structures:

- `ConcurrentHashMap<Integer, ClientThread>`: Thread-safe active client list
- `ConcurrentLinkedQueue<Integer>`: Ordered buzz queue
- `ConcurrentHashMap<Integer, Integer>`: Player scores

### Client Design

#### Components:

1. GUI Layer (Swing)
- Question/options display
- Interactive controls (Poll/Submit buttons)
- Score/timer visualization
2. Network Layer
- TCP Connection: Persistent server connection
- UDP Socket: For buzzer presses
- Message handlers for server communication
3. Game State Manager
- Tracks current question
- Manages answer eligibility
- Handles timer countdowns

### Network Protocol

#### TCP Messages (Reliable Delivery)

| Message Type | Direction | Purpose | Payload |
| :--: | :--: | :--: | :--: |
| QUESTION | S $\rightarrow$ C | Deliver new question | Question object |
| ACK | S $\rightarrow$ C | Confirm buzzer success | None |
| NACK | S $\rightarrow$ C | Reject buzzer attempt | None |
| CORRECT | S $\rightarrow$ C | Right answer | None |
| WRONG | S $\rightarrow$ C | Wrong answer | None |
| TIMEOUT | S $\rightarrow$ C | Answer timeout | None |
| SCORE_UPDATE | S $\rightarrow$ C | Broadcast scores | Map<ClientID, Score> |
| GAME_OVER | S $\rightarrow$ C | End game signal | None |
| PLAYER_ANSWER | C $\rightarrow$ S | Submit answer | PlayerAnswer object |

#### UDP Messages (Fast Buzzer)

| Message Type | Direction | Purpose | Payload |
| :--: | :--: | :--: | :--: |
| BUZZ | C $\rightarrow$ S | Buzzer attempt | None |

### Implementation Details

#### Concurrency Control

##### Server-Side:
- Thread pool for client connections
- Synchronized access to shared buzz queue
- Atomic score updates
- Volatile flags for game state

##### Client-Side:
- Swing event dispatch thread
- Separate thread for TCP message listening
- Thread-safe GUI updates via `SwingUtilities.invokeLater()`

#### TCP/UDP Communication

##### TCP Implementation:
- Persistent sockets per client
- Object streams for serialization
- Heartbeat detection for disconnected clients

##### UDP Implementation:
- Timestamp-based ordering
- Client IP verification
- Queue management with thread safety

#### GUI Implementation

##### Key Features:
- Responsive layout with leaderboard
- Visual timer countdown
- Disabled states during inappropriate times
- Immediate feedback for answers

##### Event Handling:
- Poll button → UDP "buzz"
- Submit button → TCP answer
- Radio buttons → Answer selection

#### Game Logic

##### Question Flow:
- Server broadcasts question (TCP)
- 15-second buzz period (UDP)
- First buzzer gets 10s to answer (TCP)
- If correct: +10 points, next question
- If wrong/timeout: -10/-20, next buzzer
- Repeat until correct answer or queue empty
- After 20 questions: end game

##### Scoring Rules:
- Correct answer: +10
- Wrong answer: -10
- Timeout: -20
- First correct answer wins points