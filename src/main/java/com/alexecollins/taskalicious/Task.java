package com.alexecollins.taskalicious;


import java.util.Date;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author alexec (alex.e.c@gmail.com)
 */
public class Task {
	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
		for (TaskListener listener:listeners) {
			listener.update(this);
		}

	}

	public enum State {PENDING,COMPLETE,DECLINED}
	private final Set<TaskListener> listeners = new CopyOnWriteArraySet<TaskListener>();
	private final String creator;
	private State state = State.PENDING;
	private Date due;
	private String text;

	Task(String creator, Date due, String text) {
		this.creator = creator;
		this.due = due;
		this.text = text;
	}

	public static Task of(String creator, String text) {
		return new Task(creator, null, text);
	}

	public String getText() {
		return text;
	}

	public interface TaskListener {void update(Task task);}

	public void addListener(TaskListener listener) {
		listeners.add(listener);
	}
}
