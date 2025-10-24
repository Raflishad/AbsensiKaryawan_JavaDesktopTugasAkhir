package absensi;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.*;
import javax.swing.*;
import javax.swing.JProgressBar;
import javax.swing.plaf.basic.BasicProgressBarUI;
import java.awt.geom.RoundRectangle2D;

public class ProgressBarCustom extends JProgressBar {
    
    public Color getColorString() {
        return colorString;
    }
    
    public void setColorString(Color colorString) {
        this.colorString = colorString;
    }
    
    private Color colorString = new Color(3, 3, 3);
    
   public ProgressBarCustom() {
        setPreferredSize(new Dimension(100, 10)); // lebih tinggi biar enak liat rounded-nya
        setBackground(new Color(190, 190, 190));
        setForeground(new Color(99, 235, 189));
        setBorderPainted(false);
        setOpaque(false); // biar transparan

        setUI(new BasicProgressBarUI() {
            @Override
            protected void paintDeterminate(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                int width = c.getWidth();
                int height = c.getHeight();
                int arc = height; // biar oval

                // Smooth
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Background (track)
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, width, height, arc, arc);

                // Progress fill
                int progressWidth = (int) (((double) getValue() / getMaximum()) * width);
                g2.setColor(getForeground());
                g2.fillRoundRect(0, 0, progressWidth, height, arc, arc);

                g2.dispose();
            }

            @Override
            protected void paintIndeterminate(Graphics g, JComponent c) {
                paintDeterminate(g, c); // biar tetap pakai gaya rounded meskipun indeterminate
            }

            @Override
            protected void paintString(Graphics g, int x, int y, int width, int height, int amountFull, Insets b) {
                // Optional: Kalau kamu mau teks persen juga tampil
                super.paintString(g, x, y, width, height, amountFull, b);
            }
        });
    }

    // Optional: Kalau mau warna string bisa diatur
//    public Color getColorString() {
//        return Color.BLACK;
//    }
}
