package com.baitan.balancer.strategy;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import com.baitan.balancer.Service;

/**
 * A thread-safe implementation of the BalancingStrategy interface using a
 * concurrent round-robin approach.
 * <p>
 * This strategy maintains a list of services and provides methods to add,
 * remove, and retrieve services in a round-robin manner, ensuring that the next
 * service is selected in a thread-safe way.
 * </p>
 * 
 * @see BalancingStrategy
 * @see Service
 * 
 * @author Tanish Baidya
 */
public class ConcurrentRoundRobinStrategy implements BalancingStrategy {

    private static volatile ConcurrentRoundRobinStrategy instance;

    private final AtomicInteger currentIndex;
    private final Lock lock;
    private final List<Service> services;

    private ConcurrentRoundRobinStrategy() {
        this.services = new CopyOnWriteArrayList<>();
        this.currentIndex = new AtomicInteger();
        this.lock = new ReentrantLock(true);
    }

    /**
     * Returns the singleton instance of ConcurrentRoundRobinStrategy.
     * <p>
     * This method ensures that only one instance of the strategy is created and
     * provides a thread-safe way to access it.
     * </p>
     * 
     * @return the singleton instance of ConcurrentRoundRobinStrategy
     */
    public static ConcurrentRoundRobinStrategy getInstance() {
        ConcurrentRoundRobinStrategy localInstance = ConcurrentRoundRobinStrategy.instance;
        if (localInstance == null) {
            synchronized (ConcurrentRoundRobinStrategy.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = new ConcurrentRoundRobinStrategy();
                }
            }
        }
        return instance;
    }

    /**
     * Adds a service to the strategy in a thread-safe manner.
     * <p>
     * This method checks if the service is valid and not already present in the
     * list before adding it. If the service is invalid or already exists, it logs
     * an appropriate message.
     * </p>
     * 
     * @param service the service to be added
     */
    @Override
    public void addServices(Service service) {
        lock.lock();
        try {
            if (Service.isInvalid(service)) {
                System.err.println("Invalid service: " + service);
                return;
            }

            if (containsService(service)) {
                System.out.println("Service already exists: " + service);
                return;
            }

            services.add(service);
            System.out.println("Added service: " + service + " at index: " + (services.size() - 1));
        } finally {
            lock.unlock();
        }
    }

    /**
     * Removes a service from the strategy in a thread-safe manner.
     * <p>
     * This method checks if the service is valid before attempting to remove it. If
     * the service is invalid or not found, it logs an appropriate message.
     * </p>
     * 
     * @param service the service to be removed
     */
    @Override
    public void removeService(Service service) {
        lock.lock();
        try {
            if (Service.isInvalid(service)) {
                System.err.println("Invalid service " + service);
                return;
            }
            int index = services.indexOf(service);
            if (index == -1) {
                System.out.println("Service not found: " + service);
                return;
            }
            services.remove(index);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Retrieves the next service in a round-robin manner.
     * <p>
     * This method returns the next service based on the current index, which is
     * updated atomically to ensure thread safety. If there are no services
     * available, it logs a message and returns null.
     * </p>
     * 
     * @return the next service or null if no services are available
     */
    @Override
    public Service getNextService() {
        if (services.isEmpty()) {
            System.out.println("No services available.");
            return null;
        }
        int size = services.size();
        int index = currentIndex.getAndUpdate(i -> (i + 1) % size) % size;
        var service = services.get(index);
        System.out.println("Next service: " + service + " at index: " + index);
        return service;
    }

    /**
     * Clears all services from the strategy in a thread-safe manner.
     * <p>
     * This method removes all services from the list and resets the current index
     * to zero.
     * </p>
     */
    @Override
    public void clearServices() {
        lock.lock();
        try {
            services.clear();
            currentIndex.set(0);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns the number of services currently managed by the strategy.
     * <p>
     * This method provides a thread-safe way to retrieve the count of services
     * without modifying the list.
     * </p>
     * 
     * @return the number of services
     */
    @Override
    public int getServiceCount() {
        return services.size();
    }

    /**
     * Checks if a service is contained in the strategy.
     * <p>
     * This method verifies if the specified service exists in the list of services
     * managed by the strategy. It returns true if the service is found, false
     * otherwise.
     * </p>
     * 
     * @param service the service to check
     * @return true if the service is contained, false otherwise
     */
    @Override
    public boolean containsService(Service service) {
        if (Service.isInvalid(service)) {
            return false;
        }
        return services.stream()
                .anyMatch(s -> s.getHost().equals(service.getHost()) && s.getPort() == service.getPort());
    }

    /**
     * Returns an array of all services currently managed by the strategy.
     * <p>
     * This method provides a thread-safe way to retrieve the list of services
     * without modifying the list. The returned array is a copy of the current
     * services.
     * </p>
     * 
     * @return an array of services
     */
    @Override
    public Service[] getServices() {
        return services.stream().toArray(Service[]::new);
    }

    /**
     * Synchronizes the strategy with a list of healthy services.
     * <p>
     * This method updates the list of services by adding new healthy services and
     * removing those that are no longer healthy. It ensures that the strategy
     * remains up-to-date with the current state of the services.
     * </p>
     * 
     * @param healthyServices an array of healthy services to synchronize with
     */
    @Override
    public void synchronizeWithHealthyServices(Service[] healthyServices) {
        lock.lock();
        try {

            // If there are no healthy services found after health check, remove all
            // services from strategy
            if (healthyServices == null || healthyServices.length == 0) {
                clearServices();
                return;
            }

            // Add new healthy services that are not already in the list
            for (Service s : healthyServices) {
                if (!containsService(s)) {
                    addServices(s);
                }
            }

            Set<Service> healthySet = Arrays.stream(healthyServices).collect(Collectors.toSet());

            var unhealthyServices = services.stream().filter(s -> !healthySet.contains(s)).collect(Collectors.toList());

            var indicesOfUnhealthyServices = unhealthyServices.stream().map(services::indexOf)
                    .collect(Collectors.toList());

            var numRemoveIndicesBeforeCurrentIndex = indicesOfUnhealthyServices.stream()
                    .filter(index -> index < currentIndex.get()).count();

            // Remove services that are not in the healthy list
            var removed = services.removeIf(s -> !healthySet.contains(s));

            // Update the current index to account for removed services
            if (numRemoveIndicesBeforeCurrentIndex > 0) {
                currentIndex.addAndGet(-(int) numRemoveIndicesBeforeCurrentIndex);
            }

            System.out.println("Removed " + (removed ? "some" : "no") + " services that are not healthy.");
        } finally {
            lock.unlock();
        }
    }
}