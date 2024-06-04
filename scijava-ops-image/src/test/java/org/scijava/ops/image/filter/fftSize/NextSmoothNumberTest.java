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

package org.scijava.ops.image.filter.fftSize;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests {@link NextSmoothNumber}.
 *
 * @author Curtis Rueden
 */
public class NextSmoothNumberTest {

	/** Tests {@link NextSmoothNumber#nextSmooth(int)}. */
	@Test
	public void testNextSmooth1D() {
		final int[] inputs = {
			42, 128, 209, 432, 561, 591, 665, 686, 713, 729,
			757, 832, 1018, 1218, 1250, 1328, 1392, 1539, 1779, 1874,
			1951, 1989, 2154, 2219, 2324, 2326, 2439, 2440, 2495, 2812,
			2902, 2919, 3289, 3417, 3490, 3612, 4009, 4084, 4139, 4180,
			4183, 4201, 4440, 4568, 4594, 4838, 4842, 4845, 4878, 4989,
			5016, 5035, 5109, 5148, 5215, 5367, 5469, 5699, 5971, 6039,
			6101, 6109, 6174, 6187, 6314, 6342, 6359, 6585, 6661, 6827,
			6851, 6930, 7277, 7392, 7409, 7620, 7635, 7726, 7820, 7886,
			8138, 8775, 8798, 8820, 8853, 8862, 8885, 8987, 9020, 9038,
			9069, 9081, 9130, 9148, 9546, 9607, 9650, 9885, 9923, 9981
		};
		final int[] expected = {
			42, 128, 210, 432, 567, 600, 672, 686, 720, 729,
			768, 840, 1024, 1225, 1250, 1344, 1400, 1568, 1792, 1875,
			1960, 2000, 2160, 2240, 2352, 2352, 2450, 2450, 2500, 2835,
			2916, 2940, 3360, 3430, 3500, 3645, 4032, 4096, 4200, 4200,
			4200, 4320, 4480, 4608, 4608, 4860, 4860, 4860, 4900, 5000,
			5040, 5040, 5120, 5184, 5250, 5376, 5488, 5760, 6000, 6048,
			6125, 6125, 6174, 6250, 6400, 6400, 6400, 6615, 6720, 6860,
			6860, 7000, 7290, 7500, 7500, 7680, 7680, 7776, 7840, 7938,
			8192, 8820, 8820, 8820, 8960, 8960, 8960, 9000, 9072, 9072,
			9072, 9216, 9216, 9216, 9600, 9720, 9720, 10000, 10000, 10000
		};
		for (int i = 0; i < inputs.length; i++) {
			final int actual = NextSmoothNumber.nextSmooth(inputs[i]);
			assertEquals(expected[i], actual);
		}
	}

	/** Tests {@link NextSmoothNumber#nextSmooth(int, int, int)}. */
	@Test
	public void testNextSmooth3D() {
		final int[] x = {
			1809, 6681, 4701, 2330, 6623, 2637, 2230, 3958, 1374, 3298,
			5720, 1375, 358, 8774, 8326, 6972, 7910, 3632, 3228, 2595,
			7880, 3717, 4878, 7961, 435, 1378, 2558, 5327, 4823, 9292,
			7539, 8962, 5656, 1646, 7069, 5303, 9173, 7478, 5852, 2905,
			2426, 7301, 6535, 5383, 4041, 9962, 9008, 6550, 1040, 4145,
			7949, 5159, 5675, 9109, 8618, 1630, 4913, 5943, 1971, 8664,
			9460, 5874, 1702, 7326, 3289, 2837, 1389, 5427, 6016, 9236,
			6226, 1071, 6709, 3316, 2979, 31, 7548, 6297, 6670, 8178,
			3171, 213, 7279, 3882, 1592, 4983, 9468, 9172, 563, 6198,
			3787, 348, 4471, 5953, 4562, 2634, 3017, 829, 8628, 2602
		};
		final int[] y = {
			5770, 7708, 3112, 9152, 2137, 3957, 4198, 7004, 8913, 6933,
			7133, 3864, 7889, 5170, 5136, 1876, 2074, 369, 6875, 82,
			3740, 6588, 2804, 6159, 8938, 2710, 4385, 586, 7394, 8161,
			1740, 7974, 3109, 2627, 6540, 9458, 6326, 1007, 6345, 3296,
			9978, 2535, 1397, 9147, 3715, 935, 9642, 7978, 1234, 4340,
			2683, 2521, 6255, 4219, 4323, 1168, 8480, 6526, 8124, 2916,
			5035, 304, 2677, 1234, 2224, 3162, 5132, 4226, 3552, 2798,
			3771, 688, 3819, 4352, 8442, 7855, 4914, 6475, 2873, 4287,
			6145, 1961, 2753, 2742, 5167, 741, 5470, 1363, 5452, 5522,
			2003, 9334, 8815, 7494, 4626, 4244, 8019, 2984, 9342, 5102
		};
		final int[] z = {
			355, 8360, 6134, 9892, 6849, 6409, 9526, 8724, 3153, 3248,
			2873, 9951, 4212, 9991, 3733, 1701, 8164, 7357, 8035, 583,
			1986, 3635, 2238, 2183, 938, 606, 1869, 5157, 7099, 6836,
			3288, 5083, 877, 8584, 3390, 5663, 3540, 9883, 6476, 7547,
			602, 7064, 1649, 4338, 5537, 1090, 3398, 649, 1582, 2005,
			4083, 4183, 4777, 9375, 5733, 8035, 5946, 5971, 6387, 7953,
			9526, 2577, 3831, 7488, 8415, 6892, 6033, 8347, 2627, 2193,
			5279, 3202, 2787, 2535, 9200, 9469, 3677, 6145, 8656, 9269,
			9788, 1460, 3637, 7703, 2771, 718, 3935, 3293, 6721, 9517,
			1495, 51, 4646, 1616, 5157, 9467, 8076, 4533, 6013, 4293
		};
		final int[] expected = {
			5832, 7776, 3125, 9216, 2160, 3969, 4200, 7056, 8960, 7000,
			7168, 3888, 7938, 5184, 5145, 1890, 2100, 375, 6912, 84,
			3750, 6615, 2835, 6174, 8960, 2744, 4410, 588, 7500, 8192,
			1750, 8000, 3125, 2646, 6561, 9600, 6400, 1008, 6400, 3360,
			10000, 2560, 1400, 9216, 3750, 945, 9720, 8000, 1250, 4374,
			2688, 2560, 6272, 4320, 4374, 1176, 8505, 6561, 8192, 2916,
			5040, 315, 2688, 1250, 2240, 3200, 5145, 4320, 3584, 2800,
			3780, 700, 3840, 4374, 8505, 7875, 5000, 6480, 2880, 4320,
			6174, 2000, 2800, 2744, 5184, 750, 5488, 1372, 5488, 5600,
			2016, 9375, 8820, 7500, 4704, 4320, 8064, 3000, 9375, 5103
		};
		for (int i = 0; i < x.length; i++) {
			final int actual = NextSmoothNumber.nextSmooth(x[i], y[i], z[i]);
			assertEquals(expected[i], actual);
		}
	}
}
