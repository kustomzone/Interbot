/* General AI - Interbot
 * Copyright (C) 2013 Tuna Oezer, General AI.
 * See license.txt for copyright information.
 */

package ai.general.interbot;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents an execute package instruction sent by the server.
 *
 * The server sents this instruction usually to update software on the client.
 * A package is a zipped tar ball with a run script. The execute package instruction specifies
 * the URL of the package, which will be donwloaded and unzipped by this client.
 * Once unpacked, this client invokes the run script inside the package which contains further
 * execution instructions.
 *
 * ExecutePackageInstruction objects are deserialized from JSON.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class ExecutePackageInstruction {

  /**
   * Creates a default ExecutePackageInstruction.
   */
  public ExecutePackageInstruction() {
    url_ = "";
  }

  /**
   * Returns the full package URL.
   * The package will be downloaded from this URL.
   *
   * @return The package URL.
   */
  public String getUrl() {
    return url_;
  }

  /**
   * Sets the full package URL.
   * The package will be downloaded from this URL.
   *
   * @param url The package URL.
   */
  public void setUrl(String url) {
    this.url_ = url;
  }

  private String url_;  // URL of tar ball to download.
}
