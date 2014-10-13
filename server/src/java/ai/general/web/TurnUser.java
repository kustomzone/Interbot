/* WebCat
 * Copyright (C) 2013 Tuna Oezer, General AI
 * All rights reserved.
 */

package ai.general.web;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

/**
 * Represents a TURN server user.
 * A TURN server is used to relay WebRTC communications between WebRTC peers.
 * The TURN protocol requires that the peers must be authenticated. This authentication is
 * separate from the main login mechanism. This class implements the TURN REST API to provide
 * temporary credentials to users involved in a TURN session.
 *
 * Following the TURN REST API specification, this class creates a username and password for the
 * TURN server based on a User account. The username and password are time sensitive and expire.
 * The expiration time depends on the TURN configuration.
 */
public class TurnUser {

  // Secret key shared with TURN server.
  // In a future version, the shared secret should be dynamically obtained from the TURN server
  // datbase.
  private static final String kSharedSecret = "xxxxxxxx";

  // SHA-1 algorithm name.
  private static final String HMAC_SHA1 = "HmacSHA1";

  private String turnuser_;
  private String password_;

  /**
   * Creates a turn user using the credentials of the specified user.
   *
   * @param user The user for which to create a TURN user.
   */
  public TurnUser(User user) {
    turnuser_ = createUsername(user);
    password_ = sign(turnuser_, kSharedSecret);
  }

  /**
   * The TURN server username.
   *
   * @return TURN server username.
   */
  public String getUsername() {
    return turnuser_;
  }

  /**
   * The TURN server password for this user.
   * The password is time sensitive and expires after some time depending on TURN server
   * configurtation.
   *
   * @return TURN server user password.
   */
  public String getPassword() {
    return password_;
  }

  /**
   * Creates a TURN username according to the TURN specification.
   *
   * @param user The user for whom to create a TURN username.
   * @return The TURN username.
   */
  private static String createUsername(User user) {
    Date now = new Date();
    long timestamp = now.getTime() / 1000;
    return "" + timestamp;
  }

  /**
   * Cryptographically signs the data given the key.
   * The returned data is formatted in base-64 encoding.
   *
   * @param data Data to sign.
   * @param key The key with which to sign the data.
   * @return The signed data in base-64 encoding.
   */
  private static String sign(String data, String key) {
    SecretKeySpec key_spec = new SecretKeySpec(key.getBytes(), HMAC_SHA1);
    try {
      Mac mac = Mac.getInstance(HMAC_SHA1);
      mac.init(key_spec);
      byte[] hmac = mac.doFinal(data.getBytes());
      return DatatypeConverter.printBase64Binary(hmac).trim();
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
      return "";
    } catch (InvalidKeyException e) {
      e.printStackTrace();
      return "";
    }
  }

  /**
   * Returns a JSON representation of the TURN server access credentials.
   * The returned JSON represenation adheres to the WebRTC protocol and can be directly used in
   * WebRTC ICE specifications.
   *
   * @param server_address The TURN server domain name or IP address.
   */
  public String toJson(String server_address) {
    return "{\"url\": \"turn:" + server_address + "\", " +
      "\"username\": \"" + getUsername() + "\", " +
      "\"credential\": \"" + getPassword() + "\"}";
  }
}
