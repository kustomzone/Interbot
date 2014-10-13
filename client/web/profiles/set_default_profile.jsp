<%-- Interbot
     Copyright (C) 2013 Tuna Oezer, General AI.
     All rights reserved.
--%>

<%@ page language="java" contentType="application/json; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="ai.general.interbot.UserProfiles" %>
<%@ page import="ai.general.interbot.web.InterbotWeb" %>

<%
  boolean success = false;
  boolean local_access = InterbotWeb.isLocalAddress(request.getRemoteAddr());
  if (local_access) {
    response.setContentType("application/json");
    response.setHeader("Content-Disposition", "inline");
    String profile_name = request.getParameter("name");
    if (profile_name != null && profile_name.length() > 0) {
      UserProfiles profiles = InterbotWeb.Instance.getClient().getUserProfiles();
      profiles.setDefault(profile_name);
      success = profiles.save();
    }
  } else {
    response.sendError(HttpServletResponse.SC_FORBIDDEN);
  }
%>

{ "success": <%= Boolean.toString(success) %> }
