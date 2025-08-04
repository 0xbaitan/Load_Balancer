# Network Load Balancer (Layer 4) in Java

This project implements a basic **Network Load Balancer** operating at the OSI model Layer 4 (Transport Layer) using Java. It efficiently distributes incoming TCP requests across multiple **Node.js backend services**, scaled horizontally with Docker. The load balancer acts as a proxy server and uses a simple **Round Robin** strategy to distribute requests evenly among healthy backend instances.

## Features

- **Automatic Service Discovery:** Uses Docker Java API to detect running backend containers dynamically (excludes the load balancer container itself).
    
- **Health Checking:** Periodically polls backend `/health` endpoints to verify container health, marking services as healthy/unhealthy accordingly.
    
- **Stateful Health Adjustment:** Automatically adds/removes backend services to/from the pool based on their health status in real time.
    
- **Concurrent Request Handling:** Supports multiple simultaneous client requests through Java thread pools.
    
- **Round Robin Load Balancing:** Thread-safe, stateless Round Robin algorithm to fairly distribute requests evenly among healthy backends.
    

## Architecture Overview

- **Main Application:** [`Main.java`](balancer/search/src/main/java/com/baitan/Main.java) — entry point initializing services and health checks.
    
- **Load Balancer Core:** [`ConcurrentLoadBalancer.java`](balancer/src/main/java/com/baitan/balancer/ConcurrentLoadBalancer.java) — manages incoming connections and dispatches requests.
    
- **Load Balancing Strategy:** [`ConcurrentRoundRobinStrategy.java`](balancer/src/main/java/com/baitan/balancer/strategy/ConcurrentRoundRobinStrategy.java) — implements thread-safe round robin logic.
    
- **Service Model:** [`Service.java`](balancer/src/main/java/com/baitan/balancer/Service.java) — represents backend service container metadata.
    
- **Health Checking:** [`HealthChecker.java` and `HealthCheckThread.java`](balancer/src/main/java/com/baitan/balancer/health/) — monitors backend health and updates service lists.
    
- **Proxy Handler:** [`ProxyHandler.java`](balancer/src/main/java/com/baitan/balancer/handlers/) — forwards client requests to selected backend instances.
    

## How It Works

1. **Startup:** On launch, the load balancer queries Docker API to discover running backend containers and initializes the round robin distribution.
    
2. **Periodic Health Checks:** A dedicated background thread continuously monitors `/health` endpoints of services; unhealthy containers are dynamically removed and can be re-added upon recovery.
    
3. **Request Routing:** Incoming requests to `localhost:8080` (the endpoint of the load balancer) are proxied to the next available healthy backend in round robin order.
    
4. **Concurrency:** Uses Java thread pools to handle many simultaneous client connections efficiently.
    

## Load Balancing Strategy

- **Round Robin:** Simplest load balancing approach cycling through backends sequentially.
    
- Adequate for uniform backend workloads (e.g., simple "Hello World" Node.js servers in this case).
    
- Not optimal if backends have uneven capacity or workload variations.
    
- For more complex environments, consider advanced algorithms like Least Connections or Least Response Time.
    

## Health Checking Details

- Uses Docker Java API (via Maven) to detect containers.
    
- Regularly polls backend `/health` endpoints.
    
- Backends return HTTP 200 are healthy; else are marked unhealthy.
    
- Simulates failure with a 5% chance of returning HTTP 500.
    
- Immediately updates backend list for load balancing.
    
- Adjusts round robin index to avoid service skipping on removal.
    

## Usage Instructions

1. **Deploy Backend Services:**
    
    - Start Node.js backend services in Docker containers.
        
    - Scale up with `docker-compose scale backend=10` or equivalent.
        
2. **Run the Load Balancer:**
    
    bash
    
    `cd balancer ./watch.sh`
    
    This script compiles, runs the load balancer, and watches for code changes.
    
3. **Send Requests:**
    
    - Access services through the load balancer at `http://localhost:8080`.
        
    - Requests are forwarded to healthy backend instances automatically.
        

## Testing

- Load tested with **Apache JMeter** simulating 1000 concurrent users ramped up over 100 seconds.
    
- Verified even request distribution and healthy backend management.
    

## Important Notes

- Only Docker containers with a `/health` endpoint returning HTTP 200 are considered healthy.
    
- The load balancer container must be named `/load_balancer` to be excluded from backend routing.
    
- The implemented Round Robin strategy is fully thread-safe for concurrent environments.
    

## License

This project is licensed under the MIT License.