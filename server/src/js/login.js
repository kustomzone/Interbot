/* Copyright (C) 2013 Tuna Oezer, General AI.
 * All rights reserved.
 */

/**
 * Main method.
 */
$(document).ready(function() {
    var previous_username = Cookie.get("username");
    if (previous_username != null) {
        $("#username_input").val(previous_username);
    }

    $("#login_button").click(function() {
        var username = $("#username_input").val();
        if (!checkString(username)) {
            alert("Invalid username.");
            return;
        }
        var password = $("#password_input").val();
        if (!checkString(password)) {
            alert("Invalid password.");
            return;
        }
        Cookie.set("username", username, 30);
        document.login_form.username.value = username;
        document.login_form.password.value = SHA1(password + username);
        document.login_form.submit();
    });
});
