<%-- WebCat
     Copyright (C) 2013 Tuna Oezer, General AI
     All rights reserved.
--%>

<?xml version="1.0" encoding="UTF-8"?>

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="ai.general.web.User" %>
<%@ page import="ai.general.web.UserManager" %>
<%@ page import="ai.general.web.UserView" %>

<%
  User[] users = new User[0];
  boolean authenticated =
      request.getRemoteAddr().equals("127.0.0.1") &&
      request.getServerPort() == 8080;
  if (authenticated) {
    UserManager user_manager = UserManager.getInstance();
    String action = request.getParameter("action");
    String username = request.getParameter("username");
    if (action != null && action.length() > 0 &&
        username != null && username.length() > 0) {
      User user = user_manager.getUser(username);
      if (user != null) {
        if (action.equals("logout")) {
          user.systemLogout();
        }
      }
      response.sendRedirect("list_users.jsp");
    } else {
      users = user_manager.listAllLoadedUsers();
    }
  } else {
    response.sendError(HttpServletResponse.SC_NOT_FOUND);
  }
%>

<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8">
    <title>List Users</title>
  </head>
  <body>
    <h1>List Users</h1>
    <p>Number of loaded users: <%= users.length %></p>
    <table border="1">
      <thead>
        <tr>
          <td><b>username</b></td>
          <td><b>type</b></td>
          <td><b>status</b></td>
          <td><b>logout</b></td>
        </tr>
      </thead>
      <tbody>
        <% for (int i = 0; i < users.length; i++) {
             User user = users[i];
             User.Status status = user.getStatus();
              String bg_color = "white";
              switch (status) {
                case Online: bg_color = "green"; break;
              }
         %>
          <tr>
            <td><%= user.getUsername() %></td>
            <td><%= user.getUserType().name() %></td>
            <td bgcolor="<%= bg_color %>"><%= status.name() %></td>
            <td>
              <form action="list_users.jsp" method="GET">
                <input type="hidden" name="action" value="logout"/>
                <input type="hidden" name="username" value="<%= user.getUsername() %>"/>
                <input type="submit" value="Logout"/>
              </form>
            </td>
          </tr>
        <% } %>
      </tbody>
    </table>
  </body>
</html>
