<%-- Interbot
     Copyright (C) 2013 Tuna Oezer, General AI.
     All rights reserved.
--%>

<?xml version="1.0" encoding="UTF-8"?>

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="ai.general.interbot.UserProfile" %>
<%@ page import="ai.general.interbot.UserProfiles" %>
<%@ page import="ai.general.interbot.web.InterbotWeb" %>
<%@ page import="ai.general.interbot.web.Wifi" %>
<%@ page import="ai.general.plugin.PluginManager" %>
<%@ page import="ai.general.scriptduino.Scriptduino" %>
<%@ page import="ai.general.scriptduino.ScriptduinoPlugin" %>

<%
  Scriptduino scriptduino = null;
  ScriptduinoPlugin scriptduino_plugin =
      (ScriptduinoPlugin) PluginManager.Instance.getPlugin("Scriptduino");

  boolean local_access = InterbotWeb.isLocalAddress(request.getRemoteAddr());
  if (local_access) {
    if (scriptduino_plugin != null) {
      scriptduino = scriptduino_plugin.getScriptduino();
      if (scriptduino.getScriptduinoConfig() == null ||
          scriptduino.listSerialPorts() == null) {
        scriptduino = null;
      }
    }
  } else {
    response.sendError(HttpServletResponse.SC_FORBIDDEN);
  }

%>

