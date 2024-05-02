# Check how many services are found to make sure discovery is working
from scyjava import config, jimport
config.endpoints.append('org.scijava:scijava-ops-tutorial:0-SNAPSHOT')
Add = jimport('org.scijava.ops.engine.math.Add')
ServiceLoader = jimport('java.util.ServiceLoader')
OpClass = jimport('org.scijava.ops.spi.Op')
s = ServiceLoader.load(OpClass)
ServiceLoader = jimport('java.util.ServiceLoader')
d = jimport('org.scijava.discovery.Discoverer')
ServiceLoader.load(d).iterator().next()
