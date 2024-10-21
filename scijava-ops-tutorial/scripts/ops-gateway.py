"""
TODO: Module description.
"""

from types import MethodType
from typing import List, Sequence
import scyjava as sj

endpoints = [
        "net.imglib2:imglib2:6.4.0",
        "net.imglib2:imglib2-imglyb",
        "org.scijava:scijava-ops-engine",
        "org.scijava:scijava-ops-flim",
        "org.scijava:scijava-ops-image"
        ]

class OpNamespace:
    """Op namespace class.

    Represents intermediate Ops categories and Ops. For example,
    "math.add" and "features.haralick.asm".
    """

    def __init__(self, env: "scijava.OpEnvironment", ns: str):
        self.op = env.op
        self._env = env
        self._ns = ns


class OpsGateway(OpNamespace):
    """SciJava Ops Gateway class.

    Contains all other namespaces, in addition to all Ops in
    the "global" namespace.
    """

    def __init__(self, env):
        super().__init__(env, "global")

    def help(self, op_name: str = None):
        """SciJava Ops help.

        :param op_name: 

            Namespace and Op name (e.g. "filter.gauss")
        """
        if op_name:
            print(self._env.help(op_name), sep="\n")
        else:
            print(self._env.help(), sep="\n")

    def helpVerbose(self, op_name: str = None):
        """SciJava Ops verbose help.

        :param op_name: 

            Namespace and Op name (e.g. "filter.gauss")
        """
        if op_name:
            print(self._env.helpVerbose(op_name), sep="\n")
        else:
            print(self._env_helpVerbose(), sep="\n")


def init(endpoints: List[str]) -> OpsGateway:
    """Get the SciJava Ops Gateway.

    Initialize the JVM and return an instance of the
    SciJava Ops Gateway class.

    :return: 

        The SciJava Ops Gateway.
    """
    # configure and start the jvm
    if not sj.jvm_started():
        sj.config.endpoints = endpoints
        sj.start_jvm()

    # build Ops environment
    env = sj.jimport("org.scijava.ops.api.OpEnvironment").build()

    # find op names, base namespaces and intermediate namespaces
    op_names = _find_op_names(env)
    op_base_ns = []
    for op in op_names:
        op_sig = op.split(".")
        # skip "base" Ops
        if len(op_sig) == 1:
            continue
        else:
            op_base_ns.append(op_sig[0])
    op_base_ns = set(op_base_ns)

    # populate base namespaces
    for ns in op_base_ns:
        _add_namespace(OpsGateway, env, ns)

    # populate nested namespaces and ops
    for op in op_names:
        op_sig = op.split(".")
        sig_size = len(op_sig)
        if sig_size > 1:
            # find/add nested namespaces
            gateway_ref = OpsGateway  # used to reference nested namespaces
            for s in op_sig[:-1]:
                if hasattr(gateway_ref, s):
                    gateway_ref = getattr(gateway_ref, s)
                else:
                    _add_namespace(gateway_ref, env, s)
                    gateway_ref = getattr(gateway_ref, s)
            # add the Op to the nested namespace
            _add_op(gateway_ref, env, op_sig[-1])
        else:
            _add_op(OpsGateway, env, op_sig[0])

    return OpsGateway(env)

def _add_namespace(gc: OpsGateway, env: "scijava.OpEnvironment", ns: str):
    """Add an Op and it's namespace to the OpsGateway.

    Helper method to add an Op call with the appropriate nested
    OpNamespace instances if needed.

    :param gc:

        OpsGateway class
    
    :param env:

        SciJava Ops environment instance
    
    :param ns:

        Namespace
    
    :param on:

        Op name
    """
    if not hasattr(gc, ns):
        setattr(gc, ns, OpNamespace(env, ns))


def _add_op(gc: OpsGateway, env: "scijava.OpEnvironment", on: str):
    """Add an Op to the OpsGateway.

    Helper method to add an Op with its corresponding function call
    to the given class.

    :param gc:

        OpsGateway class
    
    :param env:

        SciJava Ops environment instance
    
    :param on:

        Op name
    """
    if hasattr(gc, on):
        return

    def f(self, *args, **kwargs):
        """Op call instance methods.

        Instance method to attach to the OpNamespace/OpsGateway that does
        the actual Op call.
        """
        fqop = on if self._ns == "global" else self._ns + "." + on
        run = kwargs.get("run", True)
        req = env.op(fqop).input(*args)

        # inplace Op requests
        if (inplace := kwargs.get("inplace", None)) is not None:
            return req.mutate(inplace) if run else req.inplace(inplace)

        # computer Op requests
        if (out := kwargs.get("out", None)) is not None:
            req = req.output(out)
            return req.compute() if run else req.computer()

        # function Op requests
        return req.apply() if run else req.function()

    if gc == OpsGateway:
        # Op name is a global
        setattr(gc, on, f)
    else:
        m = MethodType(f, gc)
        setattr(gc, on, m)


def _find_op_names(env: "scijava.OpEnvironment") -> set:
    """Find all Op names in a SciJava Ops environment.

    :return:
    
        Set of all Op names/signatures
    """
    return {str(name) for info in env.infos() for name in info.names()}

ops = init(endpoints)
