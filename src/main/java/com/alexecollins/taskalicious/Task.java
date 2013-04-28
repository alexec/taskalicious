package com.alexecollins.taskalicious;


import lombok.Data;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author alexec (alex.e.c@gmail.com)
 */

@Data
public class Task {

	public enum State {PENDING,COMPLETE,DECLINED}
	private State state = State.PENDING;
	private Date due;
	private String text;
	private User owner;

	Task(String s) {
		fromString(s);
	}

	public static Task of(String s) {
		return new Task(s);
	}

	public void fromString(String s) {
		if (s == null) {throw new IllegalArgumentException("null string");}
		{
			final Pattern p = Pattern.compile("^([-x]) (.*)");
			Matcher m = p.matcher(s);
			if (!m.find()) {
				throw new IllegalArgumentException(s + " invalid, must start with - or x");
			}
			state = m.group(1).equals("-") ? State.PENDING : State.COMPLETE;
			s = m.group(2);
		}
		{
			final Pattern p = Pattern.compile("(.*) - (.*)$");
			final Matcher m = p.matcher(s);
			if (!m.find()) {
				throw new IllegalArgumentException(s + " invalid, must end with ' - owner'");}
			owner = User.named(m.group(2));
			s = m.group(1);
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
		System.out.println(this);
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
