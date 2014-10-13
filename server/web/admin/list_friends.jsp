<%-- WebCat
     Copyright (C) 2013 Tuna Oezer, General AI
     All rights reserved.
--%>

<?xml version="1.0" encoding="UTF-8"?>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List"%>
<%@ page import="ai.general.web.UserDB"%>

<%
  boolean authenticated =
      request.getRemoteAddr().equals("127.0.0.1") &&
      request.getServerPort() == 8080;
  if (!authenticated) {
    response.sendError(HttpServletResponse.SC_NOT_FOUND);
  }
  String username = request.getParameter("username");
  List<String> friends = null;
  if (authenticated && username != null) {
    UserDB user_db = UserDB.getInstance();
    friends = user_db.listFriends(username);
  }
%>

<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8">
    <title>List Friends</title>
  </head>
  <body>
    <% if (authenticated && friends != null) { %>
      <ul>
        <% for (String friend : friends) { %>
          <li><%= friend %></li>
        <% } %>
      </ul>
      <hr/>
      <a href="user_admin.jsp">user admin</a>
    <% } %>
  </body>
</html>
