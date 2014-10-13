<%-- WebCat
     Copyright (C) 2013 Tuna Oezer, General AI.
     All rights reserved.
--%>

<?xml version="1.0" encoding="UTF-8"?>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="ai.general.web.SessionManager"%>

<%
  String session_id = (String) session.getAttribute("session_id");
  if (session_id == null || session_id.length() == 0) {
    session_id = SessionManager.createSessionId();
    session.setAttribute("session_id", session_id);
  }
  String login_box_class = "login_box_general";
  if (SessionManager.isPhone(request.getHeader("User-Agent"))) {
    login_box_class = "login_box_phone";
  }
%>

<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8">
    <link rel="stylesheet" href="../style/common.css"/>
    <link rel="stylesheet" href="../style/login.css">
    <meta name="viewport"
          content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no"/>
    <meta http-equiv="cache-control" content="no-cache"/>
    <title>General AI</title>
    <script src="../js/jquery-2.0.3.min.js"></script>
    <script src="../js/sha1.min.js"></script>
    <script src="../js/login.min.js"></script>
  </head>
  <body>
    <div id="page">
      <span id="top_bar">
        <span id="logo">General AI - Alpha</span>
      </span>
      <span id="root_pane">
        <div id="login_box" class="<%= login_box_class %>">
          <div id="login_row_user" class="login_row">
            <span class="login_row_heading">Username</span>
            <input type="text" id="username_input" class="text_input"/>
          </div>
          <div id="login_row_password" class="login_row">
            <span class="login_row_heading">Password</span>
            <input type="password" id="password_input" class="text_input"/>
          </div>
          <div id="login_row_button" class="login_row">
            <span><input type="button" id="login_button" value="Login"/></span>
          </div>
        </div>
      </span>
    </div>
    <form name="login_form" action="dologin.jsp" method="post">
      <input type="hidden" name="username" value="" />
      <input type="hidden" name="password" value="" />
    </form>
  </body>
</html>
