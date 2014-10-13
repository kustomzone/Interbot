<%-- Interbot
     Copyright (C) 2013 Tuna Oezer, General AI.
     All rights reserved.
--%>

<?xml version="1.0" encoding="UTF-8"?>

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="ai.general.interbot.web.InterbotWeb" %>

<%
  boolean local_access = InterbotWeb.isLocalAddress(request.getRemoteAddr());
  if (local_access) {
    InterbotWeb.Instance.poweroff();
  } else {
    response.sendError(HttpServletResponse.SC_FORBIDDEN);
  }
%>

<!DOCTYPE html>
<html>
  <head>
    <link rel="stylesheet" href="style/main.css"/>
    <style>
      body { cursor: wait; }
    </style>
    <title>General AI - Robot</title>
    <script>
     setTimeout(function() {
       window.location.href = "index.jsp";
     }, 20000);
    </script>
  </head>
  <body>
    <div id="page">
      <span id="menu_bar">
        <span id="logo">General AI</span>
      </span>
      <span id="root_pane">
        <h1>Shutting down...</h1>
      </span>
    </div>
  </body>
</html>
