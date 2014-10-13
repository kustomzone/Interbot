/* WebCat
 * Copyright (C) 2013 Tuna Oezer, General AI
 * All rights reserved.
 */

package ai.general.web.video;

import ai.general.web.Participant;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Implements the video input stream endpoint.
 *
 * The video input endpoint receives video stream on a WebSocket connection. Video streams are
 * sent as chunked binary data.
 *
 * The client must specify the associated session and video channel via the session_id and channel
 * parameters. Any data sent to this input endpoint will be relayed to the receivers listening
 * the video channel.
 */
@ServerEndpoint(value="/video/in")
public class VideoInputServlet {

  /**
   * Initializes the VideoInputServlet.
   */
  public VideoInputServlet() {
    this.channel_ = null;
    this.sender_ = null;
  }

  /**
   * Called when the connection is opened.
   *
   * @param session The WebSocket session associated with the connection.
   */
  @OnOpen
  public synchronized void onOpen(Session session) {
    if (session.getRequestParameterMap().get("session_id").size() == 0 ||
        session.getRequestParameterMap().get("channel").size() == 0) {
      // Closing the session causes Tomcat to throw an exception if client sends a message.
      log.debug("video input error: missing parameters");
      return;
    }
    String session_id = session.getRequestParameterMap().get("session_id").get(0);
    String channel_id = session.getRequestParameterMap().get("channel").get(0);
    log.debug("(/{}) video input stream request for channel {}", session_id, channel_id);
    channel_ = VideoChannelManager.getInstance().getChannel(channel_id);
    if (channel_ == null) {
      log.debug("video input error: no channel");
      return;
    }
    sender_ = Participant.getFirstWithRole(
        channel_.getActivity().getParticipants(),
        VideoStreamActivityDefinition.getInstance().getRole(
            VideoStreamActivityDefinition.kRoleSender));
    if (sender_ == null || !sender_.getSession().getSessionId().equals(session_id)) {
      log.debug("video input error: wrong sender");
      return;
    }
    log.info("({}/{}) opened video input stream for channel {}",
             sender_.getUser().getUsername(), session_id, channel_id);
  }

  /**
   * Called when the connection is closed.
   */
  @OnClose
  public synchronized void onClose() {
    if (channel_ != null && sender_ != null) {
      channel_.getActivity().exit(sender_);
      log.debug("({}/{}) closed video channel {}",
                sender_.getUser().getUsername(), sender_.getSession().getSessionId(),
                channel_.getChannelId());
      channel_ = null;
      sender_ = null;
    }
  }

  /**
   * Called when a binary message is received.
   *
   * @param data The incoming binary data.
   */
  @OnMessage
  public synchronized void onMessage(byte[] data) {
    if (channel_ != null && sender_ != null) {
      log.debug("({}/{}) recieved {} bytes on channel {}",
                sender_.getUser().getUsername(), sender_.getSession().getSessionId(),
                data.length, channel_.getChannelId());
      channel_.stream(data);
    }
  }

  /**
   * Called when an error has occurred.
   *
   * @param error Error details.
   */
  @OnError
  public synchronized void onError(Throwable error) {
    log.catching(Level.DEBUG, error);
  }

  private static Logger log = LogManager.getLogger();  

  VideoChannel channel_;
  Participant sender_;
}
