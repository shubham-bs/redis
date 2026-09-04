# Redis From Scratch in Java

A Redis-compatible in-memory server implemented from scratch in **Java 21**.

The goal is to understand how a Redis-like server works internally — from TCP connections and the RESP2 wire protocol to command dispatching, concurrent storage, expiration, Redis data structures, pipelining, and protocol hardening.

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Architecture](#architecture)
- [Request Lifecycle](#request-lifecycle)
- [Project Structure](#project-structure)
- [RESP2 Protocol](#resp2-protocol)
- [Networking and Concurrency](#networking-and-concurrency)
- [Storage Architecture](#storage-architecture)
- [Expiration](#expiration)
- [Supported Commands](#supported-commands)
- [Pipelining](#pipelining)
- [Protocol Hardening](#protocol-hardening)
- [Error Handling](#error-handling)
- [Testing](#testing)
- [Running the Server](#running-the-server)
- [Using redis-cli](#using-redis-cli)
- [Design Decisions](#design-decisions)
- [Limitations](#limitations)
- [Future Work](#future-work)
- [What I Learned](#what-i-learned)
- [Project Status](#project-status)

## Overview

This project implements a focused subset of Redis functionality in Java.

The server accepts Redis clients over TCP, decodes Redis Serialization Protocol (RESP2) messages, converts them into commands, executes those commands against a shared in-memory datastore, and encodes the result back into RESP2.

The implementation focuses on understanding core systems concepts rather than reproducing the entire Redis codebase.

### Core pipeline

```text
                                   Client
                                     |
                                     | TCP
                                     v
                                RedisServer
                                     |
                                     | Virtual Thread per client
                                     v
                              RESP2 Decoder
                                     |
                                     v
                              CommandRequest
                                     |
                                     v
                             CommandDispatcher
                                     |
                                     v
                              Command Handler
                                     |
                                     v
                              +-------------+
                              |  DataStore  |
                              +------+------+
                                     |
                    +----------------+----------------+
                    |                |                |
                    v                v                v
                +-------+        +-------+        +-------+
                |String |        | List  |        | Hash  |
                +-------+        +-------+        +-------+
                   |                |                |
                   +----------------+----------------+
                                    |
                                 +-------+
                                 |  Set  |
                                 +-------+
  
                                     |
                                     +--> Expiration
                                     |
                                     v
                               RESP2 Encoder
                                     |
                                     v
                                  Client
```

## Features

### Networking

- TCP server using Java `ServerSocket`
- Multiple simultaneous clients
- One Java 21 virtual thread per client
- Shared in-memory datastore
- Graceful client disconnect handling
- Protocol-error handling per client

### RESP2 Protocol

Supports:

- Simple Strings
- Errors
- Integers
- Bulk Strings
- Arrays
- Null values

The decoder handles:

- Fragmented TCP reads
- Multiple requests received together
- Pipelined commands
- Incomplete messages
- Invalid protocol input
- Input size limits
- Proper CRLF validation

### Strings

```text
PING
ECHO
SET
GET
DEL
EXISTS
INCR
DECR
```

### Expiration

```text
EXPIRE
PEXPIRE
TTL
PTTL
PERSIST
```

`SET` also supports:

```text
SET key value EX seconds
SET key value PX milliseconds
```

### Lists

```text
LPUSH
RPUSH
LPOP
RPOP
LLEN
LRANGE
```

### Hashes

```text
HSET
HGET
HGETALL
HDEL
HEXISTS
```

### Sets

```text
SADD
SREM
SISMEMBER
SMEMBERS
SCARD
```

## Architecture

```text
                         +----------------+
                         |   redis-cli    |
                         |   / TCP Client |
                         +-------+--------+
                                 |
                                 | TCP
                                 v
                         +---------------+
                         |  RedisServer  |
                         +-------+-------+
                                 |
                            accept()
                                 |
                                 v
                    +------------------------+
                    | Java Virtual Thread    |
                    |       per client       |
                    +-----------+------------+
                                |
                                v
                       +----------------+
                       |  RespDecoder   |
                       +-------+--------+
                               |
                               v
                       +----------------+
                       | CommandRequest |
                       +-------+--------+
                               |
                               v
                    +---------------------+
                    | CommandDispatcher   |
                    +----------+----------+
                               |
                               v
                       +---------------+
                       | Command       |
                       | Handler       |
                       +-------+-------+
                               |
                               v
                       +---------------+
                       |   DataStore   |
                       +-------+-------+
                               |
              +----------------+----------------+
              |                |                |
              v                v                v
          +-------+        +-------+        +-------+
          |String |        | List  |        | Hash  |
          +-------+        +-------+        +-------+
                                                  |
                                                  v
                                            +---------+
                                            |   Set   |
                                            +---------+

                               |
                               v
                       +---------------+
                       |  Expiration   |
                       +---------------+
                               |
                               v
                       +---------------+
                       |  RespEncoder  |
                       +-------+-------+
                               |
                               v
                         TCP Response
                               |
                               v
                            Client
```

## Request Lifecycle

For:

```text
SET name shubham
```

the request flows through:

```text
1. Client creates RESP2 request
             |
             v
2. TCP sends bytes
             |
             v
3. RespDecoder reads bytes
             |
             v
4. RESP Array becomes Java RespValue objects
             |
             v
5. CommandRequestParser creates:

   command = "SET"
   arguments = ["name", "shubham"]

             |
             v
6. CommandDispatcher finds SetCommand
             |
             v
7. SetCommand executes against DataStore
             |
             v
8. DataStore stores RedisString("shubham")
             |
             v
9. Command returns SimpleString("OK")
             |
             v
10. RespEncoder converts response to:

    +OK\r\n

             |
             v
11. TCP sends response to client
```

This separation keeps protocol parsing, command routing, command logic, and storage independent.

## Project Structure

```text
redis/
├── pom.xml
├── README.md
│
├── src/
│   ├── main/
│   │   └── java/
│   │       └── redis/
│   │           ├── Main.java
│   │           │
│   │           ├── network/
│   │           │   └── RedisServer.java
│   │           │
│   │           ├── protocol/
│   │           │   ├── RespValue.java
│   │           │   ├── RespDecoder.java
│   │           │   └── RespEncoder.java
│   │           │
│   │           ├── command/
│   │           │   ├── Command.java
│   │           │   ├── CommandRequest.java
│   │           │   ├── CommandRequestParser.java
│   │           │   ├── CommandDispatcher.java
│   │           │   └── handlers/
│   │           │
│   │           ├── storage/
│   │           │   ├── DataStore.java
│   │           │   ├── Entry.java
│   │           │   ├── RedisValue.java
│   │           │   ├── RedisString.java
│   │           │   ├── RedisList.java
│   │           │   ├── RedisHash.java
│   │           │   └── RedisSet.java
│   │           │
│   │           └── expiration/
│   │               └── ExpirationManager.java
│   │
│   └── test/
│       └── java/
│           └── redis/
│               ├── command/
│               ├── storage/
│               ├── protocol/
│               ├── expiration/
│               └── integration/

```

## RESP2 Protocol

Redis clients communicate with the server using bytes on the network, not Java objects.

For example:

```text
SET name shubham
```

is represented approximately as:

```text
*3\r\n
$3\r\n
SET\r\n
$4\r\n
name\r\n
$7\r\n
shubham\r\n
```

The decoder transforms these bytes into:

```text
RespValue.Array
    |
    +-- BulkString("SET")
    +-- BulkString("name")
    +-- BulkString("shubham")
```

Then the command parser creates:

```text
CommandRequest
    command: SET
    arguments:
        name
        shubham
```

Responses are encoded back into RESP2.

For example:

```text
+OK\r\n
```

represents a RESP Simple String.

## Networking and Concurrency

The server uses blocking Java TCP sockets.

The core model is:

```text
ServerSocket
     |
     +-- accept client A
     |       |
     |       +--> Virtual Thread A
     |
     +-- accept client B
     |       |
     |       +--> Virtual Thread B
     |
     +-- accept client C
             |
             +--> Virtual Thread C
```

Each client gets an independent virtual thread.

All clients share the same `DataStore`:

```text
Client A ─────┐
              |
Client B ─────┼────> Shared DataStore
              |
Client C ─────┘
```

This allows multiple clients to operate on the same Redis keys concurrently.

### Why virtual threads?

Java 21 virtual threads provide a simple thread-per-client programming model without requiring a traditional platform thread for every connected client.

This project intentionally uses this straightforward blocking model instead of implementing a selector/event-loop architecture.

## Storage Architecture

The top-level keyspace is backed by a concurrent map:

```text
ConcurrentHashMap<String, Entry>
```

Conceptually:

```text
"counter" -> Entry(RedisString("42"))

"users"   -> Entry(RedisHash(...))

"queue"   -> Entry(RedisList(...))

"skills"  -> Entry(RedisSet(...))
```

`RedisValue` acts as the common abstraction for supported Redis data types.

```text
RedisValue
   |
   +-- RedisString
   +-- RedisList
   +-- RedisHash
   +-- RedisSet
```

This allows different Redis types to live under different keys.

## Concurrency and Atomic Operations

A normal read-modify-write sequence can have a race condition.

For example, two clients performing:

```text
INCR counter
```

at the same time could both read:

```text
counter = 10
```

and both calculate:

```text
11
```

resulting in a lost update.

The implementation therefore performs the mutation using:

```text
ConcurrentHashMap.compute(...)
```

so the update of a particular key is performed atomically.

The same approach is used for `DECR`.

This project includes concurrency tests that execute increments from multiple virtual-thread clients.

## Redis Data Structures

### Strings

Stored using:

```text
RedisString
```

Example:

```text
SET name shubham
GET name
```

### Lists

Implemented using an internal Java list.

Supported operations:

```text
LPUSH
RPUSH
LPOP
RPOP
LLEN
LRANGE
```

Example:

```text
LPUSH numbers 3
LPUSH numbers 2
RPUSH numbers 4

LRANGE numbers 0 -1
```

Conceptually:

```text
[2, 3, 4]
```

### Hashes

Implemented using a map of fields to values.

```text
HSET user name shubham
HSET user mood boutta kiss a moving train

HGET user name
HGETALL user
```

Conceptually:

```text
user
 |
 +-- name    -> shubham
 +-- mood -> boutta kiss a moving train
```

### Sets

Implemented using a set of unique members.

```text
SADD skills java
SADD skills redis
SADD skills java
```

The duplicate `java` is not inserted twice.

Supported operations:

```text
SADD
SREM
SISMEMBER
SMEMBERS
SCARD
```

## Expiration

Keys can have an expiration timestamp.

For:

```text
SET session abc EX 10
```

the datastore conceptually maintains:

```text
session
   |
   +-- value      -> abc
   |
   +-- expiration -> current time + 10 seconds
```

The implementation uses an `ExpirationManager` to maintain expiration timestamps.

Expiration is checked when a key is accessed.

This is a **lazy expiration** approach: keys are removed when they are observed to have expired rather than requiring a dedicated background expiration thread.

### Supported commands

```text
EXPIRE key seconds
PEXPIRE key milliseconds

TTL key
PTTL key

PERSIST key
```

`SET` can also create an expiration directly:

```text
SET key value EX 10
SET key value PX 5000
```

When a normal `SET` overwrites an existing key, its previous expiration is removed.

## Pipelining

TCP is a byte stream, so multiple RESP requests can be sent together.

For example:

```text
SET a 1
SET b 2
INCR a
GET a
GET b
```

can be encoded and sent as one network write.

The server then repeatedly decodes requests from the same stream:

```text
TCP byte stream
      |
      v
+-------------+
| Request #1  | ---> Response #1
+-------------+
| Request #2  | ---> Response #2
+-------------+
| Request #3  | ---> Response #3
+-------------+
| Request #4  | ---> Response #4
+-------------+
| Request #5  | ---> Response #5
+-------------+
```

A dedicated integration test verifies multiple commands sent through a single connection.

## Protocol Hardening

The RESP decoder validates incoming input rather than assuming every client is well behaved.

Current protections include:

### Maximum line length

```text
64 KB
```

### Maximum bulk string size

```text
10 MB
```

### Maximum array size

```text
1,000,000 elements
```

The decoder also rejects:

- Invalid integers
- Invalid bulk-string lengths
- Invalid array lengths
- Bare LF where CRLF is expected
- Invalid CRLF sequences
- Unknown RESP types
- Incomplete bulk strings
- Incomplete arrays

Malformed protocol input is treated as a protocol error for that client connection.

## Error Handling

Unknown commands return RESP errors.

Example:

```text
ERR unknown command 'FOO'
```

Wrong argument counts are returned as errors.

Wrong data types are detected.

For example:

```text
SET name shubham
LPUSH name hello
```

results in a `WRONGTYPE` error because `name` contains a string rather than a list.

Command execution errors are converted into RESP Error responses rather than crashing the entire server.

## Supported Commands

| Category | Commands |
|---|---|
| Connection/basic | `PING`, `ECHO` |
| Strings | `SET`, `GET` |
| Key management | `DEL`, `EXISTS` |
| Counters | `INCR`, `DECR` |
| Expiration | `EXPIRE`, `PEXPIRE`, `TTL`, `PTTL`, `PERSIST` |
| Lists | `LPUSH`, `RPUSH`, `LPOP`, `RPOP`, `LLEN`, `LRANGE` |
| Hashes | `HSET`, `HGET`, `HGETALL`, `HDEL`, `HEXISTS` |
| Sets | `SADD`, `SREM`, `SISMEMBER`, `SMEMBERS`, `SCARD` |

## Testing

This project contains unit, integration, protocol, and concurrency tests.

Test areas include:

```text
Command handlers
        |
        +-- PING
        +-- ECHO
        +-- SET
        +-- GET
        +-- DEL
        +-- EXISTS
        +-- INCR
        +-- DECR
        +-- expiration commands
        +-- list commands
        +-- hash commands
        +-- set commands

Protocol
        |
        +-- RESP decoding
        +-- RESP encoding
        +-- edge cases
        +-- malformed input
        +-- hardening

Integration
        |
        +-- TCP server
        +-- multiple clients
        +-- shared datastore
        +-- pipelining
        +-- data types

Concurrency
        |
        +-- concurrent INCR
        +-- concurrent DECR
```

Current verification:

```text
108 tests
0 failures
0 errors
```

Build verification:

```bash
mvn test
mvn package
```

The Maven package step produces:

```text
target/redis-from-scratch-1.0-SNAPSHOT.jar
```

## Running the Server

### Requirements

- Java 21+
- Maven
- `redis-cli` for manual testing

Check Java:

```bash
java -version
```

Build:

```bash
mvn package
```

Run:

```bash
java -cp target/redis-from-scratch-1.0-SNAPSHOT.jar redis.Main
```

The default server port is:

```text
6379
```

## Using redis-cli

With the server running:

```bash
redis-cli
```

### Basic commands

```text
127.0.0.1:6379> PING
PONG

127.0.0.1:6379> ECHO hello
"hello"

127.0.0.1:6379> SET name shubham
OK

127.0.0.1:6379> GET name
"shubham"
```

### Counters

```text
127.0.0.1:6379> SET counter 10
OK

127.0.0.1:6379> INCR counter
(integer) 11

127.0.0.1:6379> DECR counter
(integer) 10
```

### Expiration

```text
127.0.0.1:6379> SET session abc EX 10
OK

127.0.0.1:6379> TTL session
(integer) 9
```

### Lists

```text
127.0.0.1:6379> LPUSH numbers 3
(integer) 1

127.0.0.1:6379> LPUSH numbers 2
(integer) 2

127.0.0.1:6379> LRANGE numbers 0 -1
1) "2"
2) "3"
```

### Hashes

```text
127.0.0.1:6379> HSET user name shubham
(integer) 1

127.0.0.1:6379> HGET user name
"shubham"
```

### Sets

```text
127.0.0.1:6379> SADD skills java
(integer) 1

127.0.0.1:6379> SADD skills java
(integer) 0

127.0.0.1:6379> SMEMBERS skills
1) "java"
```

## Design Decisions

### Why ServerSocket?

The networking layer intentionally uses Java's blocking `ServerSocket` and `Socket` APIs.

This keeps the server architecture straightforward:

```text
accept()
   |
   +--> virtual thread
   |
   +--> virtual thread
   |
   +--> virtual thread
```

A selector/event-loop implementation would add complexity without being necessary right now (i.e too much work brodaa...i just wanna sleep..ded).

### Why ConcurrentHashMap?

Multiple clients can access the same datastore simultaneously.

`ConcurrentHashMap` provides thread-safe access to the top-level keyspace.

Operations that require atomic read-modify-write semantics use `compute(...)`.

### Why synchronize Redis data structures?

A thread-safe map does not automatically make the objects stored inside it thread-safe.

Therefore, mutations and reads inside the list, hash, and set implementations are synchronized.

### Why separate the protocol layer?

The protocol layer should not know what `SET`, `GET`, or `LPUSH` means.

Its responsibility is:

```text
bytes <-> RESP values
```

The command layer handles:

```text
RESP values -> command request -> command execution
```

This separation makes the architecture easier to test and extend (i.e. separating issues before the issues separate me).

## Limitations

This is an chootu sa Redis-compatible server, not a production replacement for Redis.

It intentionally does not implement the complete Redis feature set.

Current limitations include:

- No AOF persistence
- No RDB snapshots
- No replication
- No Pub/Sub
- No transactions
- No WATCH
- No Sorted Sets
- No Streams
- No GEO
- No ACL/authentication
- No cluster mode
- No Redis modules
- No production-level memory management
- No complete Redis command compatibility
- Lazy expiration rather than a full Redis expiration subsystem
- Simplified networking compared with production Redis

## Future Work

Possible future extensions:

1. Append-only file persistence (AOF)
2. RDB snapshots
3. Replication
4. Pub/Sub
5. Transactions
6. WATCH
7. Sorted Sets
8. Streams
9. Additional protocol compatibility
10. More complete Redis command support

AOF is the most natural next extension because it would introduce persistence and startup recovery while fitting the existing command architecture.

