CREATE TABLE IF NOT EXISTS plots (
    x INT NOT NULL,
    y INT NOT NULL,
    wood INT,
    clay INT,
    iron INT,
    stone INT,
    food INT,
    background INT,
    plot_type VARCHAR(50),
    layer INT,
    region INT,
    hospital BOOLEAN,
    sovable BOOLEAN,
    passable BOOLEAN,
    npc BOOLEAN,
    brg BOOLEAN,
    food_sum INT,
    total_sum INT,
    PRIMARY KEY (x, y)
);

DROP INDEX plots_food_idx ON plots;
CREATE INDEX plots_food_idx ON plots (food);

DROP INDEX plots_total_idx ON plots;
CREATE INDEX plots_total_idx ON plots (total);

DROP INDEX plots_sovable_idx ON plots;
CREATE INDEX plots_sovable_idx ON plots (sovable);

DROP INDEX plots_food_sum_idx ON plots;
CREATE INDEX plots_food_sum_idx ON plots (food_sum);

DROP INDEX plots_total_sum_idx ON plots;
CREATE INDEX plots_total_sum_idx ON plots (total_sum);

CREATE TABLE IF NOT EXISTS creatures (
    x INT NOT NULL,
    y INT NOT NULL,
    name VARCHAR(100),
    id VARCHAR(100),
    amount VARCHAR(100),
    PRIMARY KEY(x, y)
);

CREATE TABLE IF NOT EXISTS deposits (
    x INT NOT NULL,
    y INT NOT NULL,
    type INT NOT NULL,
    PRIMARY KEY(x, y, type)
);

CREATE TABLE IF NOT EXISTS resources (
    x INT NOT NULL,
    y INT NOT NULL,
    type INT,
    rd VARCHAR(50),
    r INT,
    PRIMARY KEY(x, y)
);

DROP INDEX resources_type_idx ON resources;
CREATE INDEX resources_type_idx ON resources (type);


CREATE TABLE IF NOT EXISTS town (
    x INT NOT NULL,
    y INT NOT NULL,
    id INT,
    owner_id INT,
    `name` VARCHAR(100),
    owner_name VARCHAR(100),
    population INT,
    alliance VARCHAR(100),
    region INT,
    race INT,
    capital BOOLEAN,
    protection BOOLEAN,
    misc1 BOOLEAN,
    abandoned BOOLEAN,
    data VARCHAR(500),
    PRIMARY KEY(x, y)
);

DROP INDEX towns_owner_idx ON town;
CREATE INDEX towns_owner_idx ON town (owner_name);

DROP INDEX towns_alliance_idx ON town;
CREATE INDEX towns_alliance_idx ON town (alliance);


CREATE TABLE IF NOT EXISTS valid_plots (
    x INT NOT NULL,
    y INT NOT NULL,
    restricted BOOLEAN,
    total_sum INT,
    food_sum INT,
    sov_count INT,
    PRIMARY KEY(x, y)
);

DROP INDEX valid_plots_total_idx ON valid_plots;
DROP INDEX valid_plots_food_idx ON valid_plots;
DROP INDEX valid_plots_sov_idx ON valid_plots;
DROP INDEX valid_plots_restricted_idx ON valid_plots;

CREATE INDEX valid_plots_total_idx ON valid_plots (total_sum);
CREATE INDEX valid_plots_food_idx ON valid_plots (food_sum);
CREATE INDEX valid_plots_sov_idx ON valid_plots (sov_count);
CREATE INDEX valid_plots_restricted_idx ON valid_plots (restricted);

CREATE TABLE IF NOT EXISTS whitelist_users (
    user_name VARCHAR(50),
    PRIMARY KEY (user_name)
);

INSERT INTO whitelist_users (user_name) VALUES ('Link');