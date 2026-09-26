package com.aizen.dao;

import com.aizen.exception.DatabaseException;
import java.util.List;
import java.util.Optional;

/**
 * Standard CRUD contract shared by every DAO in AiZen.
 *
 * Demonstrates: GENERICS with a bounded-style usage pattern - {@code T} is
 * the entity type and {@code ID} its primary-key type, so the same
 * interface fits UserDAO (Integer id) and ResumeDAO (Integer id) without
 * duplicating method signatures.
 *
 * @param <T>  the entity type this DAO manages
 * @param <ID> the type of the entity's primary key
 */
public interface GenericDAO<T, ID> {

    T save(T entity) throws DatabaseException;

    Optional<T> findById(ID id) throws DatabaseException;

    List<T> findAll() throws DatabaseException;

    void update(T entity) throws DatabaseException;

    void deleteById(ID id) throws DatabaseException;
}
