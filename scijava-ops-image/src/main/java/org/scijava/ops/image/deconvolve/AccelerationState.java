package org.scijava.ops.image.deconvolve;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;

public class AccelerationState<T extends RealType<T>> {
		
    public final RandomAccessibleInterval<T> yk_iterated;
    public Img<T> xkm1_previous = null;
	public Img<T> yk_prediction = null;
	public Img<T> hk_vector = null;
	public Img<T> gk;
	public Img<T> gkm1;
    public double accelerationFactor = 0.0f;

    public AccelerationState(RandomAccessibleInterval<T> yk_iterated) {
        this.yk_iterated = yk_iterated;
    }
}