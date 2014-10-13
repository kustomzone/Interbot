/* WebCat
 * Copyright (C) 2013 Tuna Oezer, General AI
 * All rights reserved.
 */

package ai.general.web;

import ai.general.common.RandomString;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Represents the user database.
 *
 * Uses JDBC and Tomcat's connection pool to connect to the database. The connection details are
 * are configured in the META-INF/context.xml file.
 *
 * UserDB provides an API to query and update users in the user database. UserDB validates
 * input and checks for SQL injection attacks.
 *
 * UserDB is a singleton class.
 * UserDB is thread-safe.
 */
public class UserDB {

  /*
   * SQL queries.
   * Any ? placeholders are replaced by actual values supplied to the query method.
   * The n-th argument to the query method is used to replace the n-th ? placeholder.
   */

  // argument: username
  private static final String kQueryUserExists = "SELECT UserExists('?');";

  // argument: username
  private static final String kQueryGetUserType =
    "SELECT user_type " +
    "FROM UserInfo " +
    "NATURAL JOIN Users " +
    "WHERE username = '?';";

  // arguments: username, password
  private static final String kQueryAuthenticateUser =
    "SELECT VerifyUserCredentials('?','?');";

  // argument: username
  private static final String kQueryListFriends =
    "SELECT username " +
    "FROM Users " +
    "INNER JOIN Friends ON Users.user_id = Friends.friend_id " +
    "WHERE Friends.user_id = (SELECT user_id FROM Users WHERE username = '?');";

  // arguments: username, password, salt
  private static final String kCallSetPassword = "CALL SetPassword('?','?','?');";

  // arguments: username, user type, password, salt
  private static final String kCallCreateUser = "CALL CreateUser('?',?,'?','?');";

  // arguments: username 1, username 2
  private static final String kCallMakeFriends = "CALL MakeFriends('?','?');";

  /**
   * Represents a SQL query. Executes a query and provides access to the query results.
   * A Query instance can be used for only one query. When processing is complete the
   * {@link #close()} method must be called to release resources.
   */
  private class Query {

    /**
     * Construct a query for the specified query string which may contain placeholders.
     *
     * @param query The query string may contain placeholders.
     */
    public Query(String query) {
      this.query_ = query;
      connection_ = null;
      statement_ = null;
      rows_ = null;
      exception_ = null;
    }

    /**
     * Returns the result rows for queries that produce rows. Returns null if there are no
     * result rows. This method returns a valid result only after a query has been executed
     * and before it has been closed.
     *
     * @return The result rows.
     */
    public ResultSet getRows() {
      return rows_;
    }

    /**
     * Returns whether an exception was encountered during query processing.
     * The exception can be obtained with the {@link #getException} method.
     *
     * @return True if an exception was encountered.
     */
    public boolean hasException() {
      return exception_ != null;
    }

    /**
     * Returns the SQL exception if an exception was encountered during query processing.
     * Returns null if no exception was encountered.
     *
     * @return The exception or null.
     */
    public SQLException getException() {
      return exception_;
    }

    /**
     * Executes the query. This method can be only used for queries that result in rows, e.g.
     * SELECT queries.
     * The supplied arguments are used to replace any placeholders in the query string in the
     * order in which they are defined.
     * Any results can be obtained via the {@link #getRows()} method.
     *
     * After this method has been called the {@link #close()} method must always be called to
     * release resources acquired by this method.
     *
     * @param args Actual values to use for placeholder characters in the query string.
     * @return True if no exceptions were encountered.
     */
    public boolean executeQuery(Object ... args) {
      for (Object arg : args) {
        query_ = query_.replaceFirst("\\?", arg.toString());
      }
      try {
        connection_ = database_.getConnection();
        statement_ = connection_.createStatement();
        rows_ = statement_.executeQuery(query_);
        return true;
      } catch (SQLException e) {
        log.catching(Level.INFO, e);
        exception_ = e;
        return false;
      }
    }

