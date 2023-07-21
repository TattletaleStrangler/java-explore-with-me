DROP TABLE IF EXISTS "endpointhit" CASCADE;
DROP TABLE IF EXISTS "app" CASCADE;

CREATE TABLE IF NOT EXISTS app (
    id       BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    app_name varchar(255)                            NOT NULL,
    CONSTRAINT pk_app PRIMARY KEY (id),
    CONSTRAINT eq_app_name UNIQUE (app_name)
);

CREATE TABLE IF NOT EXISTS endpointhit (
    id       BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    app_id   varchar(255)                            NOT NULL,
    uri      varchar(512)                            NOT NULL,
    ip       varchar(127)                            NOT NULL,
    datetime timestamp                               NOT NULL,
    CONSTRAINT pk_hit PRIMARY KEY (id),
    CONSTRAINT fk_hit_to_app FOREIGN KEY (app_id) REFERENCES app(id)
);