# Announcing FHIRBase  0.1

We are happy to announce today the first release of [FHIRBase](https://github.com/fhirbase/fhirbase),
open source relational storage for FHIR with document API
based on PostgreSQL.

FHIRBase is an attempt to take the best parts of Relational & Document Databases for persistence of FHIR resources.
FHIRBase stores resources relationally and gives you the power of SQL for querying & aggregating.
At the same time FHIRBase provides a set of SQL procedures & views to persist and retrieve resources
as a json documents in one hop.

You can find more information in the [README](https://github.com/fhirbase/fhirbase/blob/master/README.md) and
play with FHIRBase on [live demo site](http://try-fhirbase.hospital-systems.com/).

There are plans for:

* FHIR extensions support
* Resource versioning support
* Semi-automatic database migration to future FHIR versions
* FHIR server with advanced query capabilities based on FHIRBase

FHIRBase is an open source project, so if you found it promising and valuable:

* Spread the word [FHIRBase](https://github.com/fhirbase/fhirbase)
* Report us your ideas, enhancements & bugs on [github](https://github.com/fhirbase/fhirbase/issues)
* Contribute the code [using pull requests](https://help.github.com/articles/using-pull-requests)
