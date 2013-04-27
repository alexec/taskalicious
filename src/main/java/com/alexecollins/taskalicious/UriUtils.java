package com.alexecollins.taskalicious;

/**
 * @author alexec (alex.e.c@gmail.com)
 */
public class UriUtils {
	private static final String mark = "-_.!~*'()\"";

	public static String encodeURI(String argString) {
		StringBuilder uri = new StringBuilder();

		for (char c : argString.toCharArray()) {
			if ((c >= '0' && c <= 'z') || mark.indexOf(c) != -1) {
				uri.append(c);
			} else {
				uri.append('%').append(Integer.toHexString(c));
			}
		}
		return uri.toString();
	}
}
