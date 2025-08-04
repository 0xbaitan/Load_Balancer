#!/bin/bash

export NUM_SERVERS=10
export NUM_LOAD_BALANCERS=1


buildDockerCompose() {
  echo "Building Docker Compose for development..."
  docker compose -f docker-compose.yaml -p load-balancer build
}



runDockerCompose() {
  echo "Running Docker Compose for development..."
  docker compose -f docker-compose.yaml -p load-balancer up --scale backend=$NUM_SERVERS --scale load_balancer=$NUM_LOAD_BALANCERS -d
}


stopDockerCompose() {
    echo "Stopping Docker Compose for development..."
    docker compose -f docker-compose.yaml -p load-balancer down
    }



help() {
    echo "Usage: $0 [dev|build|stop]"
    echo "Commands:"
    echo "  dev         - Run Docker Compose"
    echo "  build       - Build Docker Compose"
    echo "  stop        - Stop Docker Compose"
}

case "$1" in
  dev)
    buildDockerCompose
    runDockerCompose
    ;;
  
  build)
    buildDockerCompose
    ;;
 
  stop)
    stopDockerCompose 
    ;;
    
  *)
    help
    exit 1
    ;;
esac