package com.alexecollins.taskalicious;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;

/**
 * @author alexec (alex.e.c@gmail.com)
 */
@Slf4j
public class HttpUtil {
	public static String get(URI uri) throws IOException {
		log.info("sayHello: " + uri);
		StringBuilder out = null;
		HttpURLConnection con = (HttpURLConnection) uri.toURL().openConnection();
		try {
			con.connect();
			String l;
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			out = new StringBuilder();
			while ((l = in.readLine()) != null) {
				out.append(l).append('\n');
			}
		} catch (IOException e) {
			log.error("failed " + e, e);
			throw e;
		} finally {
			con.disconnect();
		}
		log.info(" -> " + out.toString());
		return out.toString();
	}
}
