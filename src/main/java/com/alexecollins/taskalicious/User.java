package com.alexecollins.taskalicious;

import lombok.Data;

/**
 * @author alexec (alex.e.c@gmail.com)
 */
@Data
public class User {
	private final String name;

	public User(String name) {
		if (name == null || name.length()==0 ||!name.trim().equals(name)) {throw new IllegalArgumentException("name null or trailing whitespace");}
		this.name = name;
	}

	public static User named(String name) {
		return new User(name);
	}

	@Override
	public String toString() {
		return name;
	}
}
