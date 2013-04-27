package com.alexecollins.taskalicious;

/**
 * @author alexec (alex.e.c@gmail.com)
 */
public class User {
	private final String name;

	private User(String name) {
		if (name == null) {throw new IllegalArgumentException();}
		this.name = name;
	}

	public static User named(String name) {
		return new User(name);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		User user = (User) o;

		if (!name.equals(user.name)) return false;

		return true;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	public String getName() {
		return name;
	}
}
