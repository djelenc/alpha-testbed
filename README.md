[![](https://jitpack.io/v/djelenc/alpha-testbed.svg)](https://jitpack.io/#djelenc/alpha-testbed)

# Alpha Testbed

Alpha testbed, or ATB for short, is a tool for benchmarking trust and reputation models. It is intended to be used by trust and reputation researchers.

ATB allows researchers to easily implement new trust models, evaluation scenarios, and benchmark metrics. These implementations can then be shared to enable straightforward comparison between various trust and reputation models.

For additional information please refer to the Wiki.

Also, please note that the site is still under construction, thus not all information is available at this point. We are doing our best to improve the site daily.

## Running it

Alpha testbed is a Java program that requires an installed maven and Java 8. To run it, simply run `mvn exec:java`. 

## Adding Alpha Testbed to your project

You can add ATB to your existing JVM-based (Java, Kotlin, Scala, etc.) projects.

First, add the JitPack repository to your build file. For maven based project,  add the following XML to your `pom.xml`.
```xml
<repositories>
	<repository>
	    <id>jitpack.io</id>
	    <url>https://jitpack.io</url>
	</repository>
</repositories>
```
Then, add the actual dependency. For maven-based projects, use the following XML snippet.
```xml
<dependency>
	<groupId>com.github.djelenc</groupId>
	<artifactId>alpha-testbed</artifactId>
	<version>1.0.3</version>
</dependency>
```
If you want to use the most recent version, replace the version number with `master-SNAPSHOT`.

### JavaDocs

[JavaDocs are available on jitpack.](https://jitpack.io/com/github/djelenc/alpha-testbed/v1.0.0/javadoc/index.html)

## Papers

These papers describe ATB in more detail.

* [David Jelenc, Ramón Hermoso, Jordi Sabater-Mir, and Denis Trček. Decision Making Matters: A Better Way to Evaluate Trust Models. Knowledge-Based Systems, 2013.](http://www.sciencedirect.com/science/article/pii/S0950705113002189)

* [David Jelenc, Ramón Hermoso, Sascha Ossowski, and Denis Trček. Alpha Test-bed: A New Approach for Evaluating Trust Models. In Proceedings of The Third International Workshop on Infrastructures and Tools for Multiagent Systems (ITMAS), Valencia, Spain, 2012.](http://riunet.upv.es/bitstream/handle/10251/16889/ITMAS%202012%20.pdf?...#page=57)