<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no"/>
    <link rel="stylesheet" href="style/smoothness/jquery-ui-1.10.3.custom.min.css"/>
    <link rel="stylesheet" href="style/main.css"/>
    <title>General AI - Robot</title>
    <script src="js/jquery-2.0.3.min.js"></script>
    <script src="js/jquery-ui-1.10.3.custom.min.js"></script>
    <script src="js/sha1.min.js"></script>
    <script src="js/main.js"></script>
  </head>
  <body>
    <% if (local_access) { %>

    <div id="page">
      <span id="top_bar">
        <span id="logo">General AI</span>
      </span>
      <span id="root_pane">
        <% if (InterbotWeb.Instance.isInitialized()) { %>
          <span id="menu_pane">
	    <table id="menu_list">
              <tr>
                <td id="menu_item_wifi"
                    class="menu_item menu_item_selected"
                    menu_page="wifi">
                  &nbsp;<img src="img/wifi.jpeg"/> WIFI
                </td>
              </tr>
              <tr>
                <td id="menu_item_power"
                    class="menu_item menu_item_unselected"
                    menu_page="power">
                  &nbsp;<img src="img/poweroff.jpeg"/> Power
                </td>
              </tr>
              <tr>
                <td id="menu_item_profile"
                    class="menu_item menu_item_unselected"
                    menu_page="profile">
                  &nbsp;<img src="img/profile.jpeg"/> Profiles
                </td>
              </tr>
              <tr>
                <td id="menu_item_serial"
                    class="menu_item menu_item_unselected"
                    menu_page="serial">
                  &nbsp;<img src="img/serial.jpeg"/> Serial
                </td>
              </tr>
            </table>
          </span>

          <span id="menu_page_wifi" class="menu_page menu_page_selected">
            <div class="menu_content">
              <p>
                <button id="new_wifi_button" class="action_button" title="New Wifi">
                  <img src="img/wifi_ap.jpeg"/> New Wifi
                </button>
              </p>
              <p>
                <button id="delete_wifi_button" class="action_button" title="Delete Wifi">
                  <img src="img/delete.jpeg"/> Delete Wifi
                </button>
              </p>
            </div>
          </span>
          <span id="menu_page_power" class="menu_page menu_page_hidden">
            <div class="menu_content">
              <p>
                <button id="poweroff_button" class="action_button" title="Turn Off Robot">
                  <img src="img/poweroff.jpeg"/> Power Off
                </button>
             </p>
              <p>
                <button id="reboot_button" class="action_button" title="Turn Off Robot">
                  <img src="img/reboot.jpeg"/> Reboot
                </button>
              </p>
            </div>
          </span>
          <span id="menu_page_profile" class="menu_page menu_page_hidden">
            <div class="menu_content">
              <span>
                Logged in as:
                <% if (InterbotWeb.Instance.getClient().getCurrentUserProfile() == null) { %>
                  not logged in
                <% } else { %>
                  <%= InterbotWeb.Instance.getClient().getCurrentUserProfile().getName() %>
                <% } %>
              </span>
              <p>
                <select id="profiles" class="drop_down">
                  <%
                    UserProfiles profiles = InterbotWeb.Instance.getClient().getUserProfiles();
                    for (UserProfile profile : profiles.getProfiles()) {
                      if (profile.getName().equals(profiles.getDefault())) {
                  %>
                    <option value="<%= profile.getName() %>"
                            username="<%= profile.getUsername() %>"
                            selected="selected">
                      <%= profile.getName() %> (default)
                    </option>
                  <%  } else { %>
                    <option value="<%= profile.getName() %>"
                            username="<%= profile.getUsername() %>">
                      <%= profile.getName() %>
                    </option>
                  <%  }
                    }
                  %>
                </select>
              </p>
              <table border="0" width="100%">
                <tr>
                  <td>
                    <button id="add_profile_button" class="action_button" title="Add Profile">
                      <img src="img/add.jpeg"/> Add
                    </button>
                  </td>
                </tr>
                <tr>
                  <td>
                    <button id="edit_profile_button" class="action_button" title="Edit Profile">
                      <img src="img/edit.jpeg"/> Edit
                    </button>
                  </td>
                </tr>
                <tr>
                  <td>
                    <button id="delete_profile_button"
                            class="action_button"
                            title="Delete Profile">
                      <img src="img/delete.jpeg"/> Delete
                    </button>
                  </td>
                </tr>
                <tr>
                  <td>
                    <button id="set_default_profile_button"
                            class="action_button"
                            title="Set as Default">
                      <img src="img/default.jpeg"/> Set Default
                    </button>
                  </td>
                </tr>
                <tr>
                  <td>
                    <button id="login_button" class="action_button" title="Login with Profile">
                      <img src="img/login.jpeg"/> Login
                    </button>
                  </td>
                </tr>
                <tr>
                  <td>
                    <button id="logout_button" class="action_button" title="Logout">
                      <img src="img/logout.jpeg"/> Logout
                    </button>
                  </td>
                </tr>
              </table>
            </div>
          </span>
          <span id="menu_page_serial" class="menu_page menu_page_hidden">
            <div class="menu_content">
              <% if (scriptduino != null) { %>
                <p>
                  <select id="serial_ports" class="drop_down">
                    <option value="(none)">(none)</option>
                    <%
                      String selected_port_name =
                          scriptduino.getScriptduinoConfig().getSerialPort();
                      List<String> serial_ports = scriptduino.listSerialPorts();
                      for (String port_name : serial_ports) {
                        if (port_name.equals(selected_port_name)) {
                    %>
                          <option value="<%= port_name %>" selected="selected">
                            <%= port_name %>
                          </option>
                    <%  } else { %>
                          <option value="<%= port_name %>"><%= port_name %></option>
                    <%  }
                      }
                    %>
                  </select>
                </p>
                <p>
                  <button id="set_serial_button" class="action_button" title="Set Port">
                    <img src="img/edit.jpeg"/> Update
                  </button>
                </p>
              <% } else { %>
                <p>Scriptduino plugin is not loaded.</p>
              <% } %>
            </div>
          </span>
        <% } else { %>
          <h1>Please wait. Booting up...</h1>
          <script>
            setTimeout(function() { window.location.reload(); }, 5000);
          </script>
        <% } %>
      </span>
    </div>

    <div id="dialog_wifi_new" title="New WIFI">
      <p><button id="wifi_scan_button">Search Networks</button></p>
      <form id="wifi_form" name="wifi_form">
        <label for="wifi_ssid">WIFI network name:</label>
        <input id="wifi_ssid"
               name="ssid"
               type="text"
               class="wifi_input"
               size="25"/>
        <br/>
        <label for="wifi_password">Password:</label>
        <input id="wifi_password"
               name="password"
               type="password"
               class="wifi_input"
               size="25"/>
      </form>
    </div>

    <div id="dialog_wifi_choose_ap" title="Access Points">
      <select id="wifi_access_points" class="drop_down">
        <% for (String ap : Wifi.listAccessPoints()) { %>
          <option value="<%= ap %>"><%= ap %></option>
        <% } %>
      </select>
    </div>

    <div id="dialog_wifi_delete" title="Delete WIFI">
      <select id="wifi_connections" class="drop_down">
        <% for (String connection : Wifi.listConnections()) { %>
          <option value="<%= connection %>"><%= connection %></option>
        <% } %>
      </select>
    </div>

    <div id="dialog_poweroff" title="Turn Off Robot">
      <p>
        <span class="ui-icon ui-icon-alert" style="float:left; margin:0 7px 20px 0;"></span>
        Do you want to turn off the robot?
      </p>
    </div>

    <div id="dialog_reboot" title="Reboot Robot">
      <p>
        <span class="ui-icon ui-icon-alert" style="float:left; margin:0 7px 20px 0;"></span>
        Do you want to reboot the robot?
      </p>
    </div>

    <div id="dialog_add_profile" title="Add Profile">
      <form id="add_profile_form" name="add_profile_form">
        <label for="add_profile_name">Profile name</label>
        <input id="add_profile_name"
               name="name"
               class="text ui-widget-content ui-corner-all"
               type="text"/>
        <br/>
        <label for="add_profile_username">User name</label>
        <input id="add_profile_username"
               name="username"
               class="text ui-widget-content ui-corner-all"
               type="text"/>
        <br/>
        <label for="add_profile_key">Password</label>
        <input id="add_profile_key"
               name="key"
               class="text ui-widget-content ui-corner-all"
               type="password"/>
      </form>
    </div>

    <div id="dialog_edit_profile" title="Edit Profile">
      <form name="edit_profile_form">
        <label for="edit_profile_name">Profile name</label>
        <input id="edit_profile_name"
               name="name"
               class="text ui-widget-content ui-corner-all"
               type="text"
               readonly="readonly"/>
        <br/>
        <label for="edit_profile_username">User name</label>
        <input id="edit_profile_username"
               name="username"
               class="text ui-widget-content ui-corner-all"
               type="text"/>
        <br/>
        <label for="edit_profile_key">Password</label>
        <input id="edit_profile_key"
               name="key"
               class="text ui-widget-content ui-corner-all"
               type="password"/>
      </form>
    </div>

    <div id="dialog_delete_profile" title="Delete Profile">
      <p>Delete profile '<span id="delete_profile_name"></span>'?</p>
    </div>

    <% } %>
  </body>
</html>
