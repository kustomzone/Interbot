/* Copyright (C) 2013 Tuna Oezer, General AI.
 * All rights reserved.
 */

/**
 * Cookie API.
 * Allows reading and setting Cookies.
 */
var Cookie = {
    /**
     * Returns the value of a Cookie or null if there is no such cookie.
     *
     * @param name The name of the cookie.
     */
    get: function(name) {
        name = name + "=";
        var cookies = document.cookie.split(";");
        for (var i = 0; i < cookies.length; i++) {
            if (cookies[i].indexOf(name) >= 0) {
                return cookies[i].substring(cookies[i].indexOf("=") + 1);
            }
        }
        return null;
    },

    /**
     * Sets the value of a Cookie.
     * This method always sets cookiees for the current domain and path.
     *
     * @param name The name of the cookie.
     * @param value The value of the cookie.
     * @param valud_for_days The maximum number of days this cookie will be stored.
     */
    set: function(name, value, valid_for_days) {
        document.cookie = name + "=" + value + "; max-age=" + (86400 * valid_for_days);
    },
};