    /**
     * Executes a stored procedure. This method can be only used with CALL queries.
     * The supplied arguments are used to replace any placeholders in the query string in the
     * order in which they are defined.
     * This execution of this method does not provide any result rows.
     *
     * After this method has been called the {@link #close()} method must always be called to
     * release resources acquired by this method.
     *
     * @param args Actual values to use for placeholder characters in the query string.
     * @return True if no exceptions were encountered.
     */
    public boolean executeCall(Object ... args) {
      for (Object arg : args) {
        query_ = query_.replaceFirst("\\?", arg.toString());
      }
      try {
        connection_ = database_.getConnection();
        statement_ = connection_.createStatement();
        statement_.execute(query_);
        return true;
      } catch (SQLException e) {
        log.catching(Level.INFO, e);
        exception_ = e;
        return false;
      }
    }

    /**
     * Releases any resources acquired during query processing. This method must be called to
     * avoid any resource leaks. This method can always be safely called. After this method
     * has been called, the Query instance can no longer be used.
     */
    public void close() {
      try {
        if (rows_ != null) rows_.close();
      } catch (SQLException e) {
        log.catching(Level.INFO, e);
      }
      try {
        if (statement_ != null) statement_.close();
      } catch (SQLException e) {
        log.catching(Level.INFO, e);
      }
      try {
        if (connection_ != null && !connection_.isClosed()) connection_.close();
      } catch (SQLException e) {
        log.catching(Level.INFO, e);
      }
    }

    private String query_;
    private Connection connection_;
    private Statement statement_;
    private ResultSet rows_;
    private SQLException exception_;
  }

  /**
   * UserDB is singleton. Use {@link #getInstance()} to create an instance.
   */
  public UserDB() {
    try {
      Context context = new InitialContext();
      context = (Context) context.lookup("java:comp/env");
      if (context == null) return;
      database_ = (DataSource) context.lookup("jdbc/UserDB");
    } catch (NamingException e) {
        log.catching(Level.ERROR, e);
    } catch (ClassCastException e) {
        log.catching(Level.ERROR, e);
    }
  }

  /**
   * Provides access to the singleton UserDB instance.
   *
   * @return The singleton Directory instance.
   */
  public static UserDB getInstance() {
    return Singleton.get(UserDB.class);
  }

  /**
   * Verifies that the supplied string may appear in SQL queries. This method is used to prevent
   * SQL injection attacks and malformed queries.
   */
  public static boolean checkString(String s) {
    if (s == null) return false;
    if (s.length() > 64) return false;
    for (int i = 0; i < s.length(); i++) {
      if (!validChar(s.charAt(i))) return false;
    }
    if (s.indexOf("--") >= 0) return false;
    if (s.toLowerCase().indexOf("delimiter") >= 0) return false;
    return true;
  }

  /**
   * Checks whether an account for the specified username exists.
   *
   * @param username Username for account.
   * @return True if the username exists.
   */
  public boolean userExists(String username) {
    if (!checkString(username)) return false;
    boolean result = false;
    Query query = new Query(kQueryUserExists);
    if (query.executeQuery(username)) {
      try {
        if (query.getRows().next()) {
          result = query.getRows().getBoolean(1);
        }
      } catch (SQLException e) {
        log.catching(Level.INFO, e);
      }
    }
    query.close();
    return result;
  }

  /**
   * Retrieves the user type from the UserInfo table for the specified user.
   *
   * @param username Username for whom to retrieve user type.
   * @return The user type or -1 if the user does not exist.
   */
  public int getUserType(String username) {
    if (!checkString(username)) return 0;
    int result = -1;
    Query query = new Query(kQueryGetUserType);
    if (query.executeQuery(username)) {
      try {
        if (query.getRows().next()) {
          result = query.getRows().getInt(1);
        }
      } catch (SQLException e) {
        log.catching(Level.INFO, e);
      }
    }
    query.close();
    return result;
  }

