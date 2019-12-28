
# OSCAR4

OSCAR (Open Source Chemistry Analysis Routines) is an open source extensible system for the automated annotation of chemistry in scientific articles. It can be used to identify chemical names, reaction names, ontology terms, enzymes and chemical prefixes and adjectives, and chemical data such as state, yield, IR, NMR and mass spectra and elemental analyses. In addition, where possible, any chemical names detected will be annotated with structures derived either by lookup, or name-to-structure parsing using OPSIN or with identifiers from the ChEBI(`Chemical Entities of Biological Interest’) ontology.

OSCAR has been under development since 2002. The current version, OSCAR4, focuses on providing a core library that facilitates integration with other tools. Its simple to use API is modularised to promote extension into other domains and allows for its use within workflow systems like Taverna and U-Compare.

OSCAR is developed by the Murray-Rust research group at the Unilever Centre for Molecular Science Informatics, University of Cambridge. The corresponding publication can be found here and the authors would appreciate it if this is cited in any work that makes use of the code.

## Documentation

Examples

## Support

Issue/Feature Request Tracker

Mailing List (Google Group)

## Downloads

OSCAR4 is available for download:

OSCAR4-4.2.2 JAR with dependencies

## Maven Repository

OSCAR4 is also available via Maven, and is on Maven Central:

````
<dependencies>
	<dependency>
		<groupId>uk.ac.cam.ch.wwmm.oscar</groupId>
		<artifactId>oscar4-api</artifactId>
		<version>4.2.2</version>
	</dependency>
</dependencies>
````
