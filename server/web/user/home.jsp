<%-- WebCat
     Copyright (C) 2013 Tuna Oezer, General AI.
     All rights reserved.
--%>

<?xml version="1.0" encoding="UTF-8"?>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List"%>
<%@ page import="ai.general.web.SessionManager"%>
<%@ page import="ai.general.web.TurnUser"%>
<%@ page import="ai.general.web.User"%>
<%@ page import="ai.general.web.UserManager"%>
<%@ page import="ai.general.web.UserView"%>

<%
  String session_id = (String) session.getAttribute("session_id");
  String username = request.getParameter("username");
  UserManager user_manager = UserManager.getInstance();
  User user = null;
  String friends_json = "[]";
  String ice_servers = "[]";
  boolean is_phone = false;
  String css_suffix = "";  // for phones styles
  if (session_id != null && session_id.length() > 0 &&
      username != null && username.length() > 0) {
    user = user_manager.getUser(username);
  }
  if (user != null && user.isLoggedInWithSessionId(session_id)) {
    friends_json = user.getFriendsAsJson();
    TurnUser turnuser = new TurnUser(user);
    ice_servers = "[" + turnuser.toJson("general.ai") + "]";
    is_phone = SessionManager.isPhone(request.getHeader("User-Agent"));
    if (is_phone) {
      css_suffix = "_phone";
    }
  } else {
    response.sendRedirect("login.jsp");
  }
%>

