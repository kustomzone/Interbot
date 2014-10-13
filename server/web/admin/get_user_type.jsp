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
  String username = request.getParameter("username");
  int result = -1;
  if (authenticated && username != null) {
    UserDB user_db = UserDB.getInstance();
    result = user_db.getUserType(username);
  }
%>

<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8">
    <title>Get User Type</title>
  </head>
  <body>
    <% if (authenticated) { %>
      <%= result %>
      <hr/>
      <a href="user_admin.jsp">user admin</a>
    <% } %>
  </body>
</html>
