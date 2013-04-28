package com.alexecollins.taskalicious.synth;

import javax.swing.*;
import java.awt.*;

/**
 * @author alexec (alex.e.c@gmail.com)
 */
public abstract class CheckBoxIcon implements Icon {

    private final int size;

    public CheckBoxIcon(int size) {
        this.size = size;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        CustomPainter.paintBox(g, x, y, getIconWidth(), getIconHeight(),
		        CustomPainter.getArc(c),
		        c.getForeground(), c.getBackground());
    }

    @Override
    public int getIconWidth() {
        return size;
    }

    @Override
    public int getIconHeight() {
        return size;
    }
}
