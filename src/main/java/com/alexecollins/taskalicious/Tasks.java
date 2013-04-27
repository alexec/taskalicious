package com.alexecollins.taskalicious;

import com.alexecollins.taskalicious.events.TaskAddedEvent;
import com.alexecollins.taskalicious.events.TaskRemovedEvent;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.eventbus.EventBus;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
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

	public void addTask(Task task) {
		tasks.add(task);
		bus.post(new TaskAddedEvent(this, task));
	}


	public Collection<Task> findTasksByOwner(final User owner) {
		return Collections2.filter(tasks, new Predicate<Task>() {
			@Override
			public boolean apply(@Nullable Task task) {
				return task.getOwner().equals(owner);
			}
		});
	}

	public void remove(Task task) {
		tasks.remove(task);
		bus.post(new TaskRemovedEvent(this, task));
	}
}
