# A console chat application written in Java

This is a simple console chat application that I wrote in Java. It uses a server-client architecture, where the server is responsible for handling incoming connections and messages, and the client is responsible for sending messages to the server.

## Message types
- **REQUEST_ACTION:** Server requests the client to choose an action (create or join a chat).
- **CREATE_CHAT:** Client requests to create a new chat room.
- **JOIN_CHAT:** Client requests to join an existing chat room.
- **CHAT_CREATED:** Server informs the client that a new chat has been created.
- **CHAT_LIST:** Server provides a list of available chat rooms.
- **PORT_REQUEST:** Server requests the client to provide the port number of the chat room to join.
- **CHAT_SELECTED:** Client provides the selected chat room's port number.
- **DISCONNECT:** Server instructs the client to disconnect.
- **NAME_REQUEST:** Server requests the client's nickname.
- **USER_NAME:** Client provides their nickname.
- **NAME_ACCEPTED:** Server confirms the nickname is accepted.
- **TEXT:** Regular text message sent by the client.
- **COMMAND:** Client sends a command (e.g., /rename, /list).
- **USER_ADDED:** Server informs clients that a new user has joined.
- **USER_REMOVED:** Server informs clients that a user has left.
- **NICKNAME_CHANGED:** Server informs clients of a nickname change.
- **USER_LIST:** Server sends a list of users in the chat room.
- **HELP_MESSAGE:** Server sends help information.
- **ERROR:** Server or client reports an error.

## Message Structure
A *Message* consists of:
- **Type** (MessageType): The type of message.
- **Data** (String): The main content or payload.
- **Sender** (String): The username of the sender.
- **Timestamp** (LocalDateTime): The time when the message was sent.
- **ChatRoom** (String): The name of the chat room.

## Client-Server Interaction Flow
### Connecting to the Main Server
1. **Client Connection:** 
    - The client connects to the MainServer on the default port (12345).
2. **Action Request:**
   - The server sends a REQUEST_ACTION message asking if the client wants to create or join a chat room.
3. **Client Response:** 
   - The client responds with either CREATE_CHAT or JOIN_CHAT.
4. **Creating a New Chat:**
   - If the client chooses to create a chat:
   - The MainServer assigns a new port and starts a ChatServer.
   - Sends a CHAT_CREATED message with the new chat's port.
   - The client then connects to the ChatServer.
5. **Joining an Existing Chat:**
   - If the client chooses to join a chat:
   - The MainServer sends a CHAT_LIST message with available chats.
   - Sends a PORT_REQUEST asking for the chat's port number.
   - The client responds with CHAT_SELECTED.
   - The server confirms, and the client connects to the ChatServer.
### Communication within a Chat Room
1. **Handshake:**
   - The ChatServer requests the client's nickname (NAME_REQUEST).
   - The client provides their nickname (USER_NAME).
   - The server accepts or rejects the nickname (NAME_ACCEPTED or ERROR).
2. **Messaging and Commands:**
   - Clients can send text messages (TEXT) or commands (COMMAND).
   - Commands include /rename, /list, /help, and /exit.
3. Broadcasting Messages:
    - The server broadcasts messages and notifications to all connected clients.
    - Includes user join/leave notifications (USER_ADDED, USER_REMOVED).

## Command Handling
    /rename [new_name]: Changes the user's nickname.
    /list: Lists all users in the current chat room.
    /help: Displays available commands.
    /exit: Exits the chat application.

## Running the Application
1. **Compile:** Compile the Java files using `gradle build`.
2. **Start the Main Server:** Run `gradle runServer --console=plain`.
3. **Start the Client:** Run `gradle runClient --console=plain`.