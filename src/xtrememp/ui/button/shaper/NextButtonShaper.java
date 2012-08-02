/**
 * Xtreme Media Player a cross-platform media player.
 * Copyright (C) 2005-2012 Besmir Beqiri
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package xtrememp.ui.button.shaper;

import java.awt.Insets;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.AbstractButton;
import static xtrememp.util.Utilities.tr;

/**
 *
 * @author Besmir Beqiri
 */
public final class NextButtonShaper extends AbstractButtonShaper {

    @Override
    public String getDisplayName() {
        return tr("MainFrame.Menu.Player.Next");
    }

    @Override
    public Shape getButtonOutline(final AbstractButton button,
        final Insets insets, final int width, final int height,
        final boolean isInner) {

        final double w = width - 1;
        final double h = height - 1;

        final double length = h / 3.0D;

        final Shape shape = new Ellipse2D.Double(0, 0, length, h);
        final Area area = new Area(new RoundRectangle2D.Double(length / 2, 0,
                w - length, h, length, length));
        area.subtract(new Area(shape));

        return area;
    }
}