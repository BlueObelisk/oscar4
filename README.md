
# OSCAR4
[![Build Status](https://travis-ci.org/BlueObelisk/oscar4.svg?branch=master)](https://travis-ci.org/BlueObelisk/oscar4) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/uk.ac.cam.ch.wwmm.oscar/oscar4/badge.svg)](https://maven-badges.herokuapp.com/maven-central/uk.ac.cam.ch.wwmm.oscar/oscar4)

OSCAR (Open Source Chemistry Analysis Routines) is an open source extensible system for the automated annotation of chemistry in scientific articles. It can be used to identify chemical names, reaction names, ontology terms, enzymes and chemical prefixes and adjectives, and chemical data such as state, yield, IR, NMR and mass spectra and elemental analyses. In addition, where possible, any chemical names detected will be annotated with structures derived either by lookup, or name-to-structure parsing using [OPSIN](https://opsin.ch.cam.ac.uk/) or with identifiers from the [ChEBI](https://www.ebi.ac.uk/chebi/) (`Chemical Entities of Biological Interest’) ontology.

OSCAR has been under development since 2002. The current version, OSCAR4, focuses on providing a core library that facilitates integration with other tools. Its simple to use API is modularised to promote extension into other domains and allows for its use within workflow systems like Taverna and U-Compare.

OSCAR is developed by the [Murray-Rust](http://www-pmr.ch.cam.ac.uk) research group at the [Unilever Centre for Molecular Science Informatics](https://www-cmi.ch.cam.ac.uk/), [University of Cambridge](https://www.cam.ac.uk/). The corresponding publication can be found [here](http://dx.doi.org/10.1186/1758-2946-3-41) and the authors would appreciate it if this is cited in any work that makes use of the code.

## Examples

The following code will identify chemical named entities in text, and output a list of them together with their Standard InChI, when available.

```java
String s = "....";

Oscar oscar = new Oscar();
List<ResolvedNamedEntity> entities = oscar.findAndResolveNamedEntities(s);
for (ResolvedNamedEntity ne : entities) {
    System.out.println(ne.getSurface());
    ChemicalStructure stdInchi = ne.getFirstChemicalStructure(FormatType.STD_INCHI);
    if (stdInchi != null) {
        System.out.println(stdInchi);
    }
    System.out.println();
}
```

## Support

[Issue/Feature Request Tracker](https://github.com/blueobelisk/oscar4/issues)

[Mailing List (Google Group)](https://groups.google.com/forum/#!forum/oscar4-users)

## Downloads

OSCAR4 is available for download:

[OSCAR4-4.2.2 JAR with dependencies](http://repo.maven.apache.org/maven2/uk/ac/cam/ch/wwmm/oscar/oscar4-all/4.2.2/oscar4-all-4.2.2-with-dependencies.jar)
