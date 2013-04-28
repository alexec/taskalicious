package com.alexecollins.taskalicious.persistence;

import com.google.common.io.Files;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Map;

/**
 * @author alexec (alex.e.c@gmail.com)
 */
public class Persister {
	public static <C> void load(Collection<C> col, Class<C> c,  File f) throws IOException {
		if (!f.exists()) {return;}

		for (String l : Files.readLines(f, Charset.forName("UTF-8"))) {
			try {
				col.add(c.getConstructor(String.class).newInstance(l));
			} catch (Exception e) {
				throw new AssertionError(e);
			}
		}
	}

	public static <C> void save(Collection<C> col, File f) throws FileNotFoundException {
		PrintWriter out = new PrintWriter(new FileOutputStream(f));

		for (Object t : col) {
			out.println(t.toString());
		}

		out.close();
	}

	public static <K,V> void load(Map<K, V> map, Class<K> k, Class<V> v,  File f) throws IOException {
		if (!f.exists()) {return;}

		for (String l : Files.readLines(f, Charset.forName("UTF-8"))) {
			try {
				int i = l.indexOf("=");
				map.put(k.getConstructor(String.class).newInstance(l.substring(i)),
						v.getConstructor(String.class).newInstance(l.substring(i + 1)));
			} catch (Exception e) {
				throw new AssertionError(e);
			}
		}
	}

	public static <K,V> void save(Map<K, V> map, File f) throws FileNotFoundException {
		PrintWriter out = new PrintWriter(new FileOutputStream(f));

		for (Map.Entry<K, V> e : map.entrySet()) {
			out.println(e.getKey() + "=" + e.getValue());
		}


		out.close();
	}
}
