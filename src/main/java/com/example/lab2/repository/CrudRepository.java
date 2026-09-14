package com.example.lab2.repository;

import com.example.lab2.annotation.Column;
import com.example.lab2.annotation.Id;
import com.example.lab2.annotation.Table;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class CrudRepository<EntityType, IdType> implements Repository<EntityType, IdType> {
    private final JdbcTemplate jdbcTemplate;
    private final String tableName;
    private final Class<EntityType> entityType;
    private String SELECT_QUERY;
    private String SELECT_QUERY_BY_ID;
    private String CREATE_QUERY;
    private String DELETE_QUERY;
    private String UPDATE_QUERY;

    protected CrudRepository(Class<EntityType> entityType, DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        Table tableAnnotation = entityType.getAnnotation(Table.class);
        this.tableName = tableAnnotation.name();
        Objects.requireNonNull(this.tableName);
        this.entityType = entityType;
        prepareStatements();
    }

    private void prepareStatements() {
        Field[] fields = entityType.getDeclaredFields();
        String idColumnName = null;
        List<String> columns = new ArrayList<>();
        for (Field field : fields) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(Id.class)) {
                idColumnName = field.getAnnotation(Id.class).name();
            }
            if (field.isAnnotationPresent(Column.class)) {
                columns.add(field.getAnnotation(Column.class).name());
            }
        }
        Objects.requireNonNull(idColumnName);
        StringBuilder insertingFields = new StringBuilder("(");
        StringBuilder values = new StringBuilder("(");
        StringBuilder setFields = new StringBuilder();
        for (int i = 0; i < columns.size(); i++) {
            insertingFields = i != columns.size() - 1 ? insertingFields.append("?,") : insertingFields.append("?);");
            values = i != columns.size() - 1 ? values.append(columns.get(i)).append(",") : values.append(columns.get(i)).append(")");
            setFields = i != columns.size() - 1 ? setFields.append(columns.get(i)).append("=?,") : setFields.append(columns.get(i)).append("=?");
        }
        this.SELECT_QUERY_BY_ID = "SELECT * FROM " + tableName + " WHERE " + idColumnName + " = ?;";
        this.SELECT_QUERY = "SELECT * FROM " + tableName + ";";
        this.CREATE_QUERY = "INSERT INTO " + tableName + values + " VALUES " + insertingFields;
        this.DELETE_QUERY = "DELETE FROM " + tableName + " WHERE " + idColumnName + " = ?;";
        this.UPDATE_QUERY = "UPDATE " + tableName + " SET " + setFields + " WHERE " + idColumnName + " = ?;";
    }

    private Object[] extractValues(EntityType type, boolean isUpdate) {
        List<Object> values = new ArrayList<>();
        Object idValue = null;
        try {
            for (Field field : entityType.getDeclaredFields()) {
                field.setAccessible(true);
                if (field.isAnnotationPresent(Id.class)) {
                    idValue = field.get(type);
                }
                if (field.isAnnotationPresent(Column.class)) {
                    values.add(field.get(type));
                }
            }
            if (isUpdate) {
                values.add(idValue);
            }
            return values.toArray();
        } catch (IllegalAccessException _) {
            throw new RuntimeException("Can't get value from field of class: " + type.getClass());
        }
    }

    public EntityType find(IdType id) {
        return this.jdbcTemplate.queryForObject(
                SELECT_QUERY_BY_ID,
                new BeanPropertyRowMapper<>(entityType),
                id);
    }

    public List<EntityType> findAll() {
        return this.jdbcTemplate.query(
                SELECT_QUERY,
                new BeanPropertyRowMapper<>(entityType)
        );
    }

    public void create(EntityType entity) {
        this.jdbcTemplate.update(CREATE_QUERY, extractValues(entity, false));
    }

    public void delete(IdType id) {
        this.jdbcTemplate.update(DELETE_QUERY, id);
    }

    public void update(EntityType entity) {
        this.jdbcTemplate.update(UPDATE_QUERY, extractValues(entity, true));
    }
}
