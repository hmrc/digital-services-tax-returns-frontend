package models

import enumeratum._

sealed trait Activity extends EnumEntry
object Activity extends Enum[Activity] with PlayJsonEnum[Activity] {
  def values = findValues
  case object SocialMedia extends Activity
  case object SearchEngine extends Activity
  case object OnlineMarketplace extends Activity

  def toUrl(activity: Activity): String =
    activity.toString.replaceAll("(^[A-Z].*)([A-Z])", "$1-$2").toLowerCase
}
