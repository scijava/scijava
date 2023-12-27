# Scijava mechanism for object serialization

The goal of scijava persist is to serialize 'easily' and in a human readable way objects (interfaces) which can't be directly serialized. Gson can do that conveniently. It's mainly what QuPath uses for all sorts of things, and that's why I came to use it.

In brief: you can serialize interfaces with gson, but gson needs to be aware of all implementations that may exist, which are potentially in different repositories.

For a practical example: I need to serialize RealTransform objects. There are many implementations, most are in imglib2-realtransform, some in bigwarp (Wrapped2DTransformAs3D), and some in bigdataviewer-biop-tools (Elliptical3DTransform).

With scijava-persist, you can create a (custom) serializer for each implementation, in different repositories. With the scijava discovery mechanism, you can just ask for a scijava serializer and it'll be aware of all implementations of all realtransform objects, provided that a serializer plugin was declared.

Thus if somebody comes with an exotic transform in an exotic repository, I could serialize this object conveniently, without modifiying anything in my code.

Internally the Gson library is used with `RunTimeAdapters`, which allow to serialize interfaces.

TODO:
* simple examples
* tests
* think about safety, or at least debugging ease (put a UUID)
