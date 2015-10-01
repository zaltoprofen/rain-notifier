create table users (
  id varchar(128) primary key,
  oauth_token varchar(128) NOT NULL
) ENGINE = InnoDB, DEFAULT CHARSET = utf8mb4;

create table venues (
  id bigint primary key auto_increment,
  venue_name varchar(128) NOT NULL,
  latitude double NOT NULL,
  longitude double NOT NULL,
  last_rainfall float NOT NULL DEFAULT 0.0,
  last_update DATETIME NOT NULL DEFAULT '2000-01-01 00:00:00',
  CONSTRAINT uq_venues_name UNIQUE (venue_name)
) ENGINE = InnoDB, DEFAULT CHARSET = utf8mb4;