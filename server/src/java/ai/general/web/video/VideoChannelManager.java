/* WebCat
 * Copyright (C) 2013 Tuna Oezer, General AI
 * All rights reserved.
 */

package ai.general.web.video;

import ai.general.common.RandomString;
import ai.general.web.Singleton;

import java.util.HashMap;

/**
 * Manages {@link VideoChannel} objects. The VideoChannelManager can be used to create and close
 * new channels and add receivers to existing channels.
 *
 * VideoChannelManager is a singleton class.
 */
public class VideoChannelManager {

  /**
   * Creates the VideoChannelManager. VideoChannelManager is a singleton.
   * Use {@link #getInstance()} to create an instance.
   */
  public VideoChannelManager() {
    channels_ = new HashMap<String, VideoChannel>();
  }

  /**
   * Returns the singleton VideoChannelManager instance.
   *
   * @return The singleton VideoChannelManager instance.
   */
  public static VideoChannelManager getInstance() {
    return Singleton.get(VideoChannelManager.class);
  }

  /**
   * Creates and returns a new video channel.
   * The returned video channel must be closed via the close() method of the VideoChannel.
   *
   * @param activity The video stream activity associated with the channel.
   * @return A new video channel.
   */
  public synchronized VideoChannel createChannel(VideoStreamActivity activity) {
    String channel_id = RandomString.nextString(16);
    VideoChannel channel = new VideoChannel(channel_id, activity);
    channels_.put(channel_id, channel);
    return channel;
  }

  /**
   * Returns the video channel with the specified channel ID or null if there is no such channel.
   *
   * @param channel_id The channel ID of the video channel.
   * @return The video channel or null.
   */
  public VideoChannel getChannel(String channel_id) {
    return channels_.get(channel_id);
  }

  /**
   * Unregisters the specified video channel. This is an internal method and should not be called
   * directly.
   *
   * @param channel The video channel to unregister.
   */
  public synchronized void unregister(VideoChannel channel) {
    channels_.remove(channel.getChannelId());
  }

  private HashMap<String, VideoChannel> channels_;
}
