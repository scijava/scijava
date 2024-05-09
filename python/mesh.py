def copy_mesh(src, dest):
        vIndexMap = {}
        # Copy the vertices, keeping track when indices change.
        for v in src.vertices():
            srcIndex = v.index()
            destIndex = dest.vertices().add(
                v.x(), v.y(), v.z(),
                v.nx(), v.ny(), v.nz(),
                v.u(), v.v()
            )
            if srcIndex != destIndex:
                # NB: If the destination vertex index matches the source, we
                # skip recording the entry, to save space in the map. Later, we
                # leave indexes unchanged which are absent from the map.
                #
                # This scenario is actually quite common, because vertices are
                # often numbered in natural order, with the first vertex having
                # index 0, the second having index 1, etc., although it is not
                # guaranteed.
                vIndexMap.put( srcIndex, destIndex )

        # Copy the triangles, taking care to use destination indices.
        for tri in src.triangles():
            v0src = tri.vertex0()
            v1src = tri.vertex1()
            v2src = tri.vertex2()
            v0 = vIndexMap.get( v0src, v0src )
            v1 = vIndexMap.get( v1src, v1src )
            v2 = vIndexMap.get( v2src, v2src )

            dest.triangles().add( v0, v1, v2, tri.nx(), tri.ny(), tri.nz() )
