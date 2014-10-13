/* Copyright (C) 2013 Tuna Oezer, General AI.
 * All rights reserved.
 */

/**
 * Returns true if ch is a permissible username or password character.
 *
 * @param ch Character to check.
 * @return True if ch is a permissble character.
 */
function validChar(ch) {
    return (ch >= 'a' && ch <= 'z') ||
      (ch >= 'A' && ch <= 'Z') ||
      (ch >= '0' && ch <= '9') ||
      ch == '-' || ch =='.' || ch == '@' || ch == '_';
}

/**
 * Checks whether str is a permissible username or password string.
 *
 * @param str String to check.
 * @return True if str is a permissible string.
 */
function checkString(str) {
    if (str.length == 0 || str.length > 64) return false;
    for (var i = 0; i < str.length; i++) {
        if (!validChar(str[i])) return false;
    }
    return true;
}
