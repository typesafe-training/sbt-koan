# sbt-koan #

sbt-koan is a plugin for [sbt](http://www.scala-sbt.org). Intended for training materials, it lets you navigate back and forth through step-by-step koan-style exercises, i.e. pre-written test cases.


## Installation ##

First you need to install sbt, please refer to the [sbt documentation](http://www.scala-sbt.org/release/docs/Getting-Started/Setup.html). Make sure that you are using a suitable version of sbt:

- sbt-koan 1.0 → sbt 0.13

Just add the below setting to the plugin definition to your project, paying attention to blank lines between settings:

```
// Potentially some other settings

addSbtPlugin("com.typesafe.sbt" % "sbt-koan" % "1.0.0")
```

Start sbt or – if it was already started – `reload` the current session. Then you should have the `koan` command available.


## Usage ##

The `koan` command takes exactly one of the following options:

- `next`: Move forward to the next koan
- `prev`: Move back to the previous koan
- `show`: Show the name of the current koan

Typically, after moving forward to the next koan, you want to execute the tests continuously and implement the solution, i.e. make the tests compile and succeed.


## Contribution policy ##

Contributions via GitHub pull requests are gladly accepted from their original author. Before we can accept pull requests, you will need to agree to the [Typesafe Contributor License Agreement](http://www.typesafe.com/contribute/cla) online, using your GitHub account - it takes 30 seconds.


## License ##

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
