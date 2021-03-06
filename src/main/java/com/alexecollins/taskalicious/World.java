package com.alexecollins.taskalicious;

import com.alexecollins.taskalicious.events.*;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Map;

import static com.alexecollins.taskalicious.HttpUtils.argsOf;

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
				World.log.info("replying to who are you");
				reply(e, me.getName() + '\n' + Peer.me());
			}
		});
		server.createContext("/peers", new HttpHandler() {
			@Override
			public void handle(HttpExchange httpExchange) throws IOException {
				World.log.info("informing client of peers");
				reply(httpExchange, Joiner.on("\n").join(Collections2.transform(peers.entrySet(), new Function<Map.Entry<User, Peer>, String>() {
					@Override
					public String apply(Map.Entry<User, Peer> e) {
						return e.getKey() + "=" + e.getValue();
					}
				})));
			}
		});
		server.createContext("/addTask", new HttpHandler() {
			@Override
			public void handle(HttpExchange e) throws IOException {
				Map<String, String> args = argsOf(e);
				World.log.info("discovered task " + args);
				try {
					bus.post(new TaskDiscovered(new Task(args.get("task"))));
				} catch (Exception e1) {
					bus.post(e1);
				}
				reply(e, "");
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
		World.log.info("replying " + body);
		PrintWriter out = new PrintWriter(e.getResponseBody());
		e.sendResponseHeaders(HttpURLConnection.HTTP_OK, body.length());
		out.print(body);
		out.close();
		e.close();
	}

	public void hello(Peer peer) throws IOException {
		 get(peer, "/hello?user=" + me + "&peer=" + Peer.me());
	}

	private String get(Peer peer, String string) throws IOException {
		return HttpUtils.get(URI.create("http://" + peer.getHostName() + ":" + peer.getPort() + string));
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
		return User.named(get(p, "/whoAreYou").split("\n")[0]);
	}

	@Subscribe
	public void taskAdded(TaskAddedEvent e) {
		log.info("taskAdded " + e);
		if (!e.getTask().getOwner().equals(me)) {
			try {
				log.info("sending task to peer");
				Peer peer = peers.get(e.getTask().getOwner());
				if (peer == null) {
					bus.post(new PeerUnavailable(peer));
				} else {
					get(peer, "/addTask?task=" + UriUtils.encodeURI(e.getTask().toString()));
				}
			} catch (IOException e1) {
				bus.post(e1);
			}
		}
	}

	@Subscribe
	public void peerDiscovered(PeerDiscovered e) {
		try {
			if (!peers.containsKey(e.getUser())) {
				hello(e.getPeer());
				log.info("requesting peers from " + e.getPeer());
				for (String l :get(e.getPeer(), "/peers").split("\n")){
					int i = l.indexOf("=");
					peers.put(User.named(l.substring(0, i)), new Peer(l.substring(i + 1)));
				}
			}
		} catch (IOException e1) {
			bus.post(e1);
		}
	}

	private class HelloHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange e) throws IOException {
			log.info("handling hello");
			Map<String,String> args = argsOf(e);
			reply(e, "");
			try {
				User user = User.named(args.get("user").trim());
				Peer peer = new Peer(args.get("peer"));
				log.info("hello from " + user);
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
