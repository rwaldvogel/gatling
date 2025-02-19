---
title: "Realtime monitoring"
description: "Analyze reports metrics as it is happening"
lead: "Analyze reports metrics as it is happening"
date: 2021-04-20T18:30:56+02:00
lastmod: 2021-04-20T18:30:56+02:00
weight: 020
---

## Introduction

By default, Gatling only provides live feedback in the console output, and generates static HTML reports.

## Gatling Enterprise

[Gatling Enterprise](https://gatling.io/enterprise/), formerly known as Gatling FrontLine, is a management interface for Gatling, that includes advanced metrics and advanced features for integration and automation.

Amongst other features like clustering support, MQTT support, advanced integration with CI tools (Jenkins, TeamCity and Bamboo) and with Grafana,
Gatling Enterprise offers entreprise-grade realtime monitoring and metrics persistence.

{{< img src="enterprise.png" alt="Gatling Enterprise" >}}

For more information, please get in touch at **contact@gatling.io**.

## Graphite-InfluxDB-Grafana

Gatling can provide live metrics via the Graphite protocol which can be persisted and visualised.

The sections below describe how to configure Gatling with InfluxDB and Graphite, and use Grafana as a graphing library. We also present a lo-fi solution which prints parsed Graphite data to standard out.

{{< alert warning >}}
As explained in [one of our blog posts](/2018/11/metrics-analysis-part-1-mean-standard-deviation/), Graphite and InfluxDB can't store distributions but only numbers. As a result, only one-second-resolution non-aggregated response time stats are correct.

**All aggregations will result in computing averages on percentiles and will inherently be broken.**

This is not a limitation of Gatling, but a limitation of those time series databases.
{{< /alert >}}

### Gatling

In the `gatling.conf` add "graphite" to the data writers and specify the host
of the Carbon or InfluxDB server.

```hocon
gatling {
  data {
    writers = [console, file, graphite]

    graphite {
      host = "192.0.2.235"  # InfluxDB or Carbon server
      port = 2003           # The port to which the Carbon server listens to (2003 is default for plaintext, 2004 is default for pickle)
      # writePeriod = 1     # Default write interval of one second
    }
  }
}
```

### InfluxDB

InfluxDB is a popular time-series database[^1].

[^1]: A time series is a sequence of data points that are measured over time, and a time-series database optimises that data.

#### Install

Install [InfluxDB](https://influxdata.com/downloads/#influxdb) through your package manager.


#### Graphite plugin

Add the below to the Graphite section of `/etc/influxdb/influxdb.conf`

```
[[graphite]]
  enabled = true
  database = "gatlingdb"
    
  templates = [
    "gatling.*.*.*.* measurement.simulation.request.status.field",
    "gatling.*.users.*.* measurement.simulation.measurement.request.field"
  ]
```

#### Start

```console
sudo service influxdb start
```

#### Verification

From the [gatling-sbt-plugin-demo project](https://github.com/gatling/gatling-sbt-plugin-demo) run the ComputerWorld simulation, and

```console
influx -database 'gatlingdb' -execute 'SELECT * FROM gatling where count != 0 LIMIT 10'
```

You should be presented with something similar to this:

```
name: gatling
time                count max mean min percentiles50 percentiles75 percentiles95 percentiles99 request                  simulation    status stdDev
----                ----- --- ---- --- ------------- ------------- ------------- ------------- -------                  ----------    ------ ------
1485784307000000000 3     23  21   21  21            21            23            23            addNewComputer           computerworld all    0
1485784307000000000 3     26  23   22  22            22            26            26            postComputers_Redirect_1 computerworld ok     1
1485784307000000000 12    81  31   21  23            27            43            81            allRequests              computerworld all    16
1485784307000000000 3     27  24   22  24            24            27            27            postComputers            computerworld all    2
1485784307000000000 3     81  55   43  43            43            81            81            getComputers             computerworld ok     17
1485784307000000000 3     23  21   21  21            21            23            23            addNewComputer           computerworld ok     0
1485784307000000000 3     81  55   43  43            43            81            81            getComputers             computerworld all    17
1485784307000000000 12    81  31   21  23            27            43            81            allRequests              computerworld ok     16
1485784307000000000 3     26  23   22  22            22            26            26            postComputers_Redirect_1 computerworld all    1
1485784307000000000 3     27  24   22  24            24            27            27            postComputers            computerworld ok     2
```

### Graphite

#### Install

Graphite can be installed through [Synthesize](https://github.com/obfuscurity/synthesize) on Ubuntu 14.04

#### Configuration

In `$GRAPHITE_HOME/conf/storage-schemas.conf`:

```
[Gatling stats]
priority = 110
pattern = ^gatling\..*
retentions = 1s:6d,10s:60d
```

If you use a different writePeriod in your Graphite data writer configuration,
make sure that your smallest retention is equal or greater than your
writePeriod.

In `$GRAPHITE_HOME/conf/storage-aggregation.conf`:

```
[sum]
pattern = \.count$
xFilesFactor = 0
aggregationMethod = sum

[min]
pattern = \.min$
xFilesFactor = 0.1
aggregationMethod = min

[max]
pattern = \.max$
xFilesFactor = 0.1
aggregationMethod = max

[default_average]
pattern = .*
xFilesFactor = 0.3
aggregationMethod = average
```

### collectd

In collectd.conf

```
...
LoadPlugin write_graphite
...
<Plugin write_graphite>
  <Node "example">
    Host "receiving.server.hostname"
    Port "2003"
    Protocol "tcp"
    LogSendErrors true
    Prefix "collectd"
    Postfix "collectd"
    StoreRates true
    AlwaysAppendDS false
    EscapeCharacter "_"
  </Node>
</Plugin>
...
```

### Grafana

Grafana is a popular open-source graphing application.

There are [binaries](http://docs.grafana.org/installation/) for all the major GNU/Linux distributions.

Once Grafana is installed and the service is running navigate to :3000 and
sign-in as admin/admin (change in /etc/grafana/grafana.ini at the earliest
opportunity).

InfluxDB or Graphite can be set as a datasource as described [here](http://docs.grafana.org/datasources/overview/).
There is a ready made [Grafana template](https://github.com/gatling/gatling/tree/main/src/docs/content/tutorials/realtime_monitoring/code/gatling.json)
if InfluxDB is used as a datasource. The graphs should look similar to the below when running a simulation:

{{< img src="gatling-grafana.png" alt="gatling-grafana" >}}

### Ports

The ports 2003 (Graphite protocol), 8086 (InfluxDB network communication) and
3000 (Grafana) will need to be exposed on the Grafana-InfluxDB box.

### Lo-fi

Netcat can be used to listen to the Graphite port. The below awk
script parses the data.

```
BEGIN{
  print "--------- stats ....... timestamp RPS error_percent 95percentile_response_time active_users -----";
  curr=0
}

{
  if($NF != curr) {
    print $NF" "n" "epct" "ptile" "u;
  }
  curr=$NF
}

/allRequests.all.count/        {n=$2}
/allRequests.ko.count/         {e=$2; if(n==0){epct=0}else{epct=int(e/n*100)}}
/allRequests.ok.percentiles95/ {ptile=$2}
/users.allUsers.active/        {u=$2}
```

To run the script:

```console
nc -l 2003 | awk -f a.awk
```
