import sbt._

object AppDependencies {

  private val bootstrapVersion = "8.2.0"
  private val hmrcMongoVersion = "1.6.0"

  val compile = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"       %% "play-frontend-hmrc-play-30"            % "8.2.0",
    "uk.gov.hmrc"       %% "play-conditional-form-mapping-play-30" % "2.0.0",
    "uk.gov.hmrc"       %% "bootstrap-frontend-play-30"            % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"                    % hmrcMongoVersion,
    "com.chuusai"       %% "shapeless"                             % "2.4.0-M1",
    "commons-validator"  % "commons-validator"                     % "1.7",
    "org.typelevel"     %% "cats-core"                             % "2.10.0",
    "fr.marcwrobel"      % "jbanking"                              % "4.1.0",
    "com.beachape"              %% "enumeratum"                    % "1.7.0",
    "com.beachape"              %% "enumeratum-play-json"          % "1.7.0"
  )

  val test = Seq(
    "uk.gov.hmrc"            %% "bootstrap-test-play-30"  % bootstrapVersion,
    "uk.gov.hmrc.mongo"      %% "hmrc-mongo-test-play-30" % hmrcMongoVersion,
    "org.scalacheck"         %% "scalacheck"              % "1.17.0",
    "org.scalatestplus.play" %% "scalatestplus-play"      % "5.1.0",
    "org.scalatestplus"      %% "scalacheck-1-15"         % "3.2.11.0",
    "org.scalatestplus"      %% "mockito-3-12"            % "3.2.10.0",
    "wolfendale"             %% "scalacheck-gen-regexp"   % "0.1.2",
    "org.mockito"            %% "mockito-scala"           % "1.17.12",
    "org.jsoup"               % "jsoup"                   % "1.15.4",
    "com.beachape"           %% "enumeratum-scalacheck"   % "1.7.2",
    "org.mockito"             % "mockito-core"            % "5.2.0",
    "io.chrisdavenport"      %% "cats-scalacheck"         % "0.3.2"
  ).map(_ % "test")

  def apply(): Seq[ModuleID] = compile ++ test
}
