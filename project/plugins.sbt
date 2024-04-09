resolvers += "HMRC-open-artefacts-maven" at "https://open.artefacts.tax.service.gov.uk/maven2"
resolvers += Resolver.url("HMRC-open-artefacts-ivy", url("https://open.artefacts.tax.service.gov.uk/ivy2"))(Resolver.ivyStylePatterns)

// To resolve a bug with version 2.x.x of the scoverage plugin - https://github.com/sbt/sbt/issues/6997
ThisBuild / libraryDependencySchemes ++= Seq("org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always)

addSbtPlugin("uk.gov.hmrc"        % "sbt-accessibility-linter" % "0.39.0")
addSbtPlugin("uk.gov.hmrc"       %% "sbt-auto-build"           % "3.21.0")
addSbtPlugin("uk.gov.hmrc"        % "sbt-distributables"       % "2.5.0")
addSbtPlugin("org.scalastyle"    %% "scalastyle-sbt-plugin"    % "1.0.0")
addSbtPlugin("org.scoverage"     %% "sbt-scoverage"            % "2.0.9")
addSbtPlugin("org.playframework"  % "sbt-plugin"               % "3.0.2")
addSbtPlugin("io.github.irundaia" % "sbt-sassify"              % "1.5.2")