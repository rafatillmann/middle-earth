# Middle-earth

## 📋 Overview

This project consists of three main applications working together to demonstrate a complete distributed system architecture:

1. **Server** 🖥️ - A key-value store server that handles client requests
2. **Client** 📱 - Generates load and measures system performance metrics
3. **Logger** 📝 - A distributed logging system

## 🏗️ Architecture

The system is designed to showcase distributed logging capabilities with the ability to switch between different logging implementations (in-memory and with Apache BookKeeper). The logger component provides a clean interface-based architecture that abstracts away implementation details.

## 🖥️ Server

The Server application implements a simple key-value store that processes client requests for get, set, and delete operations. It maintains an in-memory ConcurrentHashMap to store key-value pairs and can handle multiple concurrent client connections.

**Key Features:**
- Concurrent request handling using threads
- Metrics collection for throughput measurement
- JSON-based message protocol

**Operations Supported:**
- `get`: Retrieve a value by key
- `set`: Store a key-value pair
- `delete`: Remove a key-value pair

## 📱 Client

The Client application is a load generator designed to stress-test the server and measure system performance. It can spawn multiple threads to simulate concurrent clients.

**Key Features:**
- Configurable number of client threads
- Read/write operation mix (configurable percentage)
- Latency measurement
- Think time simulation between requests
- Variable payload sizes

## 📝 Logger

The Logger application is a distributed logging system with a clean interface-based design. It provides abstraction over different logging strategies, currently supporting:

- **In-Memory**: Fast in-process logging for testing
- **BookKeeper**: Distributed, durable logging using Apache BookKeeper

### 🎯 Logger Interfaces

The logger is built around several key interfaces that define the contract for logging operations:

#### 1. 📦 Entry Interface
```java
public interface Entry {
    long entryId();
    byte[] payload();
}
```
Represents a single log entry with a unique identifier and byte array payload.

#### 2. ✍️ Writer Interface
```java
public interface Writer {
    void write(byte[] data) throws LoggerException;
    void write(byte[] data, LogCallback.AddEntryCallback callback) throws LoggerException;
    Reader getReader() throws LoggerException;
}
```
Provides methods to write data to the log with callback support.

#### 3. 📖 Reader Interface
```java
public interface Reader {
    Entry read(long entryId) throws LoggerException;
    List<Entry> read(long firstEntryId, long lastEntryId) throws LoggerException;
}
```
Allows reading individual entries by ID or ranges of entries.

#### 4. 🏭 LogFactory Interface
```java
public interface LogFactory {
    Writer getWriter(long logId) throws LoggerException;
}
```
Factory for creating writers for specific log identified by logId.

#### 5. 🎯 Cursor Interface
```java
public interface Cursor {
    CompletableFuture<String> notifyEntryAvailable(long entryId) throws LoggerException;
}
```
Provides asynchronous waiting for entries to become available, returning a CompletableFuture that completes when the entry is ready.

#### 6. 🌐 Gateway Interface
```java
public interface Gateway {
    void initialize() throws LoggerException;
}
```
Manages external connections and initializes the communication gateway (e.g., Socket-based gateway).

#### 7. 📞 LogCallback Interfaces
```java
public interface LogCallback {
    interface AddEntryCallback {
        void onComplete(Long entryId);
    }
}
```
Callbacks for asynchronous operations, allowing notification when write operations complete.

## 🧩 Project Structure

```
middle-earth/
├── client/          # Load generator and performance tester
├── logger/          # Distributed logging system
│   ├── interfaces/  # Core logging interfaces
│   ├── bookkeeper/  # BookKeeper implementation
│   ├── memory/      # In-memory implementation
│   └── gateway/     # Socket gateway implementation
├── server/          # Key-value store server
└── docker-compose.yml  # Infrastructure orchestration
```