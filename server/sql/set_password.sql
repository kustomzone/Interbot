/* WebCat
 * Copyright (C) 2013 Tuna Oezer, General AI
 * All rights reserved.
 */

/* Sets the password and salt of a user.
 * This method is intended to be used via the API. The password must be hashed with SHA-1.
 */
DELIMITER {}
CREATE PROCEDURE SetPassword(
    param_username VARCHAR(255),
    param_password VARCHAR(40),
    param_salt VARCHAR(32))
MODIFIES SQL DATA
BEGIN
    DECLARE local_user_id BIGINT UNSIGNED;

    SELECT user_id
    INTO local_user_id
    FROM Users
    WHERE username = param_username;

    IF ((SELECT count(local_user_id)) = 1) THEN
        UPDATE Passwords
        SET password = sha2(concat(param_salt, param_password), 512),
            salt = param_salt
        WHERE user_id = local_user_id;
    END IF;
END {}
DELIMITER ;
