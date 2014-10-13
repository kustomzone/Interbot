/* WebCat
 * Copyright (C) 2013 Tuna Oezer, General AI
 * All rights reserved.
 */

/* Verifies the credentials of a user.
 * Returns true if and only if param_password is the password of param_username
 */
CREATE FUNCTION VerifyUserCredentials(
    param_username VARCHAR(255),
    param_password VARCHAR(64))
RETURNS BOOL
READS SQL DATA
RETURN
    (SELECT count(stored.password) > 0 AND
            sha2(concat(stored.salt, param_password), 512) = convert(stored.password USING utf8)
     FROM
         (SELECT password, salt
          FROM Passwords
          NATURAL JOIN Users
          WHERE username = param_username)
         AS stored);
