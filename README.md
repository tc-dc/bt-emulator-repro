This is a "simple" reproduction case for (multiple?) bugs in the Bigtable emulator.

When run against Bigtable itself, this `ReadRowsRequest` returns 12 rows.
When run against the Bigtable emulator, the filter seems to be ignored, and 240 rows are returned.
