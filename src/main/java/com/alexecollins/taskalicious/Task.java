package com.alexecollins.taskalicious;


import com.alexecollins.taskalicious.events.TaskChanged;
import com.google.common.eventbus.EventBus;
import lombok.Data;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author alexec (alex.e.c@gmail.com)
 */
@Data
public class Task {

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
		fireChange();
	}

	private void fireChange() {
		bus.post(new TaskChanged(this));
	}

	public enum State {PENDING,COMPLETE,DECLINED}
	private final EventBus bus;
	private final User creator;
	private State state = State.PENDING;
	private Date due;
	private String text;
	private User owner;

	Task(EventBus bus, User creator, String s) {
		if (bus == null) {throw new IllegalArgumentException("null bus");}
		if (creator == null) {throw new IllegalArgumentException("null creator");}
		this.bus = bus;
		this.creator = creator;
		fromString(s);
	}

	public static Task of(EventBus bus, User creator, String s) {
		return new Task(bus, creator, s);
	}

	public void fromString(String s) {
		if (s == null) {throw new IllegalArgumentException("null string");}
		owner = creator;
		{
			final Pattern p = Pattern.compile("([-x]) (.*)");
			Matcher m = p.matcher(s);
			if (!m.find()) {
				throw new IllegalArgumentException(s + " invalid, must start with - or x");
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
				due = TimeUtils.parse(m.group(2));
				if (due != null) {
					s = m.group(1);
				}
			}
		}
		text = s;
		fireChange();
	}

	public boolean isOverdue() {
		return due != null && due.getTime() < System.currentTimeMillis();
	}

	@Override
	public String toString() {
		return (state == State.COMPLETE ? "x" : "-") + " " + text + (due != null ? (" due " + TimeUtils.format(due)) : "")
				+ " - " + owner;
	}

}
