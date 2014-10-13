/* WebCat
 * Copyright (C) 2013 Tuna Oezer, General AI
 * All rights reserved.
 */

package ai.general.web.video;

import ai.general.web.Participant;

import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Implements the video output stream endpoint.
 */
@WebServlet(name="VideoOutput",
            urlPatterns="/video/out",
            asyncSupported=true)
public class VideoOutputServlet extends HttpServlet {

  public static final String kBoundaryIndicator = "frame";

  private static final long serialVersionUID = 1;

  /**
   * Streams video to the client using the video channel specified in the request.
   *
   * @param request The request.
   * @param response The response.
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) {
    String session_id = request.getParameter("session_id");
    String channel_id = request.getParameter("channel");
    if (session_id == null || channel_id == null) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    VideoChannel channel = VideoChannelManager.getInstance().getChannel(channel_id);
    if (channel == null) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    Participant participant = Participant.getFirstWithRole(
        channel.getActivity().getParticipants(),
        VideoStreamActivityDefinition.getInstance().getRole(
            VideoStreamActivityDefinition.kRoleReceiver));
    if (participant == null || !participant.getSession().getSessionId().equals(session_id)) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    log.info("({}/{}) Opened video output stream for channel {}",
             participant.getUser().getUsername(), session_id, channel.getChannelId());

    response.setContentType("multipart/x-mixed-replace;boundary=" + kBoundaryIndicator);
    try {
      VideoReceiver receiver = new VideoReceiver(participant, response.getOutputStream());
      channel.addReceiver(receiver);
      synchronized (receiver) {
        receiver.wait();
      }
    } catch (IOException e) {
    } catch (InterruptedException e) {}
    log.debug("({}/{}) exiting video out for channel {}",
              participant.getUser(), session_id, channel.getChannelId());
  }

  private static Logger log = LogManager.getLogger();  
}
