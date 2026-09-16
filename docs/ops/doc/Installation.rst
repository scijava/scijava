============
Installation
============

Fiji
====

SciJava-Ops ships as a part of `Fiji <https://fiji.sc>`_'s ``latest`` distribution. There are a few ways to get this distribution:

Installing Fiji ``latest``
--------------------------

If you do not yet have Fiji installed, you can download it `here <https://imagej.net/software/fiji/downloads>`_. **Be sure to download the latest distribution.**

Updating Fiji
-------------

If you have Fiji Latest, you must ensure your Fiji contains SciJava Ops. You can check by:

1. With Fiji open, navigate to ``Help → Update...``
  * If Fiji tells you to restart, close Fiji, relaunch, and proceed again from Step 1.
2. Click on ``Advanced Mode``
3. In the ``Search``bar, type ``scijava-ops-engine``. You should see ``jars/scijava-ops-engine.jar`` present in the results

If you do **not** see ``jars/scijava-ops-engine.jar``, you must update your Fiji installation.


.. figure:: https://media.scijava.org/scijava-ops/1.1.0/scijava-ops-updater.jpg
   :align: center

   Fiji's Advanced Updater panel, showing SciJava Ops installed

Testing SciJava Ops
===================

You can test SciJava Ops' installation:

1. Open a new Script: ``File -> New -> Script...``
2. Paste the following code
    .. tabs::

        .. code-tab:: scijava-groovy

            #@OpEnvironment ops

            print(ops.infos().size() + " Ops available!")
3. Click ``Run``. If you should see a nonzero number of Ops printed to the scripting console, SciJava Ops has been successfully installed!
