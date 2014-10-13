/* WebCat
 * Copyright (C) 2013 Tuna Oezer, General AI
 * All rights reserved.
 */

/* Makes username_1 and username_2 friends of each other.
 * This method is symmetric. username_1 becomes a friend of username_2 and username_2
 * becomes a friend of username_1.
 * This procedure has no effect if either username_1 or username_2 does not exist.
 */
DELIMITER {}
CREATE PROCEDURE MakeFriends(
       param_username_1 VARCHAR(255),
       param_username_2 VARCHAR(255))
MODIFIES SQL DATA
BEGIN
    DECLARE local_user_id_1 BIGINT UNSIGNED;
    DECLARE local_user_id_2 BIGINT UNSIGNED;

    SELECT user_id INTO local_user_id_1 FROM Users WHERE username = param_username_1;
    SELECT user_id INTO local_user_id_2 FROM Users WHERE username = param_username_2;
    IF (((SELECT count(local_user_id_1)) = 1) AND ((SELECT count(local_user_id_2)) = 1)) THEN
        START TRANSACTION;
            INSERT INTO Friends(user_id, friend_id) VALUES(local_user_id_1, local_user_id_2);
            INSERT INTO Friends(user_id, friend_id) VALUES(local_user_id_2, local_user_id_1);
        COMMIT;
    END IF;
END {}
DELIMITER ;
