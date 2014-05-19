package pt.it.esoares.android.ip;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Build;

import java.io.*;
import java.net.*;
import java.util.*;

import org.apache.http.conn.util.InetAddressUtils;

/**
 * 
 * Code by: https://stackoverflow.com/questions/6064510/how-to-get-ip-address-of-the-device/13007325#13007325
 * 
 */
public class Utils {

	/**
	 * Convert byte array to hex string
	 * 
	 * @param bytes
	 * @return
	 */
	public static String bytesToHex(byte[] bytes) {
		StringBuilder sbuf = new StringBuilder();
		for (int idx = 0; idx < bytes.length; idx++) {
			int intVal = bytes[idx] & 0xff;
			if (intVal < 0x10)
				sbuf.append("0");
			sbuf.append(Integer.toHexString(intVal).toUpperCase(Locale.US));
		}
		return sbuf.toString();
	}

	/**
	 * Get utf8 byte array.
	 * 
	 * @param str
	 * @return array of NULL if error was found
	 */
	public static byte[] getUTF8Bytes(String str) {
		try {
			return str.getBytes("UTF-8");
		} catch (Exception ex) {
			return null;
		}
	}

	/**
	 * Load UTF8withBOM or any ansi text file.
	 * 
	 * @param filename
	 * @return
	 * @throws java.io.IOException
	 */
	public static String loadFileAsString(String filename) throws java.io.IOException {
		final int BUFLEN = 1024;
		BufferedInputStream is = new BufferedInputStream(new FileInputStream(filename), BUFLEN);
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream(BUFLEN);
			byte[] bytes = new byte[BUFLEN];
			boolean isUTF8 = false;
			int read, count = 0;
			while ((read = is.read(bytes)) != -1) {
				if (count == 0 && bytes[0] == (byte) 0xEF && bytes[1] == (byte) 0xBB && bytes[2] == (byte) 0xBF) {
					isUTF8 = true;
					baos.write(bytes, 3, read - 3); // drop UTF8 bom marker
				} else {
					baos.write(bytes, 0, read);
				}
				count += read;
			}
			return isUTF8 ? new String(baos.toByteArray(), "UTF-8") : new String(baos.toByteArray());
		} finally {
			try {
				is.close();
			} catch (Exception ex) {
			}
		}
	}

	/**
	 * Returns MAC address of the given interface name.
	 * 
	 * @param interfaceName
	 *            eth0, wlan0, ...
	 * @return mac address or empty string
	 */
	@SuppressLint("NewApi")
	public static String getMACAddress(String interfaceName) {
		if (interfaceName == null) {
			return "";
		}
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO) {
			try {
				List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
				for (NetworkInterface intf : interfaces) {
					if (!intf.getName().equalsIgnoreCase(interfaceName))
						continue;
					byte[] mac = intf.getHardwareAddress();
					if (mac == null)
						return "";
					StringBuilder buf = new StringBuilder();
					for (int idx = 0; idx < mac.length; idx++)
						buf.append(String.format("%02X:", mac[idx]));
					if (buf.length() > 0)
						buf.deleteCharAt(buf.length() - 1);
					return buf.toString();
				}
			} catch (Exception ex) {
			} // for now eat exceptions
		}
		try {
			// this is so Linux hack
			return loadFileAsString("/sys/class/net/" + interfaceName + "/address").toUpperCase(Locale.US).trim();
		} catch (IOException ex) {
			return null;
		}
	}

	/**
	 * Get IP address from first non-localhost interface
	 * 
	 * @param ipv4
	 *            true=return ipv4, false=return ipv6
	 * @return address or empty string
	 */
	public static String getIPAddress(boolean useIPv4) {
		try {
			List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
			for (NetworkInterface intf : interfaces) {
				List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
				for (InetAddress addr : addrs) {
					if (!addr.isLoopbackAddress()) {
						String sAddr = addr.getHostAddress().toUpperCase(Locale.US);
						boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
						if (useIPv4) {
							if (isIPv4)
								return sAddr;
						} else {
							if (!isIPv4) {
								int delim = sAddr.indexOf('%'); // drop ip6 port suffix
								return delim < 0 ? sAddr : sAddr.substring(0, delim);
							}
						}
					}
				}
			}
		} catch (Exception ex) {
		} // for now eat exceptions
		return "";
	}

	/**
	 * Changes state of WIFI
	 * 
	 * @param context
	 *            Android Context
	 * @param on
	 *            true=turns WIFI on, false=turns off WIFI
	 */
	public static void changeWifiState(Context context, boolean on) {
		if (context == null) {
			return;
		}
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		wifiManager.setWifiEnabled(on);
		try {
			Thread.sleep(5 * 1000);// waits 5 seconds before returning to changes become effective
		} catch (InterruptedException e) {
		}
	}
}