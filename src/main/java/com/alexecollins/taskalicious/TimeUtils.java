package com.alexecollins.taskalicious;

import com.mdimension.jchronic.Chronic;
import com.mdimension.jchronic.utils.Span;
import org.ocpsoft.prettytime.PrettyTime;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author alexec (alex.e.c@gmail.com)
 */
public class TimeUtils {

	private static final PrettyTime prettyTime = new PrettyTime();

	public static Date parse(String t) {
		Pattern p = Pattern.compile("(?:due|by|on) (.*)");
		Matcher m = p.matcher(t);
		if (m.find()) {
			t = m.group(1);
		}
		final Span sp = Chronic.parse(t);
		return sp == null ? null :  sp.getEndCalendar().getTime();
	}
	public static String format(Date d) {
		String s = prettyTime.format(d);
		if (s.equals("moments ago")) {
			return "today";
		}
		return s;
	}
}
