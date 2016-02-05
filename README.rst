================
cassandra-brotli
================

.. image:: https://travis-ci.org/eevans/cassandra-brotli.svg?branch=master
               :target: https://travis-ci.org/eevans/cassandra-brotli

Brotli is an open source data compression library developed by Jyrki Alakuijala
and Zoltán Szabadka [#]_. It is based on a modern variant of the LZ77 algorithm,
Huffman coding and 2nd order context modeling.

Brotli typically gives an increase of 20% in compression density for text, while
compression and decompression speeds are roughly unchanged when compared to
`deflate`_.

This module provides Brotli compression of persisted data for
`Apache Cassandra`_.

.. contents:: Table of Contents

Current Status
--------------
*Works-for-me* (read: experimental).

Requirements
------------
* JDK 8
* Maven
  
Building
--------
This module uses `jbrotli`_, which relies on a native (architecture dependent)
shared library.  The *cassandra-brotli* build system produces a shaded jar file
that contains all of the (non-Cassandra) dependencies, including a copy of the
native library apropos to the build machine architecture.  *In other words, you
must build the jar on a target system that matches where you plan to use it.*

To build the shaded jar::

   $ mvn package

Installation
------------
Add the jar file to Cassandra's classpath.  The easiest way to achieve this is
to copy it to the ``lib/`` directory::

  $ cp target/cassandra-brotli-{version}.jar {cassandra_home}/lib

Enabling Brotli Compression
---------------------------  
To enable Brotli compression on an existing table, issue an ``ALTER TABLE``
CQL statement that specifies the ``BrotliCompressor`` class (see the
`CQL docs`_ for a full list of standard options, or the `Options`_ section below
for a list of Brotli-specific ones)::

  cqlsh> ALTER TABLE keyspace.table WITH
     ...     compression = {'sstable_compression': 'org.apache.cassandra.io.compress.BrotliCompressor', [options]}

For example, to enable Brotli compression with a quality level of 1::

  cqlsh> ALTER TABLE keyspace.table WITH
     ...     compression = {'sstable_compression': 'org.apache.cassandra.io.compress.BrotliCompressor', 'quality': 1}

Options
~~~~~~~

``quality``
  An integer in the range ``0..11``.  Controls the compression-speed vs.
  compression-density tradeoffs. The higher the quality, the slower the
  compression.  Default: ``0``.

``lgwin``
  An integer value in the range ``10..24``.  The base 2 logarithm of the sliding
  window size; The value corresponds to the window sizes in the range of
  (2^10)-16 to (2^24)-16 bytes (1008-16777200). Default: ``22`` (4194288 bytes).

``lgblock``
  A value in the range ``16..24``.  The base logarithm of the maximum input
  block size.  Default: ``0`` (results in the input block size being based on
  ``quality``).

``mode``
  One of ``generic``, ``text``, or ``font`` (``generic`` is likely the only
  value that makes sense here).  Default: ``generic``.


.. _CQL docs: http://cassandra.apache.org/doc/cql3/CQL.html#compressionOptions
.. _Apache Cassandra: http://cassandra.apache.org
.. _deflate: https://en.wikipedia.org/wiki/DEFLATE
.. _jbrotli: https://github.com/MeteoGroup/jbrotli


.. [#] http://www.ietf.org/id/draft-alakuijala-brotli-08.txt
