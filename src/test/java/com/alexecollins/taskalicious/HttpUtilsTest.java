package com.alexecollins.taskalicious;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

/**
 * @author alexec (alex.e.c@gmail.com)
 */
public class HttpUtilsTest {
	@Test
	public void testArgsOf() throws Exception {
		assertEquals(Collections.singletonMap("a", "1"), HttpUtils.argsOf("a=1"));
		assertEquals(ImmutableMap.of("a","1","b","2"), HttpUtils.argsOf("a=1&b=2"));
	}
}
