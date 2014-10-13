<%-- WebCat
     Copyright (C) 2013 Tuna Oezer, General AI
     All rights reserved.
--%>

<?xml version="1.0" encoding="UTF-8"?>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="ai.general.web.UserDB"%>

<%
  boolean authenticated =
      request.getRemoteAddr().equals("127.0.0.1") &&
      request.getServerPort() == 8080;
  if (!authenticated) {
    response.sendError(HttpServletResponse.SC_NOT_FOUND);
  }
  String username_1 = request.getParameter("username1");
  String username_2 = request.getParameter("username2");
  if (authenticated && username_1 != null && username_2 != null) {
    UserDB user_db = UserDB.getInstance();
    user_db.makeFriends(username_1, username_2);
  }
%>

<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8">
    <title>Make Friends</title>
  </head>
  <body>
    <% if (authenticated) { %>
      <p>ok</p>
      <hr/>
      <a href="user_admin.jsp">user admin</a>
    <% } %>
  </body>
</html>
