'''
Python entry point script for using SciJava Ops in python. After running this script
you will have an "ops" variable that can be used to call ops, and explore available ops.

For example:
    ops.math.add(27, 15)
    ops.filter.addPoissonNoise(img)

Variables:
    ops - fully intialized OpGateway with all SciJava and ImageJ Ops available
    env - OpEnvironment for more traditional Java-style API
'''

from scyjava import config, jimport
config.endpoints.append('net.imagej:imagej-ops2-tutorial:0-SNAPSHOT')

import gateways as g

ops = g.ops
env = g.env
