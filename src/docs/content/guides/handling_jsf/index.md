---
title: "Handling JSF"
description: "Handling Jakarta Server Faces with Gatling DSL"
lead: "Handling Jakarta Server Faces with Gatling DSL"
date: 2021-04-20T18:30:56+02:00
lastmod: 2021-04-20T18:30:56+02:00
weight: 040
---

## Basic JSF

JSF requires a parameter named `javax.faces.ViewState` to be captured on every page and be passed in every POST request.

Adding a check for capturing the value and a param on very request would be very cumbersome.
Hopefully, we can factor out these operations.

Define factory methods for building JSF requests that would automatically perform those operations:

{{< include-code "factory-methods" java kt scala >}}

You can then build your requests just like you're used to:

{{< include-code "example-scenario" java kt scala >}}

{{< alert tip >}}
The sample above is taken from the [Primefaces demo](http://www.primefaces.org/showcase-labs).
{{< /alert >}}

See Rafael Pestano's [demo project](https://github.com/rmpestano/gatling-jsf-demo) for a complete sample.

## Trinidad

Trinidad's `_afPfm` query parameter can be handled similarly:

{{< include-code "trinidad" java kt scala >}}
