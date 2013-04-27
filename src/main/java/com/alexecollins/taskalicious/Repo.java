package com.alexecollins.taskalicious;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
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
		for (RepoListener listener : listeners) {
			listener.removed(this, task);
		}
	}

	public interface RepoListener {
		public void added(Repo repo, Task task);
		void removed(Repo repo, Task task);
	}
}
