'''
Create namespace convenience methods for all discovered ops.
Assumes we're running in an environment with scyjava configured to have a classpath with some SciJava Ops implementations (i.e. org.scijava:scijava-ops-engine plus implementations)

Classes:
    OpNamespace
    OpGateway

Variables:
    env - OpEnvironment used to populate the OpGateway and OpNamespaces
    ops - OpGateway entry point
'''

from scyjava import jimport
from types import MethodType
env=jimport('org.scijava.ops.engine.DefaultOpEnvironment')()

op_names={str(name) for info in env.infos() for name in info.names()}

class OpNamespace:
    '''
    Represents intermediate ops categories. For example, "math.add" and "math.sub" are in both in the "math" namespace.
    '''
    def __init__(self, env, ns):
        self.env = env
        self.ns = ns

    def help(self, op_name=None):
        '''
        Convenience wrapper for OpEnvironment.description(), for information about available ops. Prints all returned information, line-by-line.
        '''
        print(*self.env.descriptions(op_name), sep = "\n")

class OpGateway(OpNamespace):
    '''
    Global base specialization of OpNamespace. Contains all other namespaces, in addition to all ops in the "global" namespace.
    '''
    def __init__(self, env):
        super().__init__(env, 'global')

def nary(env, fqop, arity):
    '''
    Helper method to convert a numerical arity to the corresponding OpEnvironment method

        Parameters:
            env (OpEnvironment): the environment being used
            fqop (str): the fully-qualified op name
            arity (int): the desired arity of the op

        Returns:
            The appropriate OpBuilder.Arity instance for invoking the given op with the indicated number of parameters
    '''
    arities=[env.nullary, env.unary, env.binary, env.ternary, env.quaternary, env.quinary, env.senary, env.septenary, env.octonary, env.nonary, env.decenary, env.arity11, env.arity12, env.arity13, env.arity14, env.arity15, env.arity16]
    return arities[arity](fqop)

def add_op(c, op_name):
    '''
    Helper method patch in a given op name as its corresponding function call within the given class

        Parameters:
            c (class): the OpNamespace/OpGateway to add a new function with the given op_name that calls the op
            op_name (str): the actual name of the op we're adding
    '''
    if hasattr(c, op_name):
        return

    def f(self, *args, **kwargs):
        '''
        Instance method to attach to our OpNamespace/OpGateway that does the actual op call
        '''
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
        m=MethodType(f, c)
        setattr(c, op_name, m)

def add_namespace(c, ns, op_name):
    '''
    Helper method to add an op call with nested OpNamespace instances if needed

        Parameters:
            c (class): the OpNamespace/OpGateway to add the given op and namespace
            ns(str): the namespace to nest the given op within
            op_name (str): the actual name of the op we're adding
    '''
    if not hasattr(c, ns):
        setattr(c, ns, OpNamespace(env, ns))
    add_op(getattr(c, ns), op_name)

# Entry point to do the namespace population.
# This modifies the OpGateway class to add a call for each op, with intermediate accessors by namespace
for op in op_names:
    dot = op.find('.')
    if dot >= 0:
        ns=op[:dot]
        op_name=op[dot+1:]
        add_namespace(OpGateway, ns, op_name)
    else:
        add_op(OpGateway, op)

# Make an instance of our modified OpGateway class as a global for external use
ops = OpGateway(env)
