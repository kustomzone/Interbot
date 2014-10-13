<%-- WebCat
     Copyright (C) 2013 Tuna Oezer, General AI
     All rights reserved.
--%>

<?xml version="1.0" encoding="UTF-8"?>

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="ai.general.web.RobotUser" %>
<%@ page import="ai.general.web.User" %>
<%@ page import="ai.general.web.UserManager" %>

<%
  String status = "";
  boolean authenticated =
      request.getRemoteAddr().equals("127.0.0.1") &&
      request.getServerPort() == 8080;
  if (authenticated) {
    String username = request.getParameter("username");
    String url = request.getParameter("url");
    if (username != null && username.length() > 0 &&
        url != null && url.length() > 0) {
      User user = UserManager.getInstance().getUser(username);
      if (user != null &&
          user.getUserType() == User.UserType.Robot &&
          user.getStatus() == User.Status.Online) {
        RobotUser robot = (RobotUser) user;
        robot.sendExecutePackageRequest(url);
        status = "request sent";
      } else {
        status = "failed to send request";
      }
    }
  } else {
    response.sendError(HttpServletResponse.SC_NOT_FOUND);
  }
%>

<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8">
    <title>Execute Package</title>
  </head>
  <body>
    <p><%= status %></p>
    <form action="execute.jsp" method="GET">
      Username: <input type="text" name="username" size="50"/><br/>
      URL: <input type="text" name="url" size="75"/><br/>
      <input type="submit" value="Send"/>
    </form>
  </body>
</html>
