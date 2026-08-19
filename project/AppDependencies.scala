import sbt.*

object AppDependencies {

  private val bootstrapVersion = "10.7.0"
  private val hmrcMongoVersion = "2.13.0"

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"       %% "play-frontend-hmrc-play-30"            % "13.11.0",
    "uk.gov.hmrc"       %% "play-conditional-form-mapping-play-30" % "3.5.0",
    "uk.gov.hmrc"       %% "bootstrap-frontend-play-30"            % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"                    % hmrcMongoVersion,
    "org.typelevel"     %% "cats-core"                             % "2.13.0",
    "fr.marcwrobel"      % "jbanking"                              % "4.3.0",
    "com.beachape"      %% "enumeratum"                            % "1.9.8",
    "com.beachape"      %% "enumeratum-play-json"                  % "1.9.8",
    "uk.gov.hmrc"       %% "crypto-json-play-30"                   % "8.4.0"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"            %% "bootstrap-test-play-30"  % bootstrapVersion,
    "uk.gov.hmrc.mongo"      %% "hmrc-mongo-test-play-30" % hmrcMongoVersion,
    "org.scalacheck"         %% "scalacheck"              % "1.19.0",
    "org.scalatestplus.play" %% "scalatestplus-play"      % "7.0.2",
    "org.scalatestplus"      %% "scalacheck-1-15"         % "3.2.11.0",
    "org.scalatestplus"      %% "mockito-3-12"            % "3.2.10.0",
    "io.github.wolfendale"   %% "scalacheck-gen-regexp"   % "1.1.0",
    "org.jsoup"               % "jsoup"                   % "1.23.1",
    "com.beachape"           %% "enumeratum-scalacheck"   % "1.9.8",
    "org.mockito"             % "mockito-core"            % "5.23.0",
    "io.chrisdavenport"      %% "cats-scalacheck"         % "0.3.2"
  ).map(_ % "test")

  def apply(): Seq[ModuleID] = compile ++ test
}
