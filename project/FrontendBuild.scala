import sbt._

object FrontendBuild extends Build with MicroService {

  val appName = "vat-registration-eligibility-frontend"

  override lazy val appDependencies: Seq[ModuleID] = AppDependencies()
}

private object AppDependencies {
  import play.core.PlayVersion
  import play.sbt.PlayImport._

  private val playHealthVersion = "2.1.0"
  private val logbackJsonLoggerVersion = "3.1.0"
  private val govukTemplateVersion = "5.3.0"
  private val playUiVersion = "7.4.0"
  private val hmrcTestVersion = "3.0.0"
  private val scalaTestVersion = "3.0.4"
  private val scalaTestPlusPlayVersion = "2.0.1"
  private val pegdownVersion = "1.6.0"
  private val mockitoAllVersion = "1.10.19"
  private val httpCachingClientVersion = "7.0.0"
  private val playReactivemongoVersion = "6.0.0"
  private val playConditionalFormMappingVersion = "0.2.0"
  private val playLanguageVersion = "3.4.0"
  private val bootstrapVersion = "1.4.0"
  private val scalacheckVersion = "1.13.4"
  private val jsoupVersion = "1.11.2"
  private val scoverageVersion = "1.3.1"
  private val wireMockVersion = "2.6.0"
  private val reactivemongoTestVersion = "3.1.0"

  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "play-reactivemongo" % playReactivemongoVersion,
    "uk.gov.hmrc" %% "logback-json-logger" % logbackJsonLoggerVersion,
    "uk.gov.hmrc" %% "govuk-template" % govukTemplateVersion,
    "uk.gov.hmrc" %% "play-health" % playHealthVersion,
    "uk.gov.hmrc" %% "play-ui" % playUiVersion,
    "uk.gov.hmrc" %% "http-caching-client" % httpCachingClientVersion,
    "uk.gov.hmrc" %% "play-conditional-form-mapping" % playConditionalFormMappingVersion,
    "uk.gov.hmrc" %% "bootstrap-play-25" % bootstrapVersion,
    "uk.gov.hmrc" %% "play-language" % playLanguageVersion
  )

  trait TestDependencies {
    val scope: Configuration
    val test : Seq[ModuleID]
  }

  private object UnitTestDependencies extends TestDependencies {
    override val scope: Configuration = Test
    override val test: Seq[ModuleID] = Seq(
      "uk.gov.hmrc" %% "hmrctest" % hmrcTestVersion % scope,
      "org.scalatest" %% "scalatest" % scalaTestVersion % scope,
      "org.scalatestplus.play" %% "scalatestplus-play" % scalaTestPlusPlayVersion % scope,
      "org.pegdown" % "pegdown" % pegdownVersion % scope,
      "org.jsoup" % "jsoup" % jsoupVersion % scope,
      "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
      "org.mockito" % "mockito-all" % mockitoAllVersion % scope,
      "org.scalacheck" %% "scalacheck" % scalacheckVersion % scope
    )

    def apply(): Seq[ModuleID] = test
  }

  private object IntegrationTestDependencies extends TestDependencies {
    override val scope: Configuration = IntegrationTest
    override val test: Seq[ModuleID] = Seq(
      "uk.gov.hmrc"             %% "hmrctest"                      % hmrcTestVersion          % scope,
      "org.scalatestplus.play"  %% "scalatestplus-play"            % scalaTestPlusPlayVersion % scope,
      "com.github.tomakehurst"  %  "wiremock"                      % wireMockVersion          % scope,
      "org.jsoup"               %  "jsoup"                         % jsoupVersion             % scope,
      "org.scoverage"           %  "scalac-scoverage-runtime_2.11" % scoverageVersion         % scope,
      "uk.gov.hmrc"             %% "reactivemongo-test"            % reactivemongoTestVersion % scope
    )

    def apply(): Seq[ModuleID] = test
  }

  def apply() = compile ++ UnitTestDependencies() ++ IntegrationTestDependencies()
}
