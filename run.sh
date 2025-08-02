#!/bin/bash

export NUM_SERVERS=10
export NUM_LOAD_BALANCERS=1


buildDockerComposeDev() {
  echo "Building Docker Compose for development..."
  docker-compose -f docker-compose.dev.yaml build
}

buildDockerComposeProd() {
  echo "Building Docker Compose for production..."
  docker-compose -f docker-compose.prod.yaml build
}

runDockerComposeDev() {
  echo "Running Docker Compose for development..."
  docker-compose -f docker-compose.dev.yaml up --scale backend=$NUM_SERVERS --scale load_balancer=$NUM_LOAD_BALANCERS -d
}

runDockerComposeProd() {
  echo "Running Docker Compose for production..."
  docker-compose -f docker-compose.prod.yaml up --scale backend=$NUM_SERVERS --scale load_balancer=$NUM_LOAD_BALANCERS -d
}

stopDockerComposeDev() {
    echo "Stopping Docker Compose for development..."
    docker-compose -f docker-compose.dev.yaml down
    }

stopDockerComposeProd() {
    echo "Stopping Docker Compose for production..."
    docker-compose -f docker-compose.prod.yaml down
}

help() {
    echo "Usage: $0 [dev|prod|build-dev|build-prod|stop-dev|stop-prod]"
    echo "Commands:"
    echo "  dev         - Run Docker Compose in development mode"
    echo "  prod        - Run Docker Compose in production mode"
    echo "  build-dev   - Build Docker Compose for development"
    echo "  build-prod  - Build Docker Compose for production"
    echo "  stop-dev    - Stop Docker Compose in development mode"
    echo "  stop-prod   - Stop Docker Compose in production mode"
}

case "$1" in
  dev)
    buildDockerComposeDev
    runDockerComposeDev
    ;;
  prod)
    buildDockerComposeProd
    runDockerComposeProd
    ;;
  build-dev)
    buildDockerComposeDev
    ;;
  build-prod)
    buildDockerComposeProd
    ;;
  stop-dev)
    stopDockerComposeDev 
    ;;
    
  stop-prod)
    stopDockerComposeProd
    ;;

  *)
    help
    exit 1
    ;;
esac