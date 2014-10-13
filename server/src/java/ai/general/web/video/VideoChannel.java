/* WebCat
 * Copyright (C) 2013 Tuna Oezer, General AI
 * All rights reserved.
 */

package ai.general.web.video;

import java.io.IOException;
import javax.servlet.ServletOutputStream;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * A video channel transmits a video stream from an input source to an output destination.
 * The video channel connects a specific video sender with a specific video receiver.
 *
 * VideoChannels are created with the {@link VideoChannelManager}. VideoChannels must be
 * closed with the {@link @close()} method.
 */
public class VideoChannel {

  /**
   * Creates a new video channel. VideoChannels should be obtained from the
   * {@link VideoChannelManager}.
   *
   * @param activity The video stream activity associated with the channel.
   * @param channel_id The unique ID of this channel known to the sender and receiver.
   */
  public VideoChannel(String channel_id, VideoStreamActivity activity) {
    this.channel_id_ = channel_id;
    this.activity_ = activity;
    this.receiver_ = null;
  }

  /**
   * Returns the video channel ID.
   *
   * @return The video channel ID.
   */
  public String getChannelId() {
    return channel_id_;
  }

  /**
   * Returns the video stream activity associated with the channel.
   *
   * @return The video stream activity associated with the channel.
   */
  public VideoStreamActivity getActivity() {
    return activity_;
  }

  /**
   * Adds a receiver to this video channel. The receiver will start receiving data sent by
   * the sender starting with the next frame.
   *
   * @param receiver The video receiver.
   * @throws IllegalArgumentException If the receiver is not a member of the video stream activity.
   */
  public void addReceiver(VideoReceiver receiver) throws IllegalArgumentException {
    if (receiver.getParticipant().getActivity() != activity_) {
      log.error("illegal receiver");
      throw new IllegalArgumentException(
          "Receiver is not a participant in video stream activity.");
    }
    receiver_ = receiver;
  }

  /**
   * Removes the video receiver added with {@link #addReceiver}.
   * Notifies any thread blocking on the receiver.
   */
  public void removeReceiver() {
    if (receiver_ == null) return;
    synchronized (receiver_) {
      receiver_.notifyAll();
    }
    receiver_ = null;
  }

  /**
   * Closes the video channel. After the channel has been closed, no further video can be streamed.
   */
  public void close() {
    VideoChannelManager.getInstance().unregister(this);
  }

  /**
   * Streams video data to the receiver. This method is repeatedly called by the sender to stream
   * chunks of video to the receiver as new data becomes available. The chunks must be sent in
   * the same order as in the original video stream.
   *
   * The content of the data is dependent on the video protocol agreed by the sender and
   * receiver.
   *
   * @param data A chunk of data from the video stream.
   */
  public void stream(byte[] data) {
    if (receiver_ != null) {
      ServletOutputStream out = receiver_.getOutputStream();
      try {
        out.print("--");
        out.println(VideoOutputServlet.kBoundaryIndicator);
        out.println("Content-Type: image/jpeg");
        out.println("Content-Length: " + data.length);
        out.println();
        out.write(data);
        out.println();
        out.flush();
      } catch (IOException e) {
        log.catching(Level.INFO, e);
      }
    }
  }

  private static Logger log = LogManager.getLogger();  

  private String channel_id_;
  private VideoStreamActivity activity_;
  private VideoReceiver receiver_;
}
