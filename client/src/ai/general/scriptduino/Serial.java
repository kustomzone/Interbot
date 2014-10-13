/* General AI - Interbot
 * Copyright (C) 2013 Tuna Oezer, General AI.
 * See license.txt for copyright information.
 */

package ai.general.scriptduino;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

/**
 * Represents a serial port. This class allows sending and receiving data from a board, such as an
 * Arduino connected via USB.
 * 
 * This class is specifically written to work in Linux and may not be portable to other operating
 * systems.
 *
 * Serial is thread-safe.
 */
public class Serial {

  private static final int kBaudRate = 115200;
  private static final String kOwner = "Scriptduino";  // Used to obtain lock on serial port.
  private static final int kTimeoutMillis = 30000;

  /**
   * Creates a new serial port for the specified port name. The port name must be the full path
   * to the serial port device. In Linux, this is typically a path of the form '/dev/ttyUSB0' or
   * '/dev/ttyACM0'.
   */
  public Serial(String port_name) {
    this.port_name_ = port_name;
    serial_ = null;
    output_ = null;
  }

  /**
   * Returns the list of serial ports on the system.
   *
   * @return The list of available serial ports.
   */
  public static List<String> listPorts() {
    List<String> ports = new ArrayList<String>();
    @SuppressWarnings("unchecked") Enumeration<CommPortIdentifier> port_enum =
      CommPortIdentifier.getPortIdentifiers();
    while (port_enum.hasMoreElements()) {
      CommPortIdentifier port = port_enum.nextElement();
      if (port.getPortType() == CommPortIdentifier.PORT_SERIAL) {
        ports.add(port.getName());
      }
    }
    return ports;
  }

  /**
   * Closes the serial port.
   * This method can be safely called at any time.
   */
  public synchronized void close() {
    if (serial_ != null) {
      output_ = null;
      serial_.close();
      serial_ = null;
    }
  }

  /**
   * Opens the serial port. Returns true if the port was opened successfully. Returns false if
   * there was an error.
   * This method also writes error information to the standard error output.
   *
   * @return True if the port was opened successfully.
   */
  public synchronized boolean open() {
    try {
      CommPortIdentifier port = CommPortIdentifier.getPortIdentifier(port_name_);
      if (port == null) {
        return false;
      }
      if (port.getPortType() != CommPortIdentifier.PORT_SERIAL) {
        return false;
      }
      serial_ = (SerialPort) port.open(kOwner, kTimeoutMillis);
      serial_.setSerialPortParams(kBaudRate,
                                  SerialPort.DATABITS_8,
                                  SerialPort.STOPBITS_1,
                                  SerialPort.PARITY_NONE);
      serial_.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
      output_ = new PrintWriter(serial_.getOutputStream());
      // flush out any stale data
      InputStream in = serial_.getInputStream();
      while (in.available() > 0) {
        in.read();
      }
    } catch (NoSuchPortException e) {
      System.err.println("Error: Serial port does not exist: " + port_name_);
      return false;
    } catch (PortInUseException e) {
      System.err.println("Error: Serial port is in use: " + port_name_);
      return false;
    } catch (UnsupportedCommOperationException e) {
      close();
      System.err.println("Error: Failed to configure serial port: " + port_name_);
      return false;
    } catch (IOException e) {
      close();
      System.err.println("Error: Failed to open serial port: " + port_name_);
      return false;
    } catch (ClassCastException e) {
      return false;
    }
    return true;
  }

  /**
   * Writes data to the serial port.
   *
   * @param data The data to write to the serial port.
   */
  public synchronized void write(String data) {
    if (output_ != null) {
      output_.print(data);
      output_.flush();
    }
  }

  private PrintWriter output_;  // Output stream to serial port.
  private String port_name_;  // The device name of the serial port.
  private SerialPort serial_;  // The serial port device.
}
