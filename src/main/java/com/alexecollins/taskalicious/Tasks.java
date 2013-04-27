package com.alexecollins.taskalicious;

import com.alexecollins.taskalicious.events.TaskAddedEvent;
import com.alexecollins.taskalicious.events.TaskDiscovered;
import com.alexecollins.taskalicious.events.TaskRemovedEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

/**
 * @author alexec (alex.e.c@gmail.com)
 */
public class Tasks {

	private final List<Task> tasks = new ArrayList<Task>();
	private final EventBus bus;

	public Tasks(EventBus bus) {
		this.bus = bus;
		bus.register(this);
	}

	public void add(Task task) {
		if (tasks.add(task)) {
			bus.post(new TaskAddedEvent(this, task));
		}
	}

	public void remove(Task task) {
		if (tasks.remove(task)) {
			bus.post(new TaskRemovedEvent(this, task));
		}
	}

	@Subscribe
	public void taskDiscovered(TaskDiscovered e) {
		add(e.getTask());
	}
}
