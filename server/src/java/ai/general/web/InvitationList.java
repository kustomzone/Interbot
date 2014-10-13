/* WebCat
 * Copyright (C) 2013 Tuna Oezer, General AI
 * All rights reserved.
 */

package ai.general.web;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Represents a list of Invitations.
 * The invitations are stored in a map and index by the particpant ID.
 */
public class InvitationList extends HashMap<String, Invitation> {
  private static final long serialVersionUID = 1;

  /**
   * Cancels all invitations.
   */
  public void cancelAll() {
    if (size() == 0) return;
    log.debug("cancelling all {} invitations", size());
    ArrayList<Invitation> invitations = new ArrayList<Invitation>();
    invitations.addAll(values());
    for (Invitation invitation : invitations) {
      invitation.cancel();
    }
  }

  /**
   * Rejects all invitations.
   */
  public void rejectAll() {
    if (size() == 0) return;
    log.debug("rejecting all {} invitations", size());
    ArrayList<Invitation> invitations = new ArrayList<Invitation>();
    invitations.addAll(values());
    for (Invitation invitation : invitations) {
      invitation.reject();
    }
  }

  private static Logger log = LogManager.getLogger();
}
