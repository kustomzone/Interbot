<%-- WebCat
     Copyright (C) 2013 Tuna Oezer, General AI.
     All rights reserved.
--%>

<?xml version="1.0" encoding="UTF-8"?>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="ai.general.web.User"%>
<%@ page import="ai.general.web.UserManager"%>

<%
  String session_id = (String) session.getAttribute("session_id");
  String username = null;
  if (session_id != null && session_id.length() > 0) {
    username = request.getParameter("username");
    if (username != null && username.length() > 0) {
      User user = UserManager.getInstance().getUser(username);
      if (user != null) {
        user.webLogout(session_id);
      }
    }
  }
%>

<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8">
    <style>
     body { background-color: #FFF8D8; }
    </style>
    <title>General AI</title>
  </head>
  <body>
    <p align="center">
      You have been logged out from account <%= username != null ? username : "" %>.
    </p>
    <p align="center"><a href="login.jsp">Login again</a></p>
  </body>
</html>
