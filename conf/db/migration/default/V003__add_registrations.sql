create table registrations (
  user_id varchar(128) NOT NULL,
  venue_id bigint NOT NULL,
  FOREIGN KEY (user_id) REFERENCES users (id) ON UPDATE CASCADE ON DELETE CASCADE,
  FOREIGN KEY (venue_id) REFERENCES venues (id) ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT uq_registrations UNIQUE (user_id, venue_id)
) ENGINE = InnoDB, DEFAULT CHARSET = utf8mb4;