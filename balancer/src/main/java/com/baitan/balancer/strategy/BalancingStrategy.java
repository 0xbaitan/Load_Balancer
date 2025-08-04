package com.baitan.balancer.strategy;

import com.baitan.balancer.Service;

/**
 * Interface representing a balancing strategy for managing a collection of
 * services.
 * <p>
 * This interface defines methods for adding, removing, and retrieving services,
 * as well as synchronizing the strategy with a list of healthy services.
 * </p>
 * 
 * @author Tanish Baidya
 */
public interface BalancingStrategy {

    /**
     * Adds a server to the balancing strategy.
     *
     * @param service the service to be added
     */
    void addServices(Service service);

    /**
     * Removes a server from the balancing strategy.
     *
     * @param service the service to be removed
     */
    void removeService(Service service);

    /**
     * Clears all services from the balancing strategy.
     */
    void clearServices();

    /**
     * Checks if the balancing strategy contains a specific service.
     *
     * @param service the service to check
     * @return true if the service is present, false otherwise
     */
    int getServiceCount();

    /**
     * Retrieves the next service based on the balancing strategy.
     *
     * @return the next service to be used
     */
    Service getNextService();

    /**
     * Checks if the balancing strategy contains a specific service.
     *
     * @param service the service to check
     * @return true if the service is present, false otherwise
     */
    boolean containsService(Service service);

    /**
     * Retrieves all services managed by the balancing strategy.
     *
     * @return an array of services
     */
    Service[] getServices();

    /**
     * Synchronizes the balancing strategy with a list of healthy services.
     * <p>
     * This method updates the internal list of services to match the provided
     * healthy services, adding new ones and removing those that are no longer
     * healthy.
     * </p>
     *
     * @param healthyServices an array of healthy services to synchronize with
     */
    void synchronizeWithHealthyServices(Service[] healthyServices);

}
