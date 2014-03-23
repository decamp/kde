/*
 * Copyright (c) 2014, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.kde;


/**
 * Performs a Fast Fourier Transform on a square matrix of values.
 * Compatible with both real and complex valued inputs.
 * <p/>
 * Not thread safe.  For parallel operation, use multiple instances.
 *
 * @author Philip DeCamp
 */
public class FastFourierTransform2d {


    /**
     * Factory method to create FastFourierTransform objects.
     * The soled argument, <code>dim</code>, indicates the size
     * of one side of square matrix on which the transform will
     * operate.  This must be a power-of-two.  For instance, if
     * <code>dim = 4</code>, then this transform will accept
     * as input a matrix of size [4,4] and write to an
     * output matrix of size [4,4].
     * <p/>
     * Memory allocate is about 18 + dim*dim*16 bytes.
     *
     * @param dim Size of one side of square matrix on which the transform operates.  Must be power-of-two.
     * @return new FastFourierTransform2d instance
     * @throws IllegalArgumentException if dim is smaller than 2, too large, or not a power-of-two.
     */
    public static FastFourierTransform2d create( int dim ) {
        return new FastFourierTransform2d( dim );
    }


    /**
     * Performs a 2D Fast Fourier Transform on a square matrix of complex values.
     * <p/>
     * Complex samples must be stored in a tightly packed format.  For a 2x2 matrix: <br/>
     * <code>[... r_0_0, i_0_0, r_1_0, i_1_0, r_0_1, i_0_1, r_1_1, i_1_1 ...] </code><br/>,
     * where <code>r_m_n</code> is the real component of element at position [m_n] and
     * <code>ik</code> is the corresponding imaginary component.
     *
     * @param x       Input array of complex values.  <code>x.length &gt= dim * dim * 2 + xOff</code>.
     * @param xOff    Start position of data in the input array.
     * @param inverse Set to false for normal FFT, true for inverse FFT.
     * @param out     Output array where transformed, complex elements are stored. <code>out.length &gt= dim*dim*2 + outOff</code>.
     * @param outOff  Start position into output array.
     */
    public void applyComplex( double[] x, int xOff, boolean inverse, double[] out, int outOff ) {
        final int dim = mDim;
        final int dim2 = mDim * 2;
        final int len = dim2 * dim;
        final int shift = 30 - mBits;

        for( int xa = 0; xa < dim2; xa += 2 ) {
            final int ia = xa + xOff;
            final int ib = (Pots.reverse( xa ) >>> shift) + outOff;

            for( int y = 0; y < len; y += dim2 ) {
                out[ib + y] = x[ia + y];
                out[ib + y + 1] = x[ia + y + 1];
            }
        }

        applyRest( out, outOff, inverse );
    }


    /**
     * Performs a 2D Fast Fourier Transform on a square matrix of real values.  NOTE that
     * output is still complex.
     * <p/>
     * Samples must be stored in a tightly packed format.  For a 2x2 matrix: <br/>
     * <code>[... r_0_0, r_1_0, r_0_1, r_1_1, ...] </code><br/>,
     * where <code>r_m_n</code> is the real-valued element at position [m,n].
     *
     * @param x       Input array of real values.  <code>x.length &gt= dim * dim + xOff</code>.
     * @param xOff    Start position of data in the input array.
     * @param inverse Set to false for normal FFT, true for inverse FFT.
     * @param out     Output array where transformed, complex elements are stored. <code>out.length &gt= dim*dim*2 + outOff</code>.
     * @param outOff  Start position into output array.
     */
    public void applyReal( double[] x, int xOff, boolean inverse, double[] out, int outOff ) {
        // Bit-reverse the order of the data and copy into the output array.
        final int shift = 31 - mBits;
        final int dim = mDim;
        final int dim2 = dim * 2;
        final int len = dim * dim;

        for( int i = 0; i < dim; i++ ) {
            final int indOut = (Pots.reverse( i ) >>> shift) + outOff;
            final int indX = i + xOff;

            for( int j = 0; j < len; j += dim ) {
                out[indOut + j * 2] = x[indX + j];
                out[indOut + j * 2 + 1] = 0.0;
            }
        }

        applyRest( out, outOff, inverse );
    }



    private static final int MAX_BITS = 15;

    private final int mDim;
    private final int mBits;

    private final double[] mWork;


    private FastFourierTransform2d( int dim ) {
        mDim = dim;
        mBits = computeNumBits( dim );
        mWork = new double[dim * dim * 2];
    }


