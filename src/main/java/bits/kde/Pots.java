/*
* Copyright (c) 2014, Massachusetts Institute of Technology
* Released under the BSD 2-Clause License
* http://opensource.org/licenses/BSD-2-Clause
*/

package bits.kde;

/**
* Fast routines for finding power-of-two numbers.  Plus some other bit twiddling hacks.
*
* @author Philip DeCamp
*/
public class Pots {

    /**
     * @param val
     * @return the smallest power-of-two value that is greatest than <code>val</code>.
     */
    public static int higherPot( int val ) {
        if( val <= 0 ) {
            return 1;
        }
        val = (val >> 1) | val;
        val = (val >> 2) | val;
        val = (val >> 4) | val;
        val = (val >> 8) | val;
        val = (val >> 16) | val;

        return val + 1;
    }

    /**
     * @param val
     * @return the smallest power-of-two value that is equal-to-or-greater than <code>val</code>.
     */
    public static int ceilPot( int val ) {
        if( val <= 0 ) {
            return 1;
        }
        return higherPot( val - 1 );
    }

    /**
     * @param val
     * @return the greatest power-of-two value that is less than <code>val</code>.
     */
    public static int lowerPot( int val ) {
        if( val <= 1 ) {
            return 1;
        }
        return higherPot( val - 1 ) >> 1;
    }

    /**
     * @param val
     * @return the greatest power-of-two that is equal-to-or-less-than <code>val</code>.
     */
    public static int floorPot( int val ) {
        if( val <= 1 ) {
            return 1;
        }
        return higherPot( val ) >> 1;
    }

    /**
     * @param val
     * @return Version of val with reversed bits.
     */
    public static int reverse( int val ) {
        long v = (((((val >>> 24)       ) * 0x0202020202L & 0x010884422010L) % 1023L)      ) |
                 (((((val >>> 16) & 0xFF) * 0x0202020202L & 0x010884422010L) % 1023L) << 8 ) |
                 (((((val >>>  8) & 0xFF) * 0x0202020202L & 0x010884422010L) % 1023L) << 16) |
                 (((((val       ) & 0xFF) * 0x0202020202L & 0x010884422010L) % 1023L) << 24);

        return (int)v;
    }

    /**
     * @param val
     * @return the smallest power-of-two value that is greatest than <code>val</code>.
     */
    public static long higherPot( long val ) {
        if( val <= 0 ) {
            return 1;
        }
        val = (val >>  1) | val;
        val = (val >>  2) | val;
        val = (val >>  4) | val;
        val = (val >>  8) | val;
        val = (val >> 16) | val;
        val = (val >> 32) | val;
        return val + 1;
    }

    /**
     * @param val
     * @return the smallest power-of-two value that is equal-to-or-greater than <code>val</code>.
     */
    public static long ceilPot( long val ) {
        if( val <= 0 ) {
            return 1;
        }
        return higherPot( val - 1 );
    }

    /**
     * @param val
     * @return the greatest power-of-two value that is less than <code>val</code>.
     */
    public static long lowerPot( long val ) {
        if( val <= 1 ) {
            return 1;
        }
        return higherPot( val - 1 ) >> 1;
    }

    /**
     * @param val
     * @return the greatest power-of-two that is equal-to-or-less-than <code>val</code>.
     */
    public static long floorPot( long val ) {
        if( val <= 1 ) {
            return 1;
        }
        return higherPot( val ) >> 1;
    }

}
