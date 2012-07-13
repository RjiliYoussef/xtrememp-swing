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
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import xtrememp.Settings;
import xtrememp.XtremeMP;
import xtrememp.player.dsp.DigitalSignalProcessor;
import xtrememp.player.dsp.DssContext;

import javax.sound.sampled.SourceDataLine;
import javax.swing.JPanel;
import org.pushingpixels.substance.api.ComponentState;

import org.pushingpixels.substance.api.SubstanceColorScheme;
import org.pushingpixels.substance.api.SubstanceLookAndFeel;
import org.pushingpixels.substance.api.skin.SkinChangeListener;
import org.pushingpixels.substance.internal.utils.border.SubstanceBorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Besmir Beqiri
 */
public class VisualizationPanel extends JPanel implements DigitalSignalProcessor,
        Runnable, SkinChangeListener {

    private static Logger logger = LoggerFactory.getLogger(VisualizationPanel.class);
    protected final GraphicsConfiguration gc;
    protected final List<VisualizationChangeListener> listeners;
    private Visualization currentVis;
    private TreeSet<Visualization> visSet;
    private Frame fullscreenWindow;
    private GraphicsDevice device;
    private DisplayMode displayMode;
    private BufferStrategy bufferStrategy;
    private int numBuffers = 2;
    private volatile boolean isFullScreen = false;

    public VisualizationPanel() {
        setOpaque(false);
        setBorder(new SubstanceBorder());

        gc = GraphicsEnvironment.getLocalGraphicsEnvironment().
                getDefaultScreenDevice().getDefaultConfiguration();
        listeners = new ArrayList<VisualizationChangeListener>();
        initVisualizations();
        initFullScreenWindow();
        addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    setFullScreen(true);
                }
            }
        });

        skinChanged();
        SubstanceLookAndFeel.registerSkinChangeListener(this);
    }

    private void initVisualizations() {
        visSet = new TreeSet<Visualization>();
        visSet.add(new Spectrogram());
        visSet.add(new SpectrumBars());
        visSet.add(new VolumeMeter());
        visSet.add(new Waveform());

        String visDisplayName = Settings.getVisualization();
        for (Visualization vis : visSet) {
            if (vis.getDisplayName().equals(visDisplayName)) {
                showVisualization(vis, false);
                break;
            }
        }
    }

    private void initFullScreenWindow() {
        fullscreenWindow = new Frame();
        fullscreenWindow.setUndecorated(true);
        fullscreenWindow.setIgnoreRepaint(true);
        fullscreenWindow.setResizable(false);
        fullscreenWindow.setBackground(Color.black);
        fullscreenWindow.setAlwaysOnTop(true);
        fullscreenWindow.setFocusable(true);
        fullscreenWindow.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_ESCAPE:
                        setFullScreen(false);
                        break;
                    case KeyEvent.VK_LEFT:
                        prevVisualization();
                        break;
                    case KeyEvent.VK_RIGHT:
                        nextVisualization();
                        break;
                    default:
                        break;
                }
            }
        });
        fullscreenWindow.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    setFullScreen(false);
                }
            }
        });
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        device = env.getDefaultScreenDevice();
        displayMode = device.getDisplayMode();
    }

    public boolean isFullScreen() {
        return isFullScreen;
    }

    public void setFullScreen(boolean flag) {
        XtremeMP.getInstance().getMainFrame().setVisible(!flag);
        if (flag && device.isFullScreenSupported()) {
            device.setFullScreenWindow(fullscreenWindow);
            validate();
            fullscreenWindow.createBufferStrategy(numBuffers);
            if (device.isDisplayChangeSupported()) {
                device.setDisplayMode(displayMode);
                setSize(new Dimension(displayMode.getWidth(), displayMode.getHeight()));
            }
            isFullScreen = true;
        } else {
            isFullScreen = false;
            device.setFullScreenWindow(null);
            fullscreenWindow.dispose();
        }
    }

    public Set<Visualization> getVisualizationSet() {
        return visSet;
    }

    public void showVisualization(Visualization newVis, boolean fireEvent) {
        if (newVis == null) {
            throw new IllegalArgumentException("Visualization is null.");
        }
        currentVis = newVis;
        repaint();
        Settings.setVisualization(currentVis.getDisplayName());
        if (fireEvent) {
            fireVisualizationChangedEvent();
        }
    }

    public Visualization prevVisualization() {
        Visualization prevVis = visSet.lower(currentVis);
        if (prevVis == null && !visSet.isEmpty()) {
            prevVis = visSet.last();
        }
        showVisualization(prevVis, true);
        return prevVis;
    }

    public Visualization nextVisualization() {
        Visualization nextVis = visSet.higher(currentVis);
        if (nextVis == null && !visSet.isEmpty()) {
            nextVis = visSet.first();
        }
        showVisualization(nextVis, true);
        return nextVis;
    }

    public void addVisualizationChangeListener(VisualizationChangeListener listener) {
        if (listener == null) {
            return;
        }
        listeners.add(listener);
        logger.info("Visualization change listener added");
    }

    public void removeVisualizationChangedListener(VisualizationChangeListener listener) {
        if (listener == null) {
            return;
        }
        listeners.remove(listener);
        logger.info("Visualization change listener removed");
    }

    /**
     * Notifies all listeners that selected visualization has changed.
     */
    private void fireVisualizationChangedEvent() {
        VisualizationEvent event = new VisualizationEvent(this, currentVis);
        for (VisualizationChangeListener listener : listeners) {
            listener.visualizationChanged(event);
        }
        logger.info("Visualization changed: {}", currentVis);
    }

    @Override
    public void init(int sampleSize, SourceDataLine sourceDataLine) {
        for (Visualization vis : visSet) {
            vis.init(sampleSize, sourceDataLine);
        }
    }

    @Override
    public void process(DssContext dssContext) {
        int width = getWidth();
        int height = getHeight();
        currentVis.checkBuffImage(gc, width, height);
        currentVis.render(dssContext, currentVis.getBuffGraphics(), width, height);
        if (isFullScreen) {
//            Dimension size = fullscreenWindow.getSize();
            bufferStrategy = fullscreenWindow.getBufferStrategy();
            Graphics2D g2d = null;
            try {
                g2d = (Graphics2D) bufferStrategy.getDrawGraphics();
                for (int i = 0; i < numBuffers; i++) {
                    if (!bufferStrategy.contentsLost()) {
                        if (currentVis != null) {
//                            setSize(size);
                            g2d.drawImage(currentVis.getBuffImage(), 0, 0, this);
                        }
                    }
                    bufferStrategy.show();
                }
            } finally {
                if (g2d != null) {
                    g2d.dispose();
                }
            }
        } else {
            EventQueue.invokeLater(this);
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        int width = getWidth();
        int height = getHeight();
        currentVis.checkBuffImage(gc, width, height);
        g.drawImage(currentVis.getBuffImage(), 0, 0, this);
    }

    @Override
    public void run() {
        repaint();
    }

    @Override
    public final void skinChanged() {
        SubstanceColorScheme colorScheme = SubstanceLookAndFeel.getCurrentSkin().
                getColorScheme(this, ComponentState.ENABLED);

        for (Visualization v : visSet) {
            v.setBackgroundColor(colorScheme.getBackgroundFillColor());
            v.setForegroundColor(colorScheme.getForegroundColor());
            repaint();
        }
    }
}