    static void doFft2d( double[] x, int off, int len, boolean inverse ) {
        double sign = inverse ? -1.0 : 1.0;

        int blockEnd = 1;
        double ar0, ar1, ar2;
        double ai0, ai1, ai2;

        for( int blockSize = 2; blockSize <= len; blockSize <<= 1 ) {
            final double angle = Math.PI * 2.0 / blockSize;
            final double cm1 = Math.cos( angle );

            //Use trig identity to quickly compute: sm1 = Math.sin(angle)
            //Block size >= 2  ergo   0 <= angle <= Math.PI  ergo  sin(angle) >= 0   
            final double sm1 = sign * Math.sqrt( 1.0 - cm1 * cm1 );

            //Use trig identity to quickly compute: cm2 = Math.cos(2.0 * angle)
            final double cm2 = 2.0 * cm1 * cm1 - 1.0;

            //Use trig identity to quickly compute: sm2 = Math.sin(2.0 * angle);
            final double sm2 = 2.0 * sm1 * cm1;

            final double w = 2.0 * cm1;

            for( int i = 0; i < len; i += blockSize ) {
                ar2 = cm2;
                ar1 = cm1;

                ai2 = sm2;
                ai1 = sm1;

                for( int j = i * 2 + off, n = 0; n < blockEnd; j += 2, n++ ) {
                    ar0 = w * ar1 - ar2;
                    ar2 = ar1;
                    ar1 = ar0;

                    ai0 = w * ai1 - ai2;
                    ai2 = ai1;
                    ai1 = ai0;

                    for( int s = 0; s < len; s++ ) {
                        int aa = j + s * len * 2;
                        int bb = aa + blockEnd * 2;

                        double tr = ar0 * x[bb] - ai0 * x[bb + 1];
                        double ti = ar0 * x[bb + 1] + ai0 * x[bb];

                        x[bb] = x[aa] - tr;
                        x[bb + 1] = x[aa + 1] - ti;

                        x[aa] += tr;
                        x[aa + 1] += ti;
                    }
                }
            }

            blockEnd = blockSize;
        }
    }


    private void applyRest( double[] a, int aOff, boolean inverse ) {
        doFft2d( a, aOff, mDim, inverse );
        shuffle1( a, aOff, mDim, mBits, mWork, 0 );
        doFft2d( mWork, 0, mDim, inverse );

        if( !inverse ) {
            shuffle2( mWork, 0, mDim, a, aOff );
        } else {
            invShuffle2( mWork, 0, mDim, a, aOff );
        }
    }


    /**
     * 1. Transpose without conjugation.
     * 2. Bit-reversal shuffle.
     */
    private static void shuffle1( double[] a, int offA, int dim, int bits, double[] out, int offOut ) {
        final int dim2 = dim * 2;
        final int shift = 30 - bits;

        for( int y = 0; y < dim2; y += 2 ) {
            int ia = y * dim + offA;
            int ib = (Pots.reverse( y ) >>> shift) + offOut;

            for( int x = 0; x < dim2; x += 2 ) {
                out[ib + x * dim] = a[ia + x];
                out[ib + x * dim + 1] = a[ia + x + 1];
            }
        }
    }

    /**
     * 1. Transpose without conjugation.
     */
    private static void shuffle2( double[] a, int offA, int dim, double[] out, int offOut ) {
        final int dim2 = dim * 2;

        for( int y = 0; y < dim2; y += 2 ) {
            int ia = y + offA;
            int ib = y * dim + offOut;

            for( int x = 0; x < dim2; x += 2 ) {
                out[ib + x] = a[ia + x * dim];
                out[ib + x + 1] = a[ia + x * dim + 1];
            }
        }
    }

    /**
     * 1. Scale
     * 2. Transpose without conjugation.
     */
    private static void invShuffle2( double[] a, int offA, int dim, double[] out, int offOut ) {
        final double scale = 1.0 / (dim * dim);
        final int dim2 = dim * 2;

        for( int y = 0; y < dim2; y += 2 ) {
            int ia = y + offA;
            int ib = y * dim + offOut;

            for( int x = 0; x < dim2; x += 2 ) {
                out[ib + x] = a[ia + x * dim] * scale;
                out[ib + x + 1] = a[ia + x * dim + 1] * scale;
            }
        }
    }


    private static int computeNumBits( int n ) {
        int ret = 1; //Start at 1 because 2^0 is not valid.

        for(; ret < 31; ret++ ) {
            if( (1 << ret) == n ) {
                break;
            }
        }

        if( ret == 31 ) {
            throw new IllegalArgumentException( "Too many samples or not a power of two." );
        }

        return ret;
    }

}