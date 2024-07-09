/*
 * #%L
 * Image processing operations for SciJava Ops.
 * %%
 * Copyright (C) 2014 - 2024 SciJava developers.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package org.scijava.ops.image.create;

import java.util.function.BiFunction;

import net.imglib2.Dimensions;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.type.NativeType;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.ComplexType;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;
import org.apache.commons.math3.special.BesselJ;

/**
 * Creates a Gibson Lanni Kernel.
 *
 * @author Jizhou Li
 * @author Brian Northan
 */
public final class DefaultCreateKernelGibsonLanni {

	private DefaultCreateKernelGibsonLanni() {
		// Prevent instantiation of static utility class
	}

	public static <T extends Type<T>, C extends ComplexType<C> & NativeType<C>>
		Img<C> createKernel(Dimensions size, Double NA, Double lambda, Double ns,
			Double ni, Double resLateral, Double resAxial, Double pZ, C type,
			BiFunction<Dimensions, T, Img<T>> createFunc)
	{

		// //////// physical parameters /////////////

        var ng0 = 1.5; // coverslip refractive index, design value
        var ng = 1.5; // coverslip refractive index, experimental
        var ni0 = 1.5; // immersion refractive index, design
        var ti0 = 150E-06; // working distance of the objective,
		// desig
		// a bit precision lost if use 170*1.0E-6
        var tg0 = 170E-6; // coverslip thickness, design value
        var tg = 170E-06; // coverslip thickness, experimental value

		// ////////approximation parameters /////////////
        var numBasis = 100; // number of basis functions
        var numSamp = 1000; // number of sampling
        var overSampling = 2; // overSampling

		// initialization in the case of null params
		if (NA == null) NA = 1.4;
		if (lambda == null) lambda = 610E-09;
		if (ns == null) ns = 1.33;
		if (ni == null) ni = 1.5;
		if (resLateral == null) resLateral = 100E-9;
		if (resAxial == null) resAxial = 250E-9;
		if (pZ == null) pZ = 2000E-9D;

		ni0 = ni;

        var nx = -1; // psf size
        var ny = -1;
        var nz = -1;

		if (size.numDimensions() == 2) {
			nx = (int) size.dimension(0);
			ny = (int) size.dimension(1);
			nz = 1;
		}
		else if (size.numDimensions() == 3) {
			nx = (int) size.dimension(0);
			ny = (int) size.dimension(1);
			nz = (int) size.dimension(2);
		}

		// compute the distance between the particle position (pZ) and the center
        var distanceFromCenter = (int) Math.abs(Math.ceil(pZ / resAxial));

		// increase z size so that the PSF is large enough so that we can later
		// recrop a centered psf
		nz = nz + 2 * distanceFromCenter;

        var x0 = (nx - 1) / 2.0D;
        var y0 = (ny - 1) / 2.0D;

        var xp = x0;
        var yp = y0;

        var maxRadius = (int) Math.round(Math.sqrt((nx - x0) * (nx - x0) + (ny -
			y0) * (ny - y0))) + 1;
        var r = new double[maxRadius * overSampling];
        var h = new double[nz][r.length];

        var a = 0.0D;
        var b = Math.min(1.0D, ns / NA);

        var k0 = 2 * Math.PI / lambda;
        var factor1 = 545 * 1.0E-9 / lambda;
        var factor = factor1 * NA / 1.4;
        var deltaRho = (b - a) / (numSamp - 1);

		// basis construction
        var rho = 0.0D;
        var am = 0.0;
        var Basis = new double[numSamp][numBasis];

        var bj0 = new BesselJ(0);
        var bj1 = new BesselJ(1);

		for (var m = 0; m < numBasis; m++) {
			// am = (3 * m + 1) * factor;
			am = 3 * m + 1;
			for (var rhoi = 0; rhoi < numSamp; rhoi++) {
				rho = rhoi * deltaRho;
				Basis[rhoi][m] = bj0.value(am * rho);
			}
		}

		// compute the function to be approximated

        var ti = 0.0D;
		double OPD = 0;
		double W = 0;

        var Coef = new double[nz][numBasis * 2];
        var Ffun = new double[numSamp][nz * 2];

		double rhoNA2;

		for (var z = 0; z < nz; z++) {
			ti = ti0 + resAxial * (z - (nz - 1.0D) / 2.0D);

			for (var rhoi = 0; rhoi < numSamp; rhoi++) {
				rho = rhoi * deltaRho;
				rhoNA2 = rho * rho * NA * NA;

				OPD = pZ * Math.sqrt(ns * ns - rhoNA2);
				OPD += tg * Math.sqrt(ng * ng - rhoNA2) - tg0 * Math.sqrt(ng0 * ng0 -
					rhoNA2);
				OPD += ti * Math.sqrt(ni * ni - rhoNA2) - ti0 * Math.sqrt(ni0 * ni0 -
					rhoNA2);

				W = k0 * OPD;

				Ffun[rhoi][z] = Math.cos(W);
				Ffun[rhoi][z + nz] = Math.sin(W);
			}
		}

		// solve the linear system
		// begin....... (Using Common Math)

		RealMatrix coefficients = new Array2DRowRealMatrix(Basis, false);
		RealMatrix rhsFun = new Array2DRowRealMatrix(Ffun, false);
        var solver = new SingularValueDecomposition(coefficients)
			.getSolver(); // slower
		// but
		// more
		// accurate
		// DecompositionSolver solver = new
		// QRDecomposition(coefficients).getSolver(); // faster, less accurate

        var solution = solver.solve(rhsFun);
		Coef = solution.getData();

		// end.......

        var RM = new double[numBasis][r.length];
        var beta = 0.0D;

        var rm = 0.0D;
		for (var n = 0; n < r.length; n++) {
			r[n] = n * 1.0 / overSampling;
			beta = k0 * NA * r[n] * resLateral;

			for (var m = 0; m < numBasis; m++) {
				// am = (3 * m + 1) * factor;
				am = 3 * m + 1;
				rm = am * bj1.value(am * b) * bj0.value(beta * b) * b;
				rm = rm - beta * b * bj0.value(am * b) * bj1.value(beta * b);
				RM[m][n] = rm / (am * am - beta * beta);

			}
		}

		// obtain one component
        var maxValue = 0.0D;
		for (var z = 0; z < nz; z++) {
			for (var n = 0; n < r.length; n++) {
                var realh = 0.0D;
                var imgh = 0.0D;
				for (var m = 0; m < numBasis; m++) {
					realh = realh + RM[m][n] * Coef[m][z];
					imgh = imgh + RM[m][n] * Coef[m][z + nz];

				}
				h[z][n] = realh * realh + imgh * imgh;
			}
		}

		// assign

        var Pixel = new double[nz][nx * ny];

		for (var z = 0; z < nz; z++) {

			for (var x = 0; x < nx; x++) {
				for (var y = 0; y < ny; y++) {
                    var rPixel = Math.sqrt((x - xp) * (x - xp) + (y - yp) * (y - yp));
                    var index = (int) Math.floor(rPixel * overSampling);

                    var value = h[z][index] + (h[z][index + 1] - h[z][index]) *
						(rPixel - r[index]) * overSampling;
					Pixel[z][x + nx * y] = value;
					if (value > maxValue) {
						maxValue = value;
					}
				}
			}
			//

		}

		// create an RAI to store the PSF
		@SuppressWarnings("unchecked")
		final var psf3d = (Img<C>) createFunc.apply(size, (T) type);

		// use a RandomAccess to access pixels
        var ra = psf3d.randomAccess();

		int start, finish;

		// if the particle position, pZ is negative crop the bottom part of the
		// larger PSF
		if (pZ < 0) {
			start = 2 * distanceFromCenter;
			finish = nz;
		}
		// if the particle position, pZ is positive crop the top part of the larger
		// PSF
		else {
			start = 0;
			finish = nz - 2 * distanceFromCenter;
		}

		// loop and copy pixel values from the PSF array to the output PSF RAI
		for (var z = start; z < finish; z++) {

			for (var x = 0; x < nx; x++) {
				for (var y = 0; y < ny; y++) {

                    var value = Pixel[z][x + nx * y] / maxValue;

					ra.setPosition(new int[] { x, y, z - start });
					ra.get().setReal(value);
				}
			}
		}

		return psf3d;
	}

}
