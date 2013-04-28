package com.alexecollins.taskalicious;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Executor;

/**
 * @author alexec (alex.e.c@gmail.com)
 */
@Slf4j
public class PeerFinder {
	private final World world;
	private final Executor executor;

	public PeerFinder(World world, Executor executor) {
		this.world = world;
		this.executor = executor;
	}

	public void start() throws UnknownHostException {
		final int x = byteArrayToInt(InetAddress.getLocalHost().getAddress());

		executor.execute(new Runnable() {
			@Override
			public void run() {
				scan(x, 1);
				scan(x, -1);
			}
		});
	}

	private void scan(int x, int n) {
		int m = 100;
		while (m>0) {
			byte[] a = intToByteArray(x);
			if (a[3] !=0 && a[3] != 255) {
				log.info("scanning " + x);
				for (int port : new int[] {Peer.DEFAULTS_PORT, Peer.DEFAULTS_PORT+1}) {
					try {
						String hostName = InetAddress.getByAddress(a).getHostName();
						log.info("scanning " + hostName);
						world.hello(new Peer(hostName+ ":" + port) );
					} catch (UnknownHostException e) {
						log.warn("unable to poke peer", e);
						return;
					} catch (IOException ignored) {
						// nop
					}
				}
			}
			x=x+n;
			m--;

		}
		PeerFinder.log.info("done scanning");
	}

	public static int byteArrayToInt(byte[] b)
	{
		return   b[3] & 0xFF |
				(b[2] & 0xFF) << 8 |
				(b[1] & 0xFF) << 16 |
				(b[0] & 0xFF) << 24;
	}

	public static byte[] intToByteArray(int a)
	{
		return new byte[] {
				(byte) ((a >> 24) & 0xFF),
				(byte) ((a >> 16) & 0xFF),
				(byte) ((a >> 8) & 0xFF),
				(byte) (a & 0xFF)
		};
	}

}
