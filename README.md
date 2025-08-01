
# OSCAR4
![Java CI with Maven](https://github.com/BlueObelisk/oscar4/workflows/Java%20CI%20with%20Maven/badge.svg) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/uk.ac.cam.ch.wwmm.oscar/oscar4/badge.svg)](https://maven-badges.herokuapp.com/maven-central/uk.ac.cam.ch.wwmm.oscar/oscar4)

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

## Deployment to the Maven central repository
1) Create a gpg key
``` 
gpg --full-generate-key --pinentry-mode=loopback
```
Note, I think it must be RSA and the largest you can create. Remember to protect it with a password.

2) Upload it to http://keyserver.ubuntu.com/
```
gpg --armor --export mjw@mjw.name
```
Take the output from the above command and paste it into that URL.

3) Create an account on https://central.sonatype.com/

4) Log in and make sure you have access to the Namespace you want to deploy to:
   https://central.sonatype.com/publishing/namespaces
   
For this repo, it will be uk.ac.cam.ch.wwmm; if you do not, you will need to request access via someone else, who does have access.

5) You will need to create a token for deployment via https://central.sonatype.com/account
This needs to be pasted into your ~/.m2/settings.xml, e.g.:
```
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
	https://maven.apache.org/xsd/settings-1.0.0.xsd">
<servers>
  <server>
	<id>central</id>
	<username>foo</username>
	<password>bar</password>
  </server>
</servers>
</settings>
```

6) Note, this assumes you have a ssh key to access github. Build, package and sign: 
```
mvn -Dusername=git release:prepare -DautoVersionSubmodules=true -DreleaseVersion=5.3.0 -DdevelopmentVersion=5.4-SNAPSHOT
```

- Set the tag label as 5.2.1 when requested
- Enter your GPG password

7) Upload it to central.sonatype.com
```
mvn -Psonatype-oss-release  release:perform -DconnectionUrl=scm:git:https://github.com/BlueObelisk/oscar4 -Dtag=5.3.0
```
- Enter your GPG password

8) Log into https://central.sonatype.com/publishing/deployments
The deployment should be here, pending to go; if everything is green, hit publish.


## Support

[Issue/Feature Request Tracker](https://github.com/blueobelisk/oscar4/issues)

[Mailing List (Google Group)](https://groups.google.com/forum/#!forum/oscar4-users)

## Downloads

OSCAR4 is available for download:

[OSCAR4-5.2.0 JAR with dependencies](https://repo.maven.apache.org/maven2/uk/ac/cam/ch/wwmm/oscar/oscar4-all/5.2.0/oscar4-all-5.2.0-with-dependencies.jar)
