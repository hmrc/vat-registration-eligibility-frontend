import sbt._

private object AppDependencies {

  import play.core.PlayVersion
  import play.sbt.PlayImport._

  private val scalaTestPlusPlayVersion = "3.1.3"
  private val pegdownVersion = "1.6.0"
  private val mockitoVersion = "3.3.3"
  private val httpCachingClientVersion = "9.4.0-play-26"
  private val playSimpleMongoVersion = "8.0.0-play-26"
  private val playConditionalFormMappingVersion = "1.6.0-play-26"
  private val playLanguageVersion = "4.12.0-play-26"
  private val bootstrapVersion = "4.2.0"
  private val scalacheckVersion = "1.14.1"
  private val jsoupVersion = "1.13.1"
  private val scoverageVersion = "1.3.1"
  private val wireMockVersion = "2.27.2"
  private val reactivemongoTestVersion = "5.0.0-play-26"

  private val playGovukFrontendVersion = "0.71.0-play-26"
  private val playHmrcFrontendVersion = "0.58.0-play-26"

  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "simple-reactivemongo" % playSimpleMongoVersion,
    "uk.gov.hmrc" %% "http-caching-client" % httpCachingClientVersion,
    "uk.gov.hmrc" %% "play-conditional-form-mapping" % playConditionalFormMappingVersion,
    "uk.gov.hmrc" %% "bootstrap-frontend-play-26" % bootstrapVersion,
    "uk.gov.hmrc" %% "play-language" % playLanguageVersion,
    "uk.gov.hmrc" %% "play-frontend-govuk" % playGovukFrontendVersion,
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
      "org.pegdown" % "pegdown" % pegdownVersion % scope,
      "org.jsoup" % "jsoup" % jsoupVersion % scope,
      "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
      "org.mockito" % "mockito-core" % mockitoVersion % scope,
      "org.scalacheck" %% "scalacheck" % scalacheckVersion % scope
    )

    def apply(): Seq[ModuleID] = test
  }

  private object IntegrationTestDependencies extends TestDependencies {
    override val scope: Configuration = IntegrationTest
    override val test: Seq[ModuleID] = Seq(
      "org.scalatestplus.play" %% "scalatestplus-play" % scalaTestPlusPlayVersion % scope,
      "org.pegdown" % "pegdown" % pegdownVersion % scope,
      "com.github.tomakehurst" % "wiremock-jre8" % wireMockVersion % scope,
      "org.jsoup" % "jsoup" % jsoupVersion % scope,
      "org.scoverage" % "scalac-scoverage-runtime_2.12" % scoverageVersion % scope,
      "uk.gov.hmrc" %% "reactivemongo-test" % reactivemongoTestVersion % scope
    )

    def apply(): Seq[ModuleID] = test
  }

  def apply() = compile ++ UnitTestDependencies() ++ IntegrationTestDependencies()
}
