<%-- WebCat
     Copyright (C) 2013 Tuna Oezer, General AI
     All rights reserved.
--%>

<?xml version="1.0" encoding="UTF-8"?>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%
  boolean authenticated =
      request.getRemoteAddr().equals("127.0.0.1") &&
      request.getServerPort() == 8080;
  if (!authenticated) {
    response.sendError(HttpServletResponse.SC_NOT_FOUND);
  }
%>

<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8">
    <title>User Admin</title>
    <script src="../js/jquery-2.0.3.min.js"></script>
    <script src="../js/sha1.min.js"></script>
    <script>
      $(document).ready(function() {
        $("[id^='submit']").click(function() {
          var id_suffix = $(this).attr("id").substring(7);
          var username_box = $("#username_" + id_suffix);
          var password_box = $("#password_" + id_suffix);
          password_box.val(SHA1(password_box.val() + username_box.val()));
          $("#form_" + id_suffix).submit();
        });
      });
    </script>
  </head>
  <body>
    <% if (authenticated) { %>
      <h3>User Exists</h3>
      <form action="user_exists.jsp" method="get">
        Username: <input type="text" name="username"/>
        <input type="submit" value="Submit"/>
      </form>
      <hr/>
      <h3>Get User Type</h3>
      <form action="get_user_type.jsp" method="get">
        Username: <input type="text" name="username"/>
        <input type="submit" value="Submit"/>
      </form>
      <hr/>
      <h3>Authenticate User</h3>
      <form action="authenticate_user.jsp" method="get" id="form_authenticate_user">
        Username: <input type="text" name="username" id="username_authenticate_user"/>
        Password: <input type="password" name="password" id="password_authenticate_user"/>
        <button id="submit_authenticate_user">Submit</button>
        <input type="submit" value="Submit Raw"/>
      </form>
      <hr/>
      <h3>Set Password</h3>
      <form action="set_password.jsp" method="get" id="form_set_password">
        Username: <input type="text" name="username" id="username_set_password"/>
        Password: <input type="password" name="password" id="password_set_password"/>
        <button id="submit_set_password">Submit</button>
        <input type="submit" value="Submit Raw"/>
      </form>
      <hr/>
      <h3>Create User</h3>
      <form action="create_user.jsp" method="get" id="form_create_user">
        Username: <input type="text" name="username" id="username_create_user"/>
        User type: <input type="number" name="usertype"/>
        Password: <input type="password" name="password" id="password_create_user"/>
        <button id="submit_create_user">Submit</button>
        <input type="submit" value="Submit Raw"/>
      </form>
      <hr/>
      <h3>List Friends</h3>
      <form action="list_friends.jsp" method="get">
        Username: <input type="text" name="username"/>
        <input type="submit" value="Submit"/>
      </form>
      <hr/>
      <h3>Make Friends</h3>
      <form action="make_friends.jsp" method="get">
        Username 1: <input type="text" name="username1"/>
        Username 2: <input type="text" name="username2"/>
        <input type="submit" value="Submit"/>
      </form>
    <% } %>
  </body>
</html>
