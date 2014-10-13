<%-- Interbot
     Copyright (C) 2013 Tuna Oezer, General AI.
     All rights reserved.
--%>

<%@ page language="java" contentType="application/json; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="ai.general.interbot.web.InterbotWeb" %>
<%@ page import="ai.general.interbot.web.Wifi" %>

<%
  boolean success = false;
  boolean local_access = InterbotWeb.isLocalAddress(request.getRemoteAddr());
  if (local_access) {
    response.setContentType("application/json");
    response.setHeader("Content-Disposition", "inline");
    String ssid = request.getParameter("ssid");
    String password = request.getParameter("password");
    if (ssid != null && ssid.length() > 0) {
      if (password == null) password = "";
      success = Wifi.addConnection(ssid, password);
    }
  }
%>

{ "success": <%= Boolean.toString(success) %> }
