package com.boardwith.admin.helper;

/**
 * Callback used by {@link BusinessIdGenerator} to verify whether a
 * {@code businessId} is already taken in the database.
 */
@FunctionalInterface
public interface BusinessIdExistsChecker {

    /**
     * @return {@code true} if the given businessId already exists.
     */
    boolean exists(String businessId);
}