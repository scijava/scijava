#Scijava mechanism for object serialization

This incubator contains a mechanism that uses Scijava extensibility mechanism in order to register adapters that can be dispatched in multiple repositories.

Internally the Gson library is used with `RunTimeAdapters`, which allow to serialize interfaces.

TODO:
* simple examples
* tests
* think about safety, or at least debugging ease (put a UUID)