package com.alexecollins.taskalicious.events;

import com.alexecollins.taskalicious.Peer;
import lombok.Data;

/**
 * @author alexec (alex.e.c@gmail.com)
 */
@Data
public class PeerUnavailable {
	private final Peer peer;
}
