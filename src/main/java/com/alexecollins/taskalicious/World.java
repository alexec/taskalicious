package com.alexecollins.taskalicious;

import com.alexecollins.taskalicious.events.PeerAddedEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.sun.net.httpserver.HttpServer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Map;

/**
 * @author alexec (alex.e.c@gmail.com)
 */
@Slf4j
public class World  {
	private final Peers peers;
	private final User me;
	private final EventBus bus;
	private HttpServer server;

	public World(Peers peers, User me, EventBus bus) {
		this.peers = peers;
		this.me = me;
		this.bus = bus;
	}

	public void start() throws IOException {
		bus.register(this);
		server = HttpServer.create(new InetSocketAddress(Peer.me().getPort()), 0);
		server.start();

		sayHello(me, peers.get(me));

		for (Map.Entry<User, Peer> entry : peers.entrySet()) {
			sayHello(entry.getKey(), entry.getValue());
		}
	}

	private String sayHello(User user, Peer peer) throws IOException {
		return get(peer, "/hello?user=" + user.toString() + "&peer=" + peer.toString());
	}

	private String get(Peer peer, String string) throws IOException {
		return HttpUtil.get(URI.create("http://" + peer.getHostName() + ":" + peer.getPort() + string));
	}

	@Subscribe
	public void peerAdded(PeerAddedEvent e) {
		try {
			sayHello(e.getUser(), e.getPeer());
		} catch (IOException e1) {
			bus.post(e1);
		}
	}

	public User whoAreYou(Peer p) throws IOException {
		return User.named(get(p, "/whoAreYou"));
	}
}
