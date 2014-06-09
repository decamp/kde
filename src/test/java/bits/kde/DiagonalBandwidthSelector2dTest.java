/*
 * Copyright (c) 2014, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.kde;

import java.util.Random;

import org.junit.*;


/**
 * @author Philip DeCamp
 */
public class DiagonalBandwidthSelector2dTest {

    @Test
    public void test() throws MathException {
        final int len = 60;
        final int quant = 512;

        Random r = new Random( 0 );
        double[] v = new double[len * 2];

        for( int i = 0; i < v.length; i++ ) {
            v[i] = r.nextDouble() * 2.0 - 1.0;
        }

        //System.out.println(TestUtil.realToMatlab(v, 0, len, 2));
        //System.out.println("===");

        Timer.start();
        double[] band = DiagonalBandwidthSelector2d.computeBandwidth( v, 0, len, null, quant );
        Timer.printSeconds( "seconds: " );

        double[] ans = { 0.3791652385790543, 0.0, 0.0, 0.35930223321154625 };
        TestUtil.assertNear( band, 0, ans, 0, 2 );
    }

}
