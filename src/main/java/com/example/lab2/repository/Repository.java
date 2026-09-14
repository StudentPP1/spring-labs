package com.example.lab2.repository;

import java.util.List;

public interface Repository<EntityType, IdType> {
    EntityType find(IdType id);

    List<EntityType> findAll();

    void create(EntityType entity);

    void delete(IdType id);

    void update(EntityType entity);
}
