package com.alexecollins.taskalicious;


import java.util.Date;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author alexec (alex.e.c@gmail.com)
 */
public class Task {

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
		fireChange();
	}

	private void fireChange() {
		for (TaskListener listener:listeners) {
			listener.update(this);
		}
	}

	public Date getDue() {
		return due;
	}

	public User getOwner() {
		return owner;
	}

	public enum State {PENDING,COMPLETE,DECLINED}
	private final Set<TaskListener> listeners = new CopyOnWriteArraySet<TaskListener>();
	private final User creator;
	private State state = State.PENDING;
	private Date due;
	private String text;
	private User owner;

	Task(User creator, String s) {
		if (creator == null) {throw new IllegalArgumentException("null creator");}
		this.creator = creator;
		fromString(s);
	}

	public static Task of(User creator, String s) {
		return new Task(creator, s);
	}

	public void fromString(String s) {
		if (s == null) {throw new IllegalArgumentException("null string");}
		owner = creator;
		{
			final Pattern p = Pattern.compile("([-x]) (.*)");
			Matcher m = p.matcher(s);
			if (!m.find()) {
				throw new IllegalArgumentException("string invalid, must start with - or x");
			}
			state = m.group(1).equals("-") ? State.PENDING : State.COMPLETE;
			s = m.group(2);
		}
		{
			final Pattern p = Pattern.compile("(.*) - (.*)");
			final Matcher m = p.matcher(s);
			if (m.find()) {
				owner = User.named(m.group(2));
				s = m.group(1);
			}
		}
		{
			final Pattern p = Pattern.compile("(.*?) ((?:due|by|on|at|before)? .*)");
			final Matcher m = p.matcher(s);
			if (m.find()) {
				due = TimeUtil.parse(m.group(2));
				if (due != null) {
					s = m.group(1);
				}
			}
		}
		text = s;
		fireChange();
	}

	public String getText() {
		return text;
	}

	public boolean isOverdue() {
		return due != null && due.getTime() < System.currentTimeMillis();
	}

	@Override
	public String toString() {
		return state == State.COMPLETE ? "x" : "-" + " " + text + (due != null ? (" due " + TimeUtil.format(due)) : "")
				+ (!owner.equals(creator) ? " - " + owner : "");
	}

	public interface TaskListener {void update(Task task);}

	public void addListener(TaskListener listener) {
		listeners.add(listener);
	}
}
