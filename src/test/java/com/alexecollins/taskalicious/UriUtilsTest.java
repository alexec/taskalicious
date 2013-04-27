package com.alexecollins.taskalicious;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author alexec (alex.e.c@gmail.com)
 */
public class UriUtilsTest {

	@Test
	public void testUri() throws Exception {

		assertEquals("a", UriUtils.encodeURI("a"));
		assertEquals("%20", UriUtils.encodeURI(" "));

	}
}
