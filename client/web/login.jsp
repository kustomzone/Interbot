<%-- Interbot
     Copyright (C) 2013 Tuna Oezer, General AI.
     All rights reserved.
--%>

<%@ page language="java" contentType="application/json; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="ai.general.interbot.ConnectionResult" %>
<%@ page import="ai.general.interbot.UserProfile" %>
<%@ page import="ai.general.interbot.web.InterbotWeb" %>

<%
  boolean success = false;
  boolean local_access = InterbotWeb.isLocalAddress(request.getRemoteAddr());
  if (local_access) {
    response.setContentType("application/json");
    response.setHeader("Content-Disposition", "inline");
    String profile_name = request.getParameter("name");
    UserProfile profile;
    if (profile_name != null && profile_name.length() > 0) {
      profile = InterbotWeb.Instance.getClient().getUserProfiles().findProfile(profile_name);
    } else {
      profile = InterbotWeb.Instance.getClient().getUserProfiles().defaultProfile();
    }
    if (profile != null) {
      success = InterbotWeb.Instance.getClient().login(profile) == ConnectionResult.Success;
    }
  } else {
    response.sendError(HttpServletResponse.SC_FORBIDDEN);
  }
%>

{ "success": <%= Boolean.toString(success) %> }
