package com.alexecollins.taskalicious;

import com.alexecollins.taskalicious.events.TaskAddedEvent;
import com.alexecollins.taskalicious.events.TaskDiscovered;
import com.alexecollins.taskalicious.events.TaskRemovedEvent;
import com.alexecollins.taskalicious.persistence.Persister;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

/**
 * @author alexec (alex.e.c@gmail.com)
 */
public class Tasks extends AbstractList<Task> {

	private final List<Task> tasks = new ArrayList<Task>();
	private final EventBus bus;
	private final File f;

	public Tasks(EventBus bus) throws IOException {
		this.bus = bus;
		bus.register(this);
		f = new File("tasks.txt");
	}

	@Override
	public int size() {
		return tasks.size();
	}

	@Override
	public boolean add(Task task) {
		if (tasks.add(task)) {
			try {
				Persister.save(this, f);
			} catch (FileNotFoundException e) {
				throw new RuntimeException(e);
			}
			bus.post(new TaskAddedEvent(this, task));
			return true;
		}
		return false;
	}

	@Override
	public Task get(int i) {
		return tasks.get(i);
	}

	public void remove(Task task) {
		if (tasks.remove(task)) {
			try {
				Persister.save(this, f);
			} catch (FileNotFoundException e) {
				throw new RuntimeException(e);
			}
			bus.post(new TaskRemovedEvent(this, task));
		}
	}

	@Subscribe
	public void taskDiscovered(TaskDiscovered e) {
		add(e.getTask());
	}

	public List<Task> getTasks() {
		return tasks;
	}

	public void start() throws IOException {
		Persister.load(this, Task.class, f);
	}
}
