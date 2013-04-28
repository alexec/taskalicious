package com.alexecollins.taskalicious;

import lombok.Data;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author alexec (alex.e.c@gmail.com)
 */
@Data
public class Peer {

	public static final int DEFAULTS_PORT = 17857;
	private static final Peer ME;
	private final String hostName;
	private final int port;
	static {
		try {
			ME = new Peer(InetAddress.getLocalHost().getHostName(), Integer.parseInt(System.getProperty("port", String.valueOf(DEFAULTS_PORT))));
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
	}

	public Peer(String s) {
		this(s.contains(":") ? s.substring(0,s.indexOf(":")) : s,
				s.contains(":") ? Integer.parseInt(s.substring(s.indexOf(":") + 1)) : DEFAULTS_PORT);
	}
	public Peer(String hostName, int port) {
		if (hostName == null || hostName.contains(":")) {throw new IllegalArgumentException("hostName " + hostName + " null or invalid");}
		if (port < 0 || port > 65535) {throw new IllegalArgumentException("port out of range 0..65535");}
		this.hostName = hostName;
		this.port = port;
	}

	public static Peer me() {
		return ME;
	}

	@Override
	public String toString() {
		return hostName+ (port != DEFAULTS_PORT ?  ":" + port : "")          ;
	}
}
