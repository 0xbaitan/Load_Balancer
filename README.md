# Load Balancer

A concurrent load balancer for Dockerized backend services, written in Java. It automatically discovers healthy containers, distributes requests using a thread-safe round robin strategy, and routes HTTP traffic to available backends.

## Features

- **Automatic Service Discovery:** Uses Docker API to find running containers (excluding itself).
- **Health Checking:** Periodically checks `/health` endpoint of each backend.
- **Concurrency:** Handles multiple requests in parallel using Java thread pools.
- **Round Robin Strategy:** Requests are distributed evenly using a concurrent round robin algorithm.
- **Hot Reload:** Supports automatic recompilation and restart on code changes (via `watch.sh`).

## Architecture

- **Main Entry:** [`src/main/java/com/baitan/Main.java`](balancer/src/main/java/com/baitan/Main.java)
- **Load Balancer Core:** [`src/main/java/com/baitan/balancer/ConcurrentLoadBalancer.java`](balancer/src/main/java/com/baitan/balancer/ConcurrentLoadBalancer.java)
- **Strategy Pattern:** [`src/main/java/com/baitan/balancer/strategy/ConcurrentRoundRobinStrategy.java`](balancer/src/main/java/com/baitan/balancer/strategy/ConcurrentRoundRobinStrategy.java)
- **Service Model:** [`src/main/java/com/baitan/balancer/Service.java`](balancer/src/main/java/com/baitan/balancer/Service.java)
- **Health Checking:** [`src/main/java/com/baitan/balancer/health/HealthChecker.java`](balancer/src/main/java/com/baitan/balancer/health/HealthChecker.java), [`HealthCheckThread.java`](balancer/src/main/java/com/baitan/balancer/health/HealthCheckThread.java)
- **Proxy Handler:** [`src/main/java/com/baitan/balancer/handlers/ProxyHandler.java`](balancer/src/main/java/com/baitan/balancer/handlers/ProxyHandler.java)
- **Build & Watch Script:** [`balancer/watch.sh`](balancer/watch.sh)

## How It Works

1. **Startup:** The load balancer starts, discovers healthy backend containers, and initializes the round robin strategy.
2. **Health Checks:** A background thread periodically updates the list of healthy services.
3. **Request Handling:** Incoming HTTP requests are routed to the next healthy backend in round robin order.
4. **Concurrency:** Multiple requests are handled in parallel using a thread pool.
5. **Hot Reload:** Changes in source files trigger recompilation and restart via `watch.sh`.

## Key Code Highlights

- **Concurrent Round Robin:**
  ```java
  // ConcurrentRoundRobinStrategy.java
  int index = currentIndex.getAndUpdate(i -> (i + 1) % size) % size;
  Service service = services.get(index);
  ```
- **Health Checking:**
  ```java
  // HealthChecker.java
  return Arrays.stream(healthyContainers)
      .map(service -> service.isHealthy() ? service : null)
      .filter(service -> service != null)
      .toArray(Service[]::new);
  ```
- **Proxy Routing:**
  ```java
  // ProxyHandler.java
  Service currentBackend = strategy.getNextService();
  currentBackend.routeRequest(exchange);
  ```

## Usage

1. **Start Docker containers for your backend services.**
2. **Run the load balancer:**

   ```bash
   cd balancer
   ./watch.sh
   ```

   This script compiles, runs, and watches for code changes.

3. **Send HTTP requests to `localhost:8080`.** The load balancer will forward requests to healthy backend containers.

## Notes

- Only containers with a `/health` endpoint returning HTTP 200 are considered healthy.
- The load balancer itself must be named `/load_balancer` in Docker to be excluded from routing.
- The round robin strategy is thread-safe and ensures fair distribution under concurrent load.

## License

MIT
