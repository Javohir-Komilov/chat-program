# Multi-User Chat Application

This project is a simple multi-user chat application developed in Java as part of a university assignment. The application allows multiple users to connect to a server and communicate with each other in real-time.

## Features

- **Server-Side Functionality:**
  - Handles multiple client connections.
  - Distributes messages from one client to all other connected clients.
  - Manages user sessions and ensures message integrity.

- **Client-Side Interface:**
  - User-friendly graphical interface for easy interaction.
  - Allows users to send and receive messages in real-time.
  - Displays a list of connected users.

## Requirements

- Java Development Kit (JDK) 8 or higher
- Basic knowledge of Java programming
- An Integrated Development Environment (IDE) like IntelliJ IDEA or Eclipse

## Installation

1. **Clone the repository:**
   ```bash
   git clone https://github.com/Javohir-Komilov/chat-program.git

2. **Navigate to the project directory:**
   ```bash
   cd chat-application

3. **Compile the project:**
   ```bash
   javac Server/*.java Connection/*.java Client/*.java

4. **Run the server:**
   ```bash
   java Server.Server

5. **Run the client:**
   ```bash
   java Client.Client

## Usage
- Start the server using the command provided above.
- Start multiple clients to simulate different users.
- Use the client interface to send messages, which will be broadcast to all connected clients.

## Project Structure
- Server/: Contains the server-side code.
- Connection/: Handles the connections between server and clients.
- Client/: Contains the client-side code and user interface.
