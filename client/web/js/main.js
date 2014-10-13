// Copyright (C) 2013 Tuna Oezer, General AI.
// All rights reserved.

// Submits a request to the server.
function submitRequest(service, data) {
    $.post(service, data, function(response) {
        if (response.success) {
            window.location.reload();
        } else {
            alert("Failed to process request.");
        }
    });
}

// Submits a form to the server for processing.
function submitForm(service, form) {
    $.post(service, $("#" + form).serialize(), function(response) {
        if (response.success) {
            window.location.reload();
        } else {
            alert("Failed to process request.");
        }
    });
}

// Shows the specified menu page
function showMenuPage(page_name) {
    var menu_item = $("#menu_item_" + page_name);
    menu_item.removeClass("menu_item_unselected");
    menu_item.addClass("menu_item_selected");
    var menu_page = $("#menu_page_" + page_name);
    menu_page.removeClass("menu_page_hidden");
    menu_page.addClass("menu_page_selected");
}

// Hides the specified menu page
function hideMenuPage(page_name) {
    var menu_item = $("#menu_item_" + page_name);
    menu_item.removeClass("menu_item_selected");
    menu_item.addClass("menu_item_unselected");
    var menu_page = $("#menu_page_" + page_name);
    menu_page.removeClass("menu_page_selected");
    menu_page.addClass("menu_page_hidden");
}

$(document).ready(function() {
    $("#dialog_wifi_new").dialog({
        autoOpen: false,
        resizable: false,
        modal: true,
        width: 350,
        buttons: {
            "Connect": function() {
                $(this).dialog("close");
                submitForm("wifi/add_wifi.jsp", "wifi_form");
            },
            "Cancel": function() {
                $(this).dialog("close");
            }
        }
    });

    $("#dialog_wifi_choose_ap").dialog({
        autoOpen: false,
        modal: true,
        width: 400,
        buttons: {
            "Choose": function() {
                $(this).dialog("close");
                document.wifi_form.ssid.value = $("#wifi_access_points option:selected").val();
            }
        }
    });

    $("#dialog_wifi_delete").dialog({
        autoOpen: false,
        resizable: false,
        modal: true,
        width: 350,
        buttons: {
            "Delete": function() {
                $(this).dialog("close");
                submitRequest("wifi/delete_wifi.jsp",
                              "ssid=" + $("#wifi_connections option:selected").val());
            },
            "Cancel": function() {
                $(this).dialog("close");
            }
        }
    });

    $("#dialog_poweroff").dialog({
        autoOpen: false,
        resizable: false,
        modal: true,
        width: 350,
        buttons: {
            "Power Off": function() {
                $(this).dialog("close");
                window.location.href = "poweroff.jsp";
            },
            "Cancel": function() {
                $(this).dialog("close");
            }
        }
    });

    $("#dialog_reboot").dialog({
        autoOpen: false,
        resizable: false,
        modal: true,
        width: 350,
        buttons: {
            "Reboot": function() {
                $(this).dialog("close");
                window.location.href = "reboot.jsp";
            },
            "Cancel": function() {
                $(this).dialog("close");
            }
        }
    });

    $("#dialog_add_profile").dialog({
        autoOpen: false,
        modal: true,
        width: 350,
        buttons: {
            "Add": function() {
                $(this).dialog("close");
                document.add_profile_form.key.value =
                    SHA1(document.add_profile_form.key.value +
                        document.add_profile_form.username.value);
                submitForm("profiles/add_profile.jsp", "add_profile_form");
            },
            "Cancel": function() {
                $(this).dialog("close");
            }
        }
    });

    $("#dialog_edit_profile").dialog({
        autoOpen: false,
        modal: true,
        width: 350,
        buttons: {
            "Update": function() {
                $(this).dialog("close");
                submitForm("profiles/update_profile.jsp", "edit_profile_form");
            },
            "Cancel": function() {
                $(this).dialog("close");
            }
        }
    });

    $("#dialog_delete_profile").dialog({
        autoOpen: false,
        modal: true,
        buttons: {
            "Delete": function() {
                $(this).dialog("close");
                submitRequest("profiles/delete_profile.jsp",
                              "name=" +  $("#profiles option:selected").val());
            },
            "Cancel": function() {
                $(this).dialog("close");
            }
        }
    });

    var selected_menu_page = $(".menu_item_selected").attr("menu_page");
    if (window.location.hash.length > 0) {
        hideMenuPage(selected_menu_page);
        selected_menu_page = window.location.hash.substring(1);
        showMenuPage(selected_menu_page);
    }
    $(".menu_item").click(function() {
        hideMenuPage(selected_menu_page);
        selected_menu_page = $(this).attr("menu_page");
        showMenuPage(selected_menu_page);
        window.location.hash = selected_menu_page;
    });

    $("#new_wifi_button").click(function() {
        $("#dialog_wifi_new").dialog("open");
    });

    $("#delete_wifi_button").click(function() {
        $("#dialog_wifi_delete").dialog("open");
    });

    $("#wifi_scan_button").click(function() {
        $("#dialog_wifi_choose_ap").dialog("open");
    });

    $("#poweroff_button").click(function() {
        $("#dialog_poweroff").dialog("open");
    });

    $("#reboot_button").click(function() {
        $("#dialog_reboot").dialog("open");
    });

    $("#add_profile_button").click(function() {
        $("#add_profile_key").val("");
        $("#dialog_add_profile").dialog("open");
    });

    $("#edit_profile_button").click(function() {
        $("#edit_profile_name").val($("#profiles option:selected").val());
        $("#edit_profile_username").val($("#profiles option:selected").attr("username"));
        $("#edit_profile_key").val("");
        $("#dialog_edit_profile").dialog("open");
    });

    $("#delete_profile_button").click(function() {
        $("#delete_profile_name").text($("#profiles option:selected").val());
        $("#dialog_delete_profile").dialog("open");
    });

    $("#set_default_profile_button").click(function() {
        submitRequest("profiles/set_default_profile.jsp",
                      "name=" + $("#profiles option:selected").val());
    });

    $("#login_button").click(function() {
        submitRequest("login.jsp",
                      "name=" + $("#profiles option:selected").val());
    });

    $("#logout_button").click(function() {
        submitRequest("logout.jsp", "");
    });

    $("#set_serial_button").click(function() {
        submitRequest("serial/set_serial.jsp",
                      "port=" + $("#serial_ports option:selected").val());
    });
});
