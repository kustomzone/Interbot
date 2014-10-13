/* WebCat
 * Copyright (C) 2013 Tuna Oezer, General AI
 * All rights reserved.
 */

/* Creates all tables. */

/* List of all users. */
CREATE TABLE IF NOT EXISTS Users(
  user_id SERIAL,
  username VARCHAR(255) UNIQUE NOT NULL,
  PRIMARY KEY(user_id)
);

/* User passwords. Passwords are hashed. */
CREATE TABLE IF NOT EXISTS Passwords(
  user_id BIGINT UNSIGNED NOT NULL,
  password TEXT NOT NULL,
  salt VARCHAR(32) NOT NULL,
  PRIMARY KEY (user_id),
  FOREIGN KEY (user_id) REFERENCES Users(user_id)
);

/* User information. */
CREATE TABLE IF NOT EXISTS UserInfo(
  id SERIAL,
  user_id BIGINT UNSIGNED NOT NULL,
  user_type INT NOT NULL,
  PRIMARY KEY(id),
  FOREIGN KEY(user_id) REFERENCES Users(user_id)
);

/* User friends */
CREATE TABLE IF NOT EXISTS Friends(
  id SERIAL,
  user_id BIGINT UNSIGNED NOT NULL,
  friend_id BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY(id),
  FOREIGN KEY(user_id) REFERENCES Users(user_id),
  FOREIGN KEY(friend_id) REFERENCES Users(user_id),
  UNIQUE(user_id, friend_id)
);
