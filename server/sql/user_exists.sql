/* WebCat
 * Copyright (C) 2013 Tuna Oezer, General AI
 * All rights reserved.
 */

/* Returns 1 if the user exists. Otherwise returns 0. */
CREATE FUNCTION UserExists(
       param_username VARCHAR(255))
RETURNS BOOL
READS SQL DATA
RETURN
    (SELECT count(*) FROM Users WHERE username = param_username);
