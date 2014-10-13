/* General AI - Interbot
 * Copyright (C) 2013 Tuna Oezer, General AI.
 * See license.txt for copyright information.
 */

package ai.general.interbot;

import java.net.InetAddress;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents information about a local network interface.
 * NetworkInterfaceInfo is sent as a reply to a system information request with a
 * NetworkInterfaces parameter.
 *
 * NetworkInterfacesInfo is serialized into JSON.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class NetworkInterfaceInfo {

  /**
   * Represents an IP address.
   */
  public static class IpAddress {
    /** IP Versions. */
    public enum Version {
      IPv4,
      IPv6
    }

    /**
     * Constructs a default IP address.
     */
    public IpAddress() {
      this.version_ = Version.IPv4;
      this.address_ = "0.0.0.0";
    }

    /**
     * Constructs an IP address.
     *
     * @param version The IP version of the address.
     * @param address String representation of the IP address.
     */
    public IpAddress(Version version, String address) {
      this.version_ = version;
      this.address_ = address;
    }

    /**
     * Returns the string representation of the IP address.
     *
     * @return The string representation of the IP address.
     */
    public String getAddress() {
      return address_;
    }

    /**
     * Returns the version of the IP address.
     *
     * @return The IP address version.
     */
    public Version getVersion() {
      return version_;
    }

    /**
     * Sets IP address. The IP address is represented as a string.
     *
     * @param address The string representation of the IP address.
     */
    public void setAddress(String address) {
      this.address_ = address;
    }

    /**
     * Sets the version of the IP address.
     *
     * @param version The IP address version.
     */
    public void setVersion(Version version) {
      this.version_ = version;
    }

    private String address_;  // String representation of the IP address.
    private Version version_;  // The IP address version.
  }

  /**
   * Constructs a NetworkInterfacesInfo object for a single network interface with an empty name.
   */
  public NetworkInterfaceInfo() {
    this.name_ = "";
    this.addresses_ = new ArrayList<IpAddress>();
  }

  /**
   * Constructs a NetworkInterfacesInfo object for a single network interface.
   *
   * @param name The name of the network interface.
   */
  public NetworkInterfaceInfo(String name) {
    this.name_ = name;
    this.addresses_ = new ArrayList<IpAddress>();
  }

  /**
   * Collects information about the local network interfaces and returns the information as an
   * array of NetworkInterfaceInfo objects.
   *
   * @return Information about the local network interfaces.
   */
  public static ArrayList<NetworkInterfaceInfo> queryAll() {
    ArrayList<NetworkInterfaceInfo> infos = new ArrayList<NetworkInterfaceInfo>();
    try {
      Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
      while (interfaces.hasMoreElements()) {
        NetworkInterface iface = interfaces.nextElement();
        String name = iface.getName();
        if (name.startsWith("eth") || name.startsWith("wlan")) {
          NetworkInterfaceInfo info = new NetworkInterfaceInfo(name);
          Enumeration<InetAddress> addresses = iface.getInetAddresses();
          while (addresses.hasMoreElements()) {
            InetAddress address = addresses.nextElement();
            if (address instanceof Inet4Address) {
              info.getAddresses().add(new IpAddress(IpAddress.Version.IPv4,
                                                    inetAddressToString(address)));
            } else if (address instanceof Inet6Address) {
              info.getAddresses().add(new IpAddress(IpAddress.Version.IPv6,
                                                    inetAddressToString(address)));
            }
          }
          if (info.getAddresses().size() > 0) {
            infos.add(info);
          }
        }
      }
    } catch (SocketException e) {}
    return infos;
  }

  /**
   * Gets the IP addresses associated with this network interface.
   *
   * @return The IP addresses associated with this network interface.
   */
  public ArrayList<IpAddress> getAddresses() {
    return addresses_;
  }

  /**
   * Returns the name of this network interface.
   *
   * @return The name of this network interface.
   */
  public String getName() {
    return name_;
  }

  /**
   * Sets the IP addresses associated with this network interface.
   *
   * @param addresses The IP addresses associated with this network interface.
   */
  public void setAddresses(ArrayList<IpAddress> addresses) {
    this.addresses_ = addresses;
  }

  /**
   * Sets the name of this network interface.
   *
   * @param name The name of this network interface.
   */
  public void setName(String name) {
    this.name_ = name;
  }

  /**
   * Returns a string representation of the specified IP address, cleaning it up if necessary.
   *
   * @param address The InetAddress to convert to a string.
   * @return A string representation of the IP address.
   */
  private static String inetAddressToString(InetAddress address) {
    String result = address.toString();
    if (result.startsWith("/")) result = result.substring(1);
    return result;
  }

  private ArrayList<IpAddress> addresses_;  // IP addresses associated with the network interface.
  private String name_;  // The name of the network interface.
}
