CREATE TABLE IF NOT EXISTS plots (
    x INT NOT NULL,
    y INT NOT NULL,
    wood INT,
    clay INT,
    iron INT,
    stone INT,
    food INT,
    total INT,
    b INT,
    hos INT,
    i INT,
    l INT,
    r INT,
    sov INT,
    imp INT,
    npc INT,
    PRIMARY KEY (x, y)
);

DROP INDEX IF EXISTS plots_food_idx;
DROP INDEX IF EXISTS plots_total_idx;
CREATE INDEX plots_food_idx ON plots (food);
CREATE INDEX plots_total_idx ON plots (total);

CREATE TABLE IF NOT EXISTS creatures (
    x INT NOT NULL,
    y INT NOT NULL,
    description VARCHAR(100),
    i VARCHAR(50),
    n VARCHAR(100),
    PRIMARY KEY(x, y)
);

CREATE TABLE IF NOT EXISTS deposits (
    x INT NOT NULL,
    y INT NOT NULL,
    skin INT,
    herb INT,
    mineral INT,
    equipment INT,
    elemental_salt INT,
    rare_herb INT,
    rare_mineral INT,
    grape INT,
    animal_part INT,
    PRIMARY KEY(x, y)
);

CREATE TABLE IF NOT EXISTS resources (
    x INT NOT NULL,
    y INT NOT NULL,
    description VARCHAR(50),
    type VARCHAR(10),
    rd VARCHAR(50),
    r INT,
    PRIMARY KEY(x, y)
);

DROP INDEX IF EXISTS resources_type_idx;
CREATE INDEX resources_type_idx ON resources (type);


CREATE TABLE IF NOT EXISTS towns (
    x INT NOT NULL,
    y INT NOT NULL,
    data VARCHAR(500),
    PRIMARY KEY(x, y)
);

CREATE TABLE IF NOT EXISTS towns (
    x INT NOT NULL,
    y INT NOT NULL,
    "name" VARCHAR(100),
    owner VARCHAR(50),
    data VARCHAR(500),
    PRIMARY KEY(x, y)
);

DROP INDEX IF EXISTS towns_owner_idx;
CREATE INDEX towns_owner_idx ON towns ("owner");


CREATE TABLE IF NOT EXISTS valid_plots (
    x INT NOT NULL,
    y INT NOT NULL,
    total_sum INT,
    food_sum INT,
    sov_count INT,
    PRIMARY KEY(x, y)
);

DROP INDEX IF EXISTS valid_plots_total_idx;
DROP INDEX IF EXISTS valid_plots_food_idx;
DROP INDEX IF EXISTS valid_plots_sov_idx;

CREATE INDEX valid_plots_total_idx ON valid_plots (total_sum);
CREATE INDEX valid_plots_food_idx ON valid_plots (food_sum);
CREATE INDEX valid_plots_sov_idx ON valid_plots (sov_sum);

CREATE TABLE IF NOT EXISTS whitelist_users (
    user_name VARCHAR(50),
    PRIMARY KEY (user_name)
)

INSERT INTO whitelist_users (user_name) VALUES ('Jerric');
INSERT INTO whitelist_users (user_name) VALUES ('Cirrej');
INSERT INTO whitelist_users (user_name) VALUES ('Lady Simbul');
