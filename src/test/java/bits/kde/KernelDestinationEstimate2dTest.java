/*
 * Copyright (c) 2014, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */
package bits.kde;

import org.junit.Ignore;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;


/**
 * @author Philip DeCamp
 */
@Ignore
public class KernelDestinationEstimate2dTest {

    public static void main( String[] args ) throws Exception {
        test1();
    }


    static void test1() {
        Random rand = new Random( 5 );
        int len = 4 * 2;
        double[] points = new double[len * 2];

        for( int i = 0; i < points.length / 2; i++ ) {
            points[i] = rand.nextDouble();
        }

        for( int i = points.length / 2; i < points.length; i += 2 ) {
            points[i] = rand.nextDouble() + 1.5;
            points[i + 1] = rand.nextDouble() + 1.3;
        }

        KernelDensityEstimate2d k = KernelDensityEstimate2d.compute( points, 0, len, null, 0.1, null );

        BufferedImage im = plot( k, points, 0, len, 1024 );
        ImagePanel.showImage( im );
    }


    private static BufferedImage plot( KernelDensityEstimate2d k,
                                       double[] points,
                                       int pOff,
                                       int pLen,
                                       int width )
    {
        double x0 = k.minX();
        double x1 = k.maxX();
        double y0 = k.minY();
        double y1 = k.maxY();
        int height = (int)Math.round( width * (y1 - y0) / (x1 - x0) );

        BufferedImage im = new BufferedImage( width, height, BufferedImage.TYPE_INT_ARGB );
        Graphics2D g = (Graphics2D)im.getGraphics();
        g.setBackground( Color.BLACK );
        g.clearRect( 0, 0, width, height );

        drawDensity( k, x0, y0, x1, y1, im );
        drawPoints( points, pOff, pLen, im, x0, y0, x1, y1 );


        return im;
    }


    private static void drawPoints( double[] p,
                                    int pOff,
                                    int len,
                                    BufferedImage im,
                                    double x0,
                                    double y0,
                                    double x1,
                                    double y1 )
    {
        int w = im.getWidth();
        int h = im.getHeight();

        for( int i = 0; i < len; i++ ) {
            double px = p[i * 2 + pOff];
            double py = p[i * 2 + pOff + 1];

            int xx = (int)((px - x0) * w / (x1 - x0) + 0.5);
            int yy = (int)((py - y0) * h / (y1 - y0) + 0.5);

            im.setRGB( xx, yy, 0xFFFFFFFF );
        }
    }


    private static void drawDensity( Function21 k,
                                     double x0,
                                     double y0,
                                     double x1,
                                     double y1,
                                     BufferedImage out )
    {
        final int w = out.getWidth();
        final int h = out.getHeight();

        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        double scaleX = (x1 - x0) / (w - 1);
        double scaleY = (y1 - y0) / (h - 1);

        for( int y = 0; y < h; y++ ) {
            double py = y0 + y * scaleY;

            for( int x = 0; x < w; x++ ) {
                double px = x0 + x * scaleX;
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
            double py = y0 + y * scaleY;

            for( int x = 0; x < w; x++ ) {
                double px = x0 + x * scaleX;
                int v = (int)((k.apply( px, py ) - min) * 255.0 / (max - min) + 0.5);
                out.setRGB( x, y, 0xFF000000 | (v << 16) );
            }
        }
    }

}
