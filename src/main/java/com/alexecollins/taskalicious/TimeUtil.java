package com.alexecollins.taskalicious;

import com.mdimension.jchronic.Chronic;
import com.mdimension.jchronic.utils.Span;
import org.ocpsoft.prettytime.PrettyTime;

import java.util.Date;

/**
 * @author alexec (alex.e.c@gmail.com)
 */
public class TimeUtil {

	private static final PrettyTime prettyTime = new PrettyTime();

	public static Date parse(String t) {
		final Span p = Chronic.parse(t);
		return p == null ? null :  p.getBeginCalendar().getTime();
	}
	public static String format(Date d) {
		return prettyTime.format(d);
	}
}
