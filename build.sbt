import org.scalastyle.sbt.ScalastylePlugin

import scalariform.formatter.preferences._

name := "flights-aggregator"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= Dependencies.apiCore

scalacOptions := Seq("-target:jvm-1.8", "-unchecked", "-deprecation", "-feature", "-encoding", "utf8", "-language:implicitConversions", "-language:postfixOps")

configs(IntegrationTest)

scalariformSettingsWithIt
ScalariformKeys.preferences := ScalariformKeys.preferences.value
  .setPreference(AlignSingleLineCaseStatements, true)
  .setPreference(DoubleIndentClassDeclaration, true)
  .setPreference(SpacesAroundMultiImports, false)
  .setPreference(AlignArguments, true)
  .setPreference(SpacesWithinPatternBinders, true)

scapegoatVersion := "1.3.0"

Revolver.settings
Revolver.enableDebugging(port = 5005, suspend = false)

ScalastylePlugin.projectSettings ++
  Seq(ScalastylePlugin.scalastyleConfig := file("project/scalastyle-config.xml"), ScalastylePlugin.scalastyleFailOnError := true)

Seq(Defaults.itSettings: _*)

// ALIASES
addCommandAlias("compileAll", ";compile;test:compile;it:compile")

cancelable in Global := true
