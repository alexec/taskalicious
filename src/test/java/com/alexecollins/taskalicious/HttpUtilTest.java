package com.alexecollins.taskalicious;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

/**
 * @author alexec (alex.e.c@gmail.com)
 */
public class HttpUtilTest {
	@Test
	public void testArgsOf() throws Exception {
		assertEquals(Collections.singletonMap("a", "1"), HttpUtil.argsOf("a=1"));
		assertEquals(ImmutableMap.of("a","1","b","2"), HttpUtil.argsOf("a=1&b=2"));
	}
}
