package com.alexecollins.taskalicious;

import com.alexecollins.taskalicious.events.PeerAddedEvent;
import com.alexecollins.taskalicious.events.PeerDiscovered;
import com.alexecollins.taskalicious.events.TaskAddedEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.HttpURLConnection;
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
		InetSocketAddress address = new InetSocketAddress(Peer.me().getPort());
		server = HttpServer.create(address, 0);
		server.createContext("/hello", new HelloHandler());
		server.createContext("/whoAreYou", new HttpHandler() {
			@Override
			public void handle(HttpExchange e) throws IOException {
				reply(e, me.getName());
			}
		});
		server.start();
		log.info("listening at " + address);

		try {
			hello(peers.get(me));
		} catch (IOException e) {
			bus.post(e);
		}

		for (Map.Entry<User, Peer> entry : peers.entrySet()) {
			try {
				hello(entry.getValue());
			} catch (IOException e) {
				bus.post(e);
			}
		}
	}

	private void reply(HttpExchange e, String body) throws IOException {
		PrintWriter out = new PrintWriter(e.getResponseBody());
		e.sendResponseHeaders(HttpURLConnection.HTTP_OK, body.length());
		out.print(body);
		out.close();
		e.close();
	}

	private String hello(Peer peer) throws IOException {
		return get(peer, "/hello?user=" + me + "&peer=" + Peer.me());
	}

	private String get(Peer peer, String string) throws IOException {
		return HttpUtil.get(URI.create("http://" + peer.getHostName() + ":" + peer.getPort() + string));
	}

	@Subscribe
	public void peerAdded(PeerAddedEvent e) {
		try {
			hello(e.getPeer());
		} catch (IOException e1) {
			bus.post(e1);
		}
	}

	public User whoAreYou(Peer p) throws IOException {
		return User.named(get(p, "/whoAreYou").trim());
	}

	@Subscribe
	public void taskAdded(TaskAddedEvent e) {
		if (e.getTask().getOwner() != me) {
			try {
				get(peers.get(e.getTask().getOwner()), "/addTask?task=" + e.getTask());
			} catch (IOException e1) {
				bus.post(e1);
			}
		}
	}

	private class HelloHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange e) throws IOException {
			Map<String,String> args = HttpUtil.argsOf(e);
			reply(e, "");
			log.info("accepted " + args);
			try {
				User user = User.named(args.get("user").trim());
				Peer peer = Peer.of(args.get("peer"));
				if (peers.put(user, peer) == null) {
					bus.post(new PeerDiscovered(user, peer));
				}
			} catch (Exception e1) {
				bus.post(e1);
			}
			log.info("done " + args);
		}
	}
}
