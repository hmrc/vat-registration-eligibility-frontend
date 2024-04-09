import sbt.*
import play.sbt.PlayImport.*
import play.core.PlayVersion

object AppDependencies {

  private val scalaTestPlusPlayVersion = "5.1.0"
  private val mockitoVersion = "5.11.0"
  private val httpCachingClientVersion = "11.2.0"
  private val mongoPlayVersion = "1.8.0"
  private val playConditionalFormMappingVersion = "2.0.0"
  private val bootstrapVersion = "8.5.0"
  private val jsoupVersion = "1.17.2"
  private val scoverageVersion = "1.4.1"
  private val wireMockVersion = "2.27.2"
  private val playHmrcFrontendVersion = "8.5.0"
  private val playVersion = "30"

  val compile = Seq(
    ws,
    "uk.gov.hmrc.mongo" %% s"hmrc-mongo-play-$playVersion" % mongoPlayVersion,
    "uk.gov.hmrc" %% s"http-caching-client-play-$playVersion" % httpCachingClientVersion,
    "uk.gov.hmrc" %% s"play-conditional-form-mapping-play-$playVersion" % playConditionalFormMappingVersion,
    "uk.gov.hmrc" %% s"bootstrap-frontend-play-$playVersion" % bootstrapVersion,
    "uk.gov.hmrc" %% s"play-frontend-hmrc-play-$playVersion" % playHmrcFrontendVersion
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"            %% s"bootstrap-test-play-$playVersion"  % bootstrapVersion,
    "uk.gov.hmrc.mongo"      %% s"hmrc-mongo-test-play-$playVersion" % mongoPlayVersion,
    "org.scalatestplus.play" %% "scalatestplus-play"                 % scalaTestPlusPlayVersion,
    "org.jsoup"               % "jsoup"                              % jsoupVersion,
    "org.playframework"      %% "play-test"                          % PlayVersion.current,
    "org.mockito"             % "mockito-core"                       % mockitoVersion,
    "com.vladsch.flexmark"    % "flexmark-all"                       % "0.64.8",
    "org.scalatestplus"      %% "mockito-3-4"                        % "3.2.9.0",
    "org.scalatestplus"      %% "scalacheck-1-15"                    % "3.2.9.0",
  ).map(_ % Test)
}
