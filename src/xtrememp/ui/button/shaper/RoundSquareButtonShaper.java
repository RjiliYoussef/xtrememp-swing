/**
 * Xtreme Media Player a cross-platform media player.
 * Copyright (C) 2005-2010 Besmir Beqiri
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
import java.awt.geom.RoundRectangle2D;
import javax.swing.AbstractButton;

/**
 *
 * @author Besmir Beqiri
 */
public class RoundSquareButtonShaper extends AbstractButtonShaper {

    @Override
    public String getDisplayName() {
        return "RoundSquare";
    }

    @Override
    public Shape getButtonOutline(AbstractButton button, Insets insets,
			int width, int height, boolean isInner) {
        double w = width - 1;
        double h = height - 1;
        double arcw = w / 3.0D;
        double arch = h / 3.0D;
        return new RoundRectangle2D.Double(0.0D, 0.0D, w, h, arcw, arch);
    }
}