  /**
   * Verifies the username + password combination. The combination is valid if the username exist
   * and the password is the password of the user.
   * The password must be hashed password. It must not be clear text.
   * The expected hash is SHA1(password + username).
   *
   * @param username Username of user to authenticate.
   * @param password Hashed password of user.
   * @return True if the username + password combination is valid.
   */
  public boolean authenticateUser(String username, String password) {
    if (!checkString(username) || !checkString(password)) return false;
    boolean result = false;
    Query query = new Query(kQueryAuthenticateUser);
    if (query.executeQuery(username, password)) {
      try {
        if (query.getRows().next()) {
          result = query.getRows().getBoolean(1);
        }
      } catch (SQLException e) {
        log.catching(Level.INFO, e);
      }
    }
    query.close();
    return result;
  }

  /**
   * Sets the password of a user. The user must exist.
   * The password must be hashed password. It must not be clear text.
   * The expected hash is SHA1(password + username).
   * This method also automatically updates the existing salt of the user.
   *
   * @param username Username of the user whose password is set.
   * @param password New hashed password of user.
   */
  public void setPassword(String username, String password) {
    if (!checkString(username) || !checkString(password)) return;
    Query query = new Query(kCallSetPassword);
    query.executeCall(username, password, createSalt());
    query.close();
  }

  /**
   * Creates a new account for the specified user. The username must not already exist.
   * The password must be hashed password. It must not be clear text.
   * The expected hash is SHA1(password + username).
   * This method automatically creates a new salt for the user.
   *
   * @param username The username of the new user account.
   * @param user_type The user type of the new user.
   * @param password The hashed password of the new user.
   */
  public void createUser(String username, int user_type, String password) {
    if (!checkString(username) || !checkString(password)) return;
    Query query = new Query(kCallCreateUser);
    query.executeCall(username, user_type, password, createSalt());
    query.close();
  }

  /**
   * Returns all friends of the specified user.
   *
   * @param username The user for which to return friends.
   * @return The list of friends of the user.
   */
  public List<String> listFriends(String username) {
    List<String> result = new ArrayList<String>();
    if (!checkString(username)) return result;
    Query query = new Query(kQueryListFriends);
    if (query.executeQuery(username)) {
      try {
        ResultSet rows = query.getRows();
        while (rows.next()) {
          result.add(rows.getString(1));
        }
      } catch (SQLException e) {
        log.catching(Level.INFO, e);
      }
    }
    query.close();
    return result;
  }

  /**
   * Makes username_1 and username_2 friends of each other. This method is symmetric. username_2
   * becomes a friend of username1 and username_1 becomes a friend of username_2.
   * username_1 and username_2 must be distinct.
   *
   * @param username_1 Username of first user.
   * @param username_2 Username of second user.
   */
  public void makeFriends(String username_1, String username_2) {
    if (!checkString(username_1) || !checkString(username_2)) return;
    if (username_1.equals(username_2)) return;
    Query query = new Query(kCallMakeFriends);
    query.executeCall(username_1, username_2);
    query.close();
  }

  /**
   * Creates a random salt.
   * The returned salt does not exceed 32 characters in length and contains only alpha-numeric
   * characters.
   *
   * @return A random salt.
   */
  private String createSalt() {
    return RandomString.nextString(32);
  }

  /**
   * Checks whether the supplied character is permissable in SQL queries. This method is used
   * to prevent SQL injection attacks and malformed requests.
   *
   * @param ch Character to check.
   * @return True if the character is permissable.
   */
  private static boolean validChar(char ch) {
    return (ch >= 'a' && ch <= 'z') ||
      (ch >= 'A' && ch <= 'Z') ||
      (ch >= '0' && ch <= '9') ||
      ch == '-' || ch =='.' || ch == '@' || ch == '_';
  }

  private static Logger log = LogManager.getLogger();

  private DataSource database_;
}
