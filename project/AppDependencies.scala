import sbt._

private object AppDependencies {

  import play.core.PlayVersion
  import play.sbt.PlayImport._

  private val scalaTestPlusPlayVersion = "5.1.0"
  private val mockitoVersion = "4.8.1"
  private val httpCachingClientVersion = "10.0.0-play-28"
  private val mongoPlayVersion = "0.74.0"
  private val playConditionalFormMappingVersion = "1.12.0-play-28"
  private val bootstrapVersion = "7.12.0"
  private val jsoupVersion = "1.15.3"
  private val scoverageVersion = "1.4.1"
  private val wireMockVersion = "2.27.2"
  private val playHmrcFrontendVersion = "3.34.0-play-28"

  val compile = Seq(
    ws,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-28" % mongoPlayVersion,
    "uk.gov.hmrc" %% "http-caching-client" % httpCachingClientVersion,
    "uk.gov.hmrc" %% "play-conditional-form-mapping" % playConditionalFormMappingVersion,
    "uk.gov.hmrc" %% "bootstrap-frontend-play-28" % bootstrapVersion,
    "uk.gov.hmrc" %% "play-frontend-hmrc" % playHmrcFrontendVersion
  )

  trait TestDependencies {
    val scope: Configuration
    val test: Seq[ModuleID]
  }

  private object UnitTestDependencies extends TestDependencies {
    override val scope: Configuration = Test
    override val test: Seq[ModuleID] = Seq(
      "org.scalatestplus.play" %% "scalatestplus-play" % scalaTestPlusPlayVersion % scope,
      "org.jsoup" % "jsoup" % jsoupVersion % scope,
      "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
      "org.mockito" % "mockito-core" % mockitoVersion % scope,
      "com.vladsch.flexmark" % "flexmark-all" % "0.36.8" % scope,
      "org.scalatestplus" %% "mockito-3-4" % "3.2.9.0" % "test",
      "org.scalatestplus" %% "scalacheck-1-15" % "3.2.9.0" % "test"
    )

    def apply(): Seq[ModuleID] = test
  }

  private object IntegrationTestDependencies extends TestDependencies {
    override val scope: Configuration = IntegrationTest
    override val test: Seq[ModuleID] = Seq(
      "org.scalatestplus.play" %% "scalatestplus-play" % scalaTestPlusPlayVersion % scope,
      "com.github.tomakehurst" % "wiremock-jre8" % wireMockVersion % scope,
      "org.jsoup" % "jsoup" % jsoupVersion % scope,
      "org.scoverage" % "scalac-scoverage-runtime_2.12" % scoverageVersion % scope,
      "uk.gov.hmrc.mongo" %% "hmrc-mongo-test-play-28" % mongoPlayVersion % scope,
      "com.vladsch.flexmark" % "flexmark-all" % "0.36.8" % scope
    )

    def apply(): Seq[ModuleID] = test
  }

  def apply(): Seq[ModuleID] = compile ++ UnitTestDependencies() ++ IntegrationTestDependencies()
}
