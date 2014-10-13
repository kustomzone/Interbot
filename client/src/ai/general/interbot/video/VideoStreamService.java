/* General AI - Interbot
 * Copyright (C) 2013 Tuna Oezer, General AI.
 * See license.txt for copyright information.
 */

package ai.general.interbot.video;

import ai.general.event.Event;
import ai.general.event.Processor;
import ai.general.interbot.InterbotConfig;
import ai.general.interbot.WebSocket;
import ai.general.interbot.WebSocketUri;
import ai.general.net.Connection;
import ai.general.plugin.annotation.Subscribe;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Implements the Interbot video stream service.
 * The video stream service streams video from a camera to the server which can be used for
 * vision or further transmitted to a receiver.
 */
public class VideoStreamService {

  public static final String kPanTiltTopic = "/robot/video/panTilt";
  public static final String kVideoTopic = "/robot/video";

  /**
   * Event processor for pan-tilt commands.
   */
  private class PanTiltProcessor extends Processor<PanTiltCommand> {
    
    /**
     * Creates a PanTiltProcessor that processes the specified pan-tilt event stream.
     *
     * @param pan_tilt_event Pan-tilt event stream.
     */
    public PanTiltProcessor(Event<PanTiltCommand> pan_tilt_event) {
      super("camera-pan-tilt-processor");
      this.pan_tilt_event_ = pan_tilt_event;
    }

    /**
     * Stops the pan-tilt command processor.
     */
    @Override
    public void halt() {
      pan_tilt_event_.unsubscribe(getObserver());
      super.halt();
    }

    /**
     * Starts the pan-tilt command processor.
     */
    @Override
    public void start() {
      pan_tilt_event_.subscribe(getObserver());
      super.start();
    }

    /**
     * Processes pan-tilt commands.
     *
     * @param command The pan-tilt command.
     */
    @Override
    protected void process(PanTiltCommand command) {
      camera_.panTilt(command.getInstruction());
    }

    private Event<PanTiltCommand> pan_tilt_event_;  // Pan-tilt request received from server.
  }

  /**
   * Initializes the video stream service.
   *
   * @param connection Connection to server.
   */
  public VideoStreamService(Connection connection) {
    this.connection_ = connection;
    this.streamer_ = null;
    this.camera_ = new IpCamera();
    this.pan_tilt_event_ = new Event<PanTiltCommand>();
    this.pan_tilt_processor_ = null;
  }

  /**
   * Handles pan-tilt requests. Pan-tilt requests control the orientation of the camera.
   *
   * @param instruction The pan-tilt command.
   */
  @Subscribe(kPanTiltTopic)
  public void onPanTiltRequest(PanTiltCommand command) {
    command.setReceptionTimestampToNow();
    log.debug("Received camera pan-tilt command: {}", command.getInstruction().name());
    pan_tilt_event_.trigger(command);
  }

  /**
   * Handles video instructions. Video instructions are used to control the video system.
   * The server can request that a video stream is started or stopped.
   *
   * @param instruction The video instruction.
   */
  @Subscribe(kVideoTopic)
  public void onVideoRequest(VideoInstruction instruction) {
    switch (instruction.getInstruction()) {
      case StartStream:
        log.debug("video instruction: start stream on channel {}", instruction.getChannel());
        if (streamer_ == null &&
            camera_.open()) {
          try {
            InterbotConfig interbot_config = InterbotConfig.load();
            WebSocketUri uri = new WebSocketUri(interbot_config.getSecure(),
                                                interbot_config.getServer(),
                                                instruction.getServicePath());
            uri.setParameter("session_id", connection_.getSessionId());
            uri.setParameter("channel", instruction.getChannel());
            WebSocket socket = new WebSocket(uri);
            if (socket.open()) {
              streamer_ = new VideoStreamer(camera_, socket);
              streamer_.start();
              pan_tilt_processor_ = new PanTiltProcessor(pan_tilt_event_);
              pan_tilt_processor_.start();
            } else {
              log.error("failed to connect to video server");
            }
          } catch (Exception e) {
            log.catching(Level.ERROR, e);
          }
        } else {
          log.error("Failed to start video stream.");
        }
        break;
      case StopStream:
        log.debug("video instruction: stop stream");
        if (streamer_ != null) {
          streamer_.halt();
          streamer_ = null;
        }
        if (pan_tilt_processor_ != null) {
          pan_tilt_processor_.halt();
          pan_tilt_processor_ = null;
        }
        camera_.close();
        break;
      default: break;
    }
  }

  private static Logger log = LogManager.getLogger();

  private IpCamera camera_;  // Video capture device.
  private Connection connection_;  // Connection on which server requests are received.
  private Event<PanTiltCommand> pan_tilt_event_;  // Pan-tilt request queue.
  private PanTiltProcessor pan_tilt_processor_;  // Pan-tilt request processor.
  private VideoStreamer streamer_;  // Used to upload video to server.
}
