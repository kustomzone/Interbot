<%-- Interbot
     Copyright (C) 2013 Tuna Oezer, General AI.
     All rights reserved.
--%>

<%@ page language="java" contentType="application/json; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="ai.general.interbot.web.InterbotWeb" %>

<%
  boolean local_access = InterbotWeb.isLocalAddress(request.getRemoteAddr());
  if (local_access) {
    response.setContentType("application/json");
    response.setHeader("Content-Disposition", "inline");
    InterbotWeb.Instance.getClient().logout();
  } else {
    response.sendError(HttpServletResponse.SC_FORBIDDEN);
  }
%>

{ "success": true }
