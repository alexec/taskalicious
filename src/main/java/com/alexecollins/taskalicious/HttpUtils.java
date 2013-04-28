package com.alexecollins.taskalicious;

import com.sun.net.httpserver.HttpExchange;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author alexec (alex.e.c@gmail.com)
 */
@Slf4j
public class HttpUtils {
	public static String get(URI uri) throws IOException {
		HttpUtils.log.info("requesting " + uri);
		StringBuilder 			out = new StringBuilder();

		HttpURLConnection con = (HttpURLConnection) uri.toURL().openConnection();
		try {
			con.setDoInput(true);
			con.connect();
			String l;
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			while ((l = in.readLine()) != null) {
				out.append(l).append('\n');
			}
		} catch (IOException e) {
			HttpUtils.log.error("failed " + e);
			throw e;
		} finally {
			con.disconnect();
		}
		HttpUtils.log.info(" -> " + out.toString());
		return out.toString();
	}

	public static Map<String, String> argsOf(HttpExchange e) {
		return argsOf(e.getRequestURI().getQuery());
	}

	static Map<String, String> argsOf(String s) {
		HashMap<String, String> map = new HashMap<String, String>();

		Matcher m = Pattern.compile("([^=]*)=([^&]*)(?:&|$)").matcher(s);
		while (m.find()) {
			map.put(m.group(1), m.group(2));
		}
		return map;
	}
}
