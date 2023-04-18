from scyjava import jimport
import scyjava.config
from types import MethodType
jars=scyjava.config.find_jars("C:\\Users\\hiner\\code\\scijava\\incubator\\imagej\\imagej-ops2\\target\\dependency\\")
scyjava.config.add_classpath(*jars)
scyjava.config.add_classpath("C:\\Users\\hiner\\code\\scijava\\incubator\\imagej\\imagej-ops2\\target\\imagej-ops2-0-SNAPSHOT.jar")
env=jimport('org.scijava.ops.engine.DefaultOpEnvironment')()

op_names={str(name) for info in env.infos() for name in info.names()}

#ns={op[:op.index('.')] for op in op_names if op.find('.') >= 0}

class OpNamespace:
    def __init__(self, env, ns):
        self.env = env
        self.ns = ns

class OpGateway(OpNamespace):
    def __init__(self, env):
        super().__init__(env, 'global')

def nary(env, fqop, arity):
    arities=[env.nullary, env.unary, env.binary, env.ternary, env.quaternary, env.quinary, env.senary, env.septenary, env.octonary, env.nonary, env.decenary, env.arity11, env.arity12, env.arity13, env.arity14, env.arity15, env.arity16]
    return arities[arity](fqop)

def add_op(c, op_name):
    if hasattr(c, op_name):
        return

    def f(self, *args, **kwargs):
        print(type(self))
        fqop = op_name if self.ns == 'global' else self.ns + "." + op_name
        run = kwargs.get('run', True)
        b = nary(self.env, fqop, len(args)).input(*args)
        # inplace
        # ops.filter.gauss(image, 5, inplace=0)
        if (inplace:=kwargs.get('inplace', None)) is not None:
            return b.mutate(inplace) if run else b.inplace(inplace)

        # computer
        # ops.filter.gauss(image, 5, out=result)
        if (out:=kwargs.get('out', None)) is not None:
            b=b.output(out)
            return b.compute() if run else b.computer()

        # function
        # gauss_op = ops.filter.gauss(image, 5)
        # result = ops.filter.gauss(image, 5)
        return b.apply() if run else b.function()

    if c == OpGateway:
        # op_name is a global.
        setattr(c, op_name, f)
    else:
#        if isinstance(c, MethodType):
#            print("oh no! - race condition happened")
        m=MethodType(f, c)
        setattr(c, op_name, m)

def add_namespace(c, ns, op_name):
    if not hasattr(c, ns):
        setattr(c, ns, OpNamespace(env, ns))
    add_op(getattr(c, ns), op_name)

for op in op_names:
    print(op)
    dot = op.find('.')
    if dot >= 0:
        ns=op[:dot]
        op_name=op[dot+1:]
        add_namespace(OpGateway, ns, op_name)
    else:
        print(f"registering global op: {op}")
        add_op(OpGateway, op)

