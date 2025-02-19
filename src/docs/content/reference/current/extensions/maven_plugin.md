---
title: "Maven Plugin"
description: "Maven plugin to run Gatling test"
lead: "The Maven plugin allows you to run Gatling test from the command line, without the bundle"
date: 2021-04-20T18:30:56+02:00
lastmod: 2021-04-20T18:30:56+02:00
weight: 007001
---

Using this plugin, Gatling can be launched when building your project, for example with your favorite Continuous Integration (CI) solution.

## Versions

Check out available versions on [Maven Central](https://search.maven.org/search?q=g:io.gatling%20AND%20a:gatling-maven-plugin&core=gav).

Beware that milestones (M versions) are not documented for OSS users and are only released for [Gatling Enterprise](https://gatling.io/enterprise/) customers.

## Setup

In your `pom.xml`, add:

```xml
<dependencies>
  <dependency>
    <groupId>io.gatling.highcharts</groupId>
    <artifactId>gatling-charts-highcharts</artifactId>
    <version>MANUALLY_REPLACE_WITH_LATEST_VERSION</version>
    <scope>test</scope>
  </dependency>
</dependencies>

<plugin>
  <groupId>io.gatling</groupId>
  <artifactId>gatling-maven-plugin</artifactId>
  <version>MANUALLY_REPLACE_WITH_LATEST_VERSION</version>
</plugin>
```

## Demo sample

You can find a [sample project demoing the gatling-maven-plugin](https://github.com/gatling/gatling-maven-plugin-demo) in Gatling's Github organization.

## Usage

### Directly running maven goal

You can directly launch the gatling-maven-plugin with the `test` goal:

  mvn gatling:test


The gatling-maven-plugin will take care of compiling your code.

### Running from maven lifecycle

If you want to have the gatling-maven-plugin during maven's phases lifecycle, eg because you want it to be triggered with `mvn verify`,
you must explicitly configure an execution block:

```xml
<plugin>
  <groupId>io.gatling</groupId>
  <artifactId>gatling-maven-plugin</artifactId>
  <version>MANUALLY_REPLACE_WITH_LATEST_VERSION</version>
  <executions>
    <execution>
      <goals>
        <goal>test</goal>
      </goals>
    </execution>
  </executions>
</plugin>
```

The `test` goal is bound by default to the `integration-test` phase

## Configuration

The plugin supports many configuration options, eg:

```xml  
<plugin>
  <groupId>io.gatling</groupId>
  <artifactId>gatling-maven-plugin</artifactId>
  <version>MANUALLY_REPLACE_WITH_LATEST_VERSION</version>
  <configuration>
      <simulationClass>foo.Bar</simulationClass>
  </configuration>
</plugin>
```

Use `mvn gatling:help -Ddetail=true -Dgoal=test` to print the description of all the available configuration options on the `test` goal.
Use `mvn gatling:help -Ddetail=true -Dgoal=recorder` to print the description of all the available configuration options on the `recorder` goal.

### Includes/Excludes filters

When running multiple simulations, you can control which simulations will be triggers with the `includes` and `excludes` filters.
Those use the ant pattern syntax and are matched against class names.
Also note that those filters are only applied against the classes that were compiled from sources in the project the plugin is set.

```xml  
<configuration>
  <!--   ...  -->
  <runMultipleSimulations>true</runMultipleSimulations>
  <includes>
    <include>my.package.*</include>
  </includes>
  <excludes>
    <exclude>my.package.IgnoredSimulation</exclude>
  </excludes>
</configuration>
```

{{< alert tip >}}
The order of filters has no impact on execution order, simulations will be sorted by class name alphabetically.
{{< /alert >}}

## Working Along with scala-maven-plugin

By default, the gatling-maven-plugin takes care of compiling your Scala code, so you can directly run `mvn gatling:test`.

Then, for some reason, you might want to have the [scala-maven-plugin](https://github.com/davidB/scala-maven-plugin) take care of compiling.

Make sure to properly configure it, in particular set `testSourceDirectory` to point to the directory that contains your Gatling classes, typically:

```xml  
<build>
  <testSourceDirectory>src/test/scala</testSourceDirectory>
</build>
```

Then, you should disable the Gatling compiler so you don't compile twice:

```xml  
<configuration>
  <disableCompiler>true</disableCompiler>
</configuration>
```

## Overriding the logback.xml file

You can either have a `logback-test.xml` that has precedence over the embedded `logback.xml` file, or add a JVM option `-Dlogback.configurationFile=myFilePath`.

## Sources

If you're interested in contributing, you can find the [gatling-maven-plugin sources](https://github.com/gatling/gatling-maven-plugin) on GitHub.
