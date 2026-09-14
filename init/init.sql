create table if not exists tasks (
    id serial primary key,
    title varchar(255) not null,
    description TEXT,
    priority int not null,
    completed boolean not null default false,
    date date not null,
    recursive_type int not null default -1
);