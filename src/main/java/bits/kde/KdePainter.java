/*
 * Copyright (c) 2014, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.kde;

import java.awt.image.BufferedImage;


/**
 * @author Philip DeCamp
 */
public class KdePainter {

    public static void drawPoints( double[] p,
                                   int pOff,
                                   int len,
                                   double minX,
                                   double minY,
                                   double maxX,
                                   double maxY,
                                   BufferedImage out )
    {
        final int w = out.getWidth();
        final int h = out.getHeight();
        final int h1 = h - 1;

        for( int i = 0; i < len; i++ ) {
            double px = p[i * 2 + pOff];
            double py = p[i * 2 + pOff + 1];

            int xx = (int)((px - minX) * w / (maxX - minX) + 0.5);
            int yy = (int)((py - minY) * h / (maxY - minY) + 0.5);

            int v = out.getRGB( xx, h1 - yy );
            out.setRGB( xx, h1 - yy, v | 0xFF );
            //out.setRGB(xx, h1 - yy, 0xFFFFFFFF);
        }
    }


    public static void drawDensity( Function21 k,
                                    double minX,
                                    double minY,
                                    double maxX,
                                    double maxY,
                                    BufferedImage out )
    {
        final int w = out.getWidth();
        final int h = out.getHeight();
        final int h1 = h - 1;

        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        double scaleX = (maxX - minX) / (w - 1);
        double scaleY = (maxY - minY) / (h - 1);

        for( int y = 0; y < h; y++ ) {
            double py = minY + y * scaleY;

            for( int x = 0; x < w; x++ ) {
                double px = minX + x * scaleX;
                double v = k.apply( px, py );

                if( v > max ) {
                    max = v;
                }

                if( v < min ) {
                    min = v;
                }
            }
        }

        for( int y = 0; y < h; y++ ) {
            double py = minY + y * scaleY;

            for( int x = 0; x < w; x++ ) {
                double px = minX + x * scaleX;
                int v = (int)((k.apply( px, py ) - min) * 255.0 / (max - min) + 0.5);
                out.setRGB( x, h1 - y, 0xFF000000 | (v << 16) );
            }
        }
    }

}
