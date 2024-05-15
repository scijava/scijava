import schema
import yaml
import sys

file = sys.argv[1]

s = schema.Schema([{
    "op": {
        "names": [str],
        "description": str,
        "source": str,
        "parameters": [{
            "name": str,
            "description": str,
            "type": str,
            "parameter type": str, # TODO - can we define this as an enum?
            "nullable": bool,
        }],
        "authors": [str],
        "version": str,
        "priority": float,
        "tags": {}
    }
}], ignore_extra_keys=True)

with open(file) as f:
    y = yaml.safe_load("".join(f.readlines()))

s.validate(y)
print("Op YAML is valid!")   