<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8">
    <link rel="stylesheet" href="../style/smoothness/jquery-ui-1.10.3.custom.min.css"/>
    <link rel="stylesheet" href="../style/common.css"/>
    <link rel="stylesheet" href="../style/home.css"/>
    <meta name="viewport"
          content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no"/>
    <meta http-equiv="cache-control" content="no-cache"/>
    <title>General AI</title>
    <script src="../js/jquery-2.0.3.min.js"></script>
    <script src="../js/jquery-ui-1.10.3.custom.min.js"></script>
    <script src="../js/autobahn.min.js"></script>
    <script src="../js/sha1.min.js"></script>
    <script>
      var g_session_id = "<%= session_id != null ? session_id : "" %>";
      var g_user_info = <%= user != null ? user.toJson() : "" %>;
      var g_friends = <%= friends_json %>;
      var g_use_webrtc = true;
      var g_ice_servers = <%= ice_servers %>;
    </script>
    <script src="../js/<%= is_phone ? "home_mobile.min.js" : "home.min.js" %>"></script>

    <% /* %>
      <script src="../js/debug/channel.js"></script>
      <script src="../js/debug/compatible.js"></script>
      <script src="../js/debug/webrtc.js"></script>
      <script src="../js/debug/activity.js"></script>
      <script src="../js/debug/robot.js"></script>
      <script src="../js/debug/<%= is_phone ? "controller_mobile.js" : "controller.js" %>">
      </script>
      <script src="../js/debug/valid.js"></script>
      <script src="../js/debug/user.js"></script>
      <script src="../js/debug/home.js"></script>
    <% */ %>
  </head>
  <body>
    <% if (user != null) { %>
      <div id="page">
        <span id="top_bar">
          <span id="username" class="username<%= css_suffix %>"><%= user.getUsername() %></span>
          <button id="button_logout">Log Out</button>
        </span>
        <span id="root_pane">

          <% if (user.getUserType() == User.UserType.Human) { %>
            <span id="robot_list_view" class="view view_selected">
              <table class="user_table<%= css_suffix %> center">
                <thead>
                  <th>Robot</th>
                  <th>Status</th>
                  <th>Control</th>
                </thead>
                <tbody>
                  <%
                    List<UserView> friends = user.getFriends();
                    for (UserView friend : friends) {
                      if (friend.getUserType() == User.UserType.Robot) {
                        boolean is_connected = friend.hasCapability("control", "robot");
                        String control_button_class = "button_start_control" +
                          (is_connected ? "" : " button_start_control_hidden");
                        String status = is_connected ? "Online" : "Offline";
                  %>
                        <tr id="user_list_row_<%= friend.getUsername() %>"
                            class="user_list_row user_list_row_<%= status %>"
                            username="<%= friend.getUsername() %>">
                          <td class="username_cell" username="<%= friend.getUsername() %>">
                            <span class="link"><%= friend.getUsername() %></span>
                          </td>
                          <td id="user_status_<%= friend.getUsername() %>"
                              class="user_list_cell">
                            <%= status %>
                          </td>
                          <td class="user_list_cell">
                            <button id="button_start_control_<%= friend.getUsername() %>"
                                    class="<%= control_button_class %>"
                                    username="<%= friend.getUsername() %>">
                              Start
                            </button>
                          </td>
                        </tr>
                  <%
                      }
                    }
                  %>
                </tbody>
              </table>
            </span>
          <% } %>

          <span id="robot_detail_view"
                class="view <%= user.getUserType() == User.UserType.Robot ?
                                "view_selected" : "view_unselected" %>">
            <% if (user.getUserType() == User.UserType.Human) { %>
              <p id="area_return_to_robot_list_view">
                <span id="link_return_to_robot_list_view" class="link">Return to Robots List</span>
              </p>
            <% } %>
            <table class="robot_properties_table<%= css_suffix %> center">
              <thead>
                <th>Property</th>
                <th>Value</th>
              </thead>
              <tbody>
                <tr>
                  <td class="property_name">Username</td>
                  <td id="property_value_username" class="property_value"></td>
                </tr>
                <tr>
                  <td class="property_name">Robot</td>
                  <td id="property_value_robot_status" class="property_value text_offline"></td>
                </tr>
                <tr>
                  <td class="property_name">WebRTC</td>
                  <td id="property_value_webrtc_status" class="property_value text_offline"></td>
                </tr>
                <tr>
                  <td class="property_name">Local IP</td>
                  <td class="property_value">
                    <a id="property_value_local_ip" href="http://127.0.0.1"></a>
                  </td>
                </tr>
              </tbody>
            </table>
            <div id="area_robot_detail_start_control" class="center">
              <button id="button_robot_detail_start_control"
                      class="button_start_control_hidden"
                      username="">
                Start Control
              </button>
            </div>
          </span>

          <span id="robot_control_view" class="view view_unselected">
            <div id="video_panel">
              <video id="remote_video" autoplay="autoplay"></video>
              <video id="local_video" autoplay="autoplay"></video>
              <img id="robot_video"/>
            </div>
            <div id="console_panel">
              <button id="button_disconnect"
                      class="button_console"
                      title="Disconnect">
                <image src="../img/end_control.jpeg"/>
              </button>
              <button id ="button_video"
                      class="button_console"
                      title="Start Robot Video">
                <image id="button_video_image" src="../img/camera.jpeg"/>
              </button>
              <button id="button_webrtc"
                      class="button_console"
                      title="WebRTC Call">
                <image id="button_webrtc_image" src="../img/call.jpeg"/>
              </button>
              <button id="button_fullscreen"
                      class="button_console"
                      title="Enter Fullscreen">
                <image id="button_fullscreen_image" src="../img/enter_fullscreen.jpeg"/>
              </button>
              <% if (user.getUserType() == User.UserType.Human) { %>
                <span id="ping_text">
                  <%= is_phone ? "" : "Latency: " %>
                  <span id="ping_result">0</span> ms
                </span>
              <% } %>
            </div>
          </span>

        </span>
      </div>

      <div id="dialog_calling" title="Calling...">
        <p>Calling <span id="dialog_calling_username"></span> ...</p>
      </div>
      <div id="dialog_receiving" title="Incoming Call">
        <p><span id="dialog_receiving_username"></span> is calling you.</p>
      </div>
      <div id="dialog_call_declined" title="Call Declined">
        <p>Your call was declined.</p>
      </div>
      <div id="dialog_control_terminated" title="Call Declined">
        <p>The connection was remotely terminated.</p>
      </div>
      <div id="dialog_account_settings" title="Account Settings">
        <p><button id="dialog_account_settings_change_password">Change Password</button></p>
      </div>
      <div id="dialog_change_password" title="Change Password">
        <form>
          <label for="current_password">Current Password:</label>
          <input type="password" id="current_password"/>
          <br/>
          <label for="new_password">New Password:</label>
          <input type="password" id="new_password"/>
        </form>
      </div>

      <audio id="sound_ring">
        <source src="../sounds/ring.ogg" type="audio/ogg" />
      </audio>
      <audio id="sound_calling">
        <source src="../sounds/calling.ogg" type="audio/ogg" />
      </audio>

      <form name="logout_form" action="logout.jsp" method="post">
        <input type="hidden" name="username" value="<%= username %>" />
      </form>
    <% } %>
  </body>
</html>
