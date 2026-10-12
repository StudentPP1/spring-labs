create table if not exists tasks (
    id serial primary key,
    title varchar(255) not null,
    description TEXT,
    priority int not null,
    completed boolean not null default false,
    date date not null,
    recursive_type int not null default -1
);

INSERT INTO tasks (title, description, priority, completed, date, recursive_type) VALUES
('Ранкова зарядка',          '30 хвилин розтяжки та кардіо',           3, false, CURRENT_DATE,           1),
('Купити продукти',          'Молоко, хліб, яйця, овочі',              2, false, CURRENT_DATE + 1,       -1),
('Зробити лабораторну №5',   'JdbcTemplate + JdbcClient + транзакції', 3, false, CURRENT_DATE + 2,       -1),
('Підготувати звіт',         'Опис виконаної роботи та скріншоти',     2, false, CURRENT_DATE + 3,       -1),
('Тижневий огляд коду',      'Code review команди',                    1, false, CURRENT_DATE + 7,        2)
('Виконана задача (приклад)', 'Вже закрита',                           1, true,  CURRENT_DATE - 2,       -1);