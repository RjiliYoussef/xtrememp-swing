/**
 * Xtreme Media Player a cross-platform media player.
 * Copyright (C) 2005-2011 Besmir Beqiri
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
package xtrememp.visualization;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.image.BufferedImage;
import java.nio.FloatBuffer;
import xtrememp.player.dsp.DssContext;

import javax.sound.sampled.SourceDataLine;

/**
 * Visualization base class.
 *
 * @author Besmir Beqiri
 */
public abstract class Visualization implements Comparable<Visualization> {

    protected Color backgroundColor = Color.black;
    protected Color foregroundColor = Color.white;
    protected BufferedImage buffImage;
    protected Graphics2D buffGraphics;

    /**
     * Returns the display name of <code>this</code> visualization.
     *
     * @return the display name of <code>this</code> visualization.
     */
    public abstract String getDisplayName();
    
    public void init(int sampleSize, SourceDataLine sourceDataLine){
        
    }

    /**
     * Defines the rendering method.
     * 
     * @param g2d a Graphics object used for painting.
     * @param width Width of the rendering area.
     * @param height Height of the rendering area.
     */
    public abstract void render(DssContext dssContext, Graphics2D g2d, int width, int height);

    /**
     * @return the backgroundColor.
     */
    public Color getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * @param backgroundColor the backgroundColor to set.
     */
    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    /**
     * @return the foregroundColor.
     */
    public Color getForegroundColor() {
        return foregroundColor;
    }

    /**
     * @param foregroundColor the foregroundColor to set.
     */
    public void setForegroundColor(Color foregroundColor) {
        this.foregroundColor = foregroundColor;
    }

    /**
     * Returns a {@link FloatBuffer} as the result of merging the channels
     * buffers.
     *
     * @param channelsBuffer The channels buffer.
     * @return A {@link FloatBuffer} object.
     */
    public FloatBuffer channelsMerge(FloatBuffer[] channelsBuffer) {
        int ch = channelsBuffer.length;
        for (int a = 0, cap = channelsBuffer[0].capacity(); a < cap; a++) {
            float mcd = 0;
            for (int b = 0; b < ch; b++) {
                mcd += channelsBuffer[b].get(a);
            }
            channelsBuffer[0].put(a, mcd / (float) ch);
        }
        return channelsBuffer[0].asReadOnlyBuffer();
    }

    public BufferedImage getBuffImage() {
        return buffImage;
    }

    public Graphics2D getBuffGraphics() {
        return buffGraphics;
    }
    
    public void checkBuffImage(GraphicsConfiguration gc, int width, int height) {
        if (buffImage == null || (buffImage.getWidth() != width || buffImage.getHeight() != height)) {
            // Free image resources.
            freeImage();

            // Create image.
            buffImage = gc.createCompatibleImage(width, height);

            buffGraphics = buffImage.createGraphics();
            buffGraphics.setColor(backgroundColor);
            buffGraphics.fillRect(0, 0, buffImage.getWidth(), buffImage.getHeight());
        }
    }

    public void freeImage() {
        if (buffGraphics != null) {
            buffGraphics.dispose();
            buffGraphics = null;
        }
        if (buffImage != null) {
            buffImage.flush();
            buffImage = null;
        }
    }

    @Override
    public int compareTo(Visualization vis) {
        return this.getDisplayName().compareTo(vis.getDisplayName());
    }
}
