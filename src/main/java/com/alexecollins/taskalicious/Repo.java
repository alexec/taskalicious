package com.alexecollins.taskalicious;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author alexec (alex.e.c@gmail.com)
 */
public class Repo {

	private final Set<RepoListener> listeners = new CopyOnWriteArraySet<RepoListener>();
	private final List<Task> tasks = new ArrayList<Task>();

	public void addTask(Task task) {
		tasks.add(task);
		for (RepoListener listener:listeners) {
			listener.added(this, task);
		}
	}

	public void addListener(RepoListener listener) {
		listeners.add(listener);
	}

	public List<Task> getTasks() {
		return tasks;
	}

	public interface RepoListener {
		public void added(Repo repo, Task task);
	}
}
