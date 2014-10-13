/* WebCat
 * Copyright (C) 2013 Tuna Oezer, General AI
 * All rights reserved.
 */

/* Creates a new user.
 * The password must be hashed with SHA-1.
 */
DELIMITER {}
CREATE PROCEDURE CreateUser(
       param_username VARCHAR(255),
       param_user_type INT,
       param_password VARCHAR(40),
       param_salt VARCHAR(32))
MODIFIES SQL DATA
BEGIN
    DECLARE local_user_id BIGINT UNSIGNED;

    IF ((SELECT UserExists(param_username)) = 0) THEN
        INSERT INTO Users(username) VALUES (param_username);
        SELECT LAST_INSERT_ID() INTO local_user_id;

        INSERT INTO Passwords(user_id, password, salt)
        VALUES (local_user_id, sha2(concat(param_salt, param_password), 512), param_salt);

        INSERT INTO UserInfo(user_id, user_type) VALUES (local_user_id, param_user_type);
    END IF;
END {}
DELIMITER ;
