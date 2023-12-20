###
# #%L
# ImgLib2: a general-purpose, multidimensional image processing library.
# %%
# Copyright (C) 2009 - 2016 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
# John Bogovic, Albert Cardona, Barry DeZonia, Christian Dietz, Jan Funke,
# Aivar Grislis, Jonathan Hale, Grant Harris, Stefan Helfrich, Mark Hiner,
# Martin Horn, Steffen Jaensch, Lee Kamentsky, Larry Lindsey, Melissa Linkert,
# Mark Longair, Brian Northan, Nick Perry, Curtis Rueden, Johannes Schindelin,
# Jean-Yves Tinevez and Michael Zinsmaier.
# %%
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as
# published by the Free Software Foundation, either version 2 of the
# License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public
# License along with this program.  If not, see
# <http://www.gnu.org/licenses/gpl-2.0.html>.
# #L%
###

# Transforms CSV data into HTML that uses pChart4mw charts.

# Charts produced:
#   - Iteration x Time for minimum resolution image, by method
#   - Iteration x Time for maximum resolution image, by method
#   - Resolution x Time at first iteration, by method
#   - Resolution x Time at last iteration, by method

# Total charts: 8 (4 for "cheap" and 4 for "expensive")

import math

# NB: Presumably there is a slick way to avoid having two very similar
# methods (generateIterationChart and generateResolutionChart) iterating
# over different list indices, but my Python knowledge is weak.

# Iteration x Time at a given resolution, by method
def generateIterationChart(name, data, methods, resolution_index, keywords):
    resolutions = range(1, 26, 3)
    iteration_count = len(data[0])
    title = 'Iteration x Time (ms) at ' + str(resolutions[resolution_index]) + ' Mpx'
    # Header
    print(f'plot("{name}IterationVsTime{resolutions[resolution_index]}", "{title}", "Iteration",')
    print(f'\t"Iteration,{",".join(methods)}\\n" +')
    # Data
    for i in range(0, iteration_count):
        suffix = '\\n" +' if i < iteration_count - 1 else '");'
        print(f'\t"{i},{",".join(str(v) for v in data[resolution_index][i])}{suffix}')
    print()

# Resolution x Time at a given iteration, by method
def generateResolutionChart(name, data, methods, iteration_index, keywords):
    resolution_count = len(data)
    title = 'Resolution x Time (ms) at iteration #' + str(iteration_index + 1)
    # Header
    print(f'plot("{name}ResolutionVsTime{iteration_index + 1}", "{title}", "Mpx",')
    print(f'\t"Mpx,{",".join(methods)}\\n" +')
    # Data
    for r in range(resolution_count):
        suffix = '\\n" +' if r < resolution_count - 1 else '");'
        print(f'\t"{r},{",".join(str(v) for v in data[r][iteration_index])}{suffix}')
    print()

# reads data from CSV files into 3D array dimensioned:
# [resolution_count][iteration_count][method_count]
def process(prefix):
    methods = []

    # loop over image resolutions
    data = []
    for p in range(1, 26, 3):
        # compute filename
        res = round(math.sqrt(1000000 * p))
        s_res = str(int(res))
        path_prefix = 'results-' + prefix + '-' + s_res + 'x' + s_res
        in_path = path_prefix + '.csv'

        # read data file
        with open(in_path, 'r') as f:
            lines = f.readlines()

        # loop over iterations
        header = True
        data0 = []
        for line in lines:
            items = line.rstrip().split('\t')
            items.pop(0)
            if header:
                header = False
                methods = items
            else:
                # loop over methods
                data1 = []
                for item in items:
                    data1.append(int(item))
                data0.append(data1)
        data.append(data0)

    resolution_count = len(data)
    iteration_count = len(data[0])

    w = 300
    h = 250
    l_size = 'size=' + str(w) + 'x' + str(h)
    r_size = 'size=' + str(w + 135) + 'x' + str(h)

    # Iteration x Time for minimum resolution image, by method
    generateIterationChart(prefix, data, methods, 0, l_size + ' plots')
    # Iteration x Time for maximum resolution image, by method
    generateIterationChart(prefix, data, methods, resolution_count - 1, r_size + ' plots legend')
    # Resolution x Time at first iteration, by method
    generateResolutionChart(prefix, data, methods, 0, l_size + ' angle=90 cubic plots')
    # Resolution x Time at last iteration, by method
    generateResolutionChart(prefix, data, methods, iteration_count - 1, r_size + ' angle=90 cubic plots legend')

print("<!--Paste the below into the Benchmarks.md file of the SciJava Ops ReadTheDocs for viewing!-->")
process('cheap')
process('expensive')