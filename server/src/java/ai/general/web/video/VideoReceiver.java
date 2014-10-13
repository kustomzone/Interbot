/* WebCat
 * Copyright (C) 2013 Tuna Oezer, General AI
 * All rights reserved.
 */

package ai.general.web.video;

import ai.general.web.Participant;

import javax.servlet.ServletOutputStream;

/**
 * Represents a video receiver. A video receiver is a participant in a video stream activity with
 * role receiver.
 *
 * A video receiver has an output stream that can be used to stream video to the receiver.
 *
 * Video receivers are added to and removed from video channels. Video receivers are created and
 * added to their video channel by the {@link VideoOutputServlet}, which waits for the receiver
 * to be removed from the video channel. The receiver is removed from the video channel when
 * the associated video stream participant exits the video stream activity.
 */
public class VideoReceiver {

  /**
   * Constructs a VideoReceiver for the specified video stream participant and video output
   * stream.
   *
   * @param participant A receiver participant in a VideoStreamActivity.
   * @param output_stream The output stream associated with the video output channel.
   */
  public VideoReceiver(Participant participant, ServletOutputStream output_stream) {
    this.participant_ = participant;
    this.output_stream_ = output_stream;
  }

  /**
   * Returns the participant who is the video receiver.
   *
   * @return The participant who is the video receiver.
   */
  public Participant getParticipant() {
    return participant_;
  }

  /**
   * Returns the output stream that can be used to stream video to the video receiver.
   *
   * @return The video receiver output stream.
   */
  public ServletOutputStream getOutputStream() {
    return output_stream_;
  }

  private Participant participant_;
  private ServletOutputStream output_stream_;
}
