package com.alexecollins.taskalicious.events;

import com.alexecollins.taskalicious.Peer;
import com.alexecollins.taskalicious.User;
import lombok.Data;

/**
 * @author alexec (alex.e.c@gmail.com)
 */
@Data
public class PeerAddedEvent {
	private final User user;
	private final Peer peer;
}
