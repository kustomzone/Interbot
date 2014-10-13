<%-- WebCat
     Copyright (C) 2013 Tuna Oezer, General AI.
     All rights reserved.
--%>

<?xml version="1.0" encoding="UTF-8"?>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="ai.general.web.User"%>
<%@ page import="ai.general.web.UserManager"%>

<%
  boolean success = false;
  String session_id = (String) session.getAttribute("session_id");
  String username = null;
  String landing_page = "home.jsp";
  if (session_id != null && session_id.length() > 0) {
    username = request.getParameter("username");
    String password = request.getParameter("password");
    if (username != null && username.length() > 0 ||
        password != null && password.length() > 0) {
      User user = UserManager.getInstance().getUser(username);
      if (user != null &&
          user.webLogin(session_id, password)) {
        success = true;
      }
    }
  }
%>

<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8">
    <style>
     body {  background-color: #FFF8D8; }
    </style>
    <title>General AI</title>
  </head>
  <body>
    <% if (success) { %>
      <form name="redirect_form" action="<%= landing_page %>" method="post">
        <input type="hidden" name="username" value="<%= username %>" />
      </form>
      <script>
        document.redirect_form.submit();
      </script>
    <% } else { %>
      <p align="center">Login Error</p>
      <p align="center"><a href="login.jsp">Login again</a></p>
    <% } %>
  </body>
</html>
