SciJava Ops: An Improved Algorithms Framework for Fiji and Beyond
=================================================================

This page serves as a stable location for the SciJava Ops Paper and associated resources. If you use, extend, or contribute to SciJava Ops, please cite our publication:

  Selzer GJ, Rueden CT, Hiner MC, Evans EL, Kolb D, Wiedenmann M, Birkhold C, Buchholz T-O, Helfrich S, Northan B, Walter A, Schindelin J, Pietzsch T, Saalfeld S, Berthold MR and Eliceiri KW. (2024). SciJava Ops: an improved algorithms framework for Fiji and beyond. *Front. Bioinform. 4:1435733*. `doi:10.3389/fbinf.2024.1435733 <https://doi.org/10.3389/fbinf.2024.1435733>`_

The following sections highlight some of the use cases found within the paper.

Python (scyjava)
----------------

This use case illustrates the ease with which SciJava Ops can be accessed in Python, showcasing ``OpEnvironment`` setup and simple image processing. The full workflow can be found in the `scyjava use case <../examples/scyjava.html>`_.

+--------------+--------------------------------------------------------------------------------------+
| |scyj_thumb| |`3D 3T3 Mouse Nucleus <https://media.scijava.org/scijava-ops/1.0.0/3t3_nucleus.tif>`_ |
+--------------+--------------------------------------------------------------------------------------+

Fluorescence Lifetime Image Analysis
------------------------------------

This use case illustrates how SciJava Ops can be freely extended with additional algorithms libraries, making use of the SciJava framework for convenience and performance in FLIM analysis. The full workflow can be found in the `FLIM use case <../examples/flim_analysis.html>`_.

+--------------+-----------------------------------------------------------------------------------+
| |flim_thumb| | `BPAE cells <https://media.scijava.org/scijava-ops/1.0.0/flim_example_data.sdt>`_ |
+--------------+-----------------------------------------------------------------------------------+

Spatially Adapted Colocalization Analysis
-----------------------------------------

This use case illustrates the novel scientific utility of the SciJava Ops Image library using powerful algorithms for pixel colocalization. The full workflow can be found in the `SACA use case <../examples/deconvolution.html>`_.

+--------------+----------------------------------------------------------------------------------------------------------------------+
| |saca_thumb| | `HeLa cell expressing HIV gene products <https://media.scijava.org/scijava-ops/1.0.0/hela_hiv_gag_ms2_mcherry.tif>`_ |
+--------------+----------------------------------------------------------------------------------------------------------------------+

Deconvolution
-------------

This use case illustrates the novel scientific utility of the SciJava Ops Image library using powerful algorithms for image deconvolution. The full workflow can be found in the `deconvolution use case <../examples/deconvolution.html>`_.

+---------------+-------------------------------------------------------------------------------------------+
| |decon_thumb| | `3D HeLa nucleus <https://media.scijava.org/scijava-ops/1.0.0/hela_nucleus.tif>`_         |
+---------------+-------------------------------------------------------------------------------------------+

.. |decon_thumb| image:: https://media.scijava.org/scijava-ops/1.0.0/hela_nucleus_thumbnail.png
    :width: 10em
.. |flim_thumb| image:: https://media.scijava.org/scijava-ops/1.0.0/flim_example_input_56.png
    :width: 10em
.. |saca_thumb| image:: https://media.scijava.org/scijava-ops/1.0.0/hela_hiv_gag_ms2_mcherry_thumbnail.png
    :width: 10em
.. |scyj_thumb| image:: https://media.scijava.org/scijava-ops/1.0.0/3t3_nucleus_thumbnail.png
    :width: 10em

