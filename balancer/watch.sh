#!/bin/bash


runLoadBalancer() {
  java -jar balancer.jar & APP_PID=$!
}

stopLoadBalancer() {
  if [[ -n "$APP_PID" ]]; then
    echo "Stopping Load Balancer..."
    kill "$APP_PID"
    wait "$APP_PID" 2>/dev/null
    echo "Load Balancer stopped."
  fi
}

compileLoadBalancer() {
  echo "Compiling Load Balancer..."
  mvn compile
}

restartLoadBalancer() {
  stopLoadBalancer
  compileLoadBalancer
  runLoadBalancer
}

watchChanges() {
    watchexec -w src -w pom.xml -- bash -c 'compileLoadBalancer && restartLoadBalancer'
}

runLoadBalancer
watchChanges