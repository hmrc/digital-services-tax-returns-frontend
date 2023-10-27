import sbt._

object AppDependencies {

  private val bootstrapVersion = "7.22.0"
  private val hmrcMongoVersion = "1.3.0"

  val compile = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"       %% "play-frontend-hmrc"            % "7.9.0-play-28",
    "uk.gov.hmrc"       %% "play-conditional-form-mapping" % "1.13.0-play-28",
    "uk.gov.hmrc"       %% "bootstrap-frontend-play-28"    % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-28"            % hmrcMongoVersion,
    "com.chuusai"       %% "shapeless"                     % "2.4.0-M1",
    "commons-validator"  % "commons-validator"             % "1.7",
    "org.typelevel"     %% "cats-core"                     % "2.10.0",
    "fr.marcwrobel"      % "jbanking"                      % "4.0.0"
  )

  val test = Seq(
    "uk.gov.hmrc"            %% "bootstrap-test-play-28"        % bootstrapVersion,
    "uk.gov.hmrc.mongo"      %% "hmrc-mongo-test-play-28"       % hmrcMongoVersion,
    "org.scalacheck"         %% "scalacheck"                    % "1.17.0",
    "org.scalatestplus.play" %% "scalatestplus-play"            % "5.1.0",
    "org.scalatest"          %% "scalatest"                     % "3.2.15",
    "org.scalatestplus"      %% "scalacheck-1-15"               % "3.2.11.0",
    "org.scalatestplus"      %% "mockito-3-12"                  % "3.2.10.0",
    "wolfendale"             %% "scalacheck-gen-regexp"         % "0.1.2",
    "org.mockito"            %% "mockito-scala"                 % "1.17.12",
    "org.pegdown"             % "pegdown"                       % "1.6.0",
    "org.jsoup"               % "jsoup"                         % "1.15.4",
    "com.vladsch.flexmark"    % "flexmark-all"                  % "0.64.6",
    "com.beachape"           %% "enumeratum-scalacheck"         % "1.7.2",
    "org.mockito"             % "mockito-core"                  % "5.2.0",
    "io.chrisdavenport"      %% "cats-scalacheck"               % "0.3.2",
    "uk.gov.hmrc"            %% "play-frontend-hmrc"            % "7.9.0-play-28",
    "uk.gov.hmrc"            %% "play-conditional-form-mapping" % "1.13.0-play-28",
    "uk.gov.hmrc"            %% "bootstrap-frontend-play-28"    % bootstrapVersion,
    "uk.gov.hmrc.mongo"      %% "hmrc-mongo-play-28"            % hmrcMongoVersion
  ).map(_ % "test, it")

  def apply(): Seq[ModuleID] = compile ++ test
}
