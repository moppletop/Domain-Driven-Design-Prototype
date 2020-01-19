CREATE DATABASE driver_db;

\c driver_db

-- Framework Tables
CREATE TABLE aggregate
(
    global_id            bigserial primary key,
    aggregate_identifier uuid unique not null,
    class_name           varchar(255) not null,
    event_sequence       bigint not null,
    current_state        json not null
);

CREATE TABLE event
(
    global_sequence      bigserial primary key,
    aggregate_global_id  bigint references aggregate(global_id),
    class_name           varchar(255) not null,
    payload              json not null,
    metadata             json not null
);

CREATE INDEX event_aggregate_global_id_index ON event (aggregate_global_id);
-- End of Framework Tables

-- Driver Tables
CREATE TABLE driver
(
    id             bigserial primary key,
    driver_id      uuid unique not null,
    name           varchar(64) not null,
    date_of_birth  date not null
);
-- End of Driver Tables
