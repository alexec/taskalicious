package com.alexecollins.taskalicious.synth;

import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.io.IOException;

/**
 * @author alexec (alex.e.c@gmail.com)
 */
public class CustomFontResource {
	public static FontUIResource createFont(String path, final int size) throws IOException, FontFormatException {

		return new FontUIResource(Font.createFont(Font.TRUETYPE_FONT,
				CustomFontResource.class.getResourceAsStream(path)).deriveFont(
				Font.PLAIN, size));
	}

}
