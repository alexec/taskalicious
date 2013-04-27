package com.alexecollins.taskalicious;

import com.alexecollins.taskalicious.events.PeerAddedEvent;
import com.google.common.eventbus.EventBus;
import lombok.extern.slf4j.Slf4j;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author alexec (alex.e.c@gmail.com)
 */
@Slf4j
public class Peers extends AbstractMap<User,Peer> {
	private static Map<User,Peer> map = new HashMap<User,Peer>();
	private final EventBus bus;

	public Peers(EventBus bus) {
		this.bus = bus;
	}

	@Override
	public Peer put(User user, Peer peer) {
		Peer put = map.put(user, peer);

		bus.post(new PeerAddedEvent(user,peer));
		log.debug("added " + user + " -> " + peer);



		return put;
	}

	@Override
	public Set<Entry<User, Peer>> entrySet() {
		return map.entrySet();
	}
}
