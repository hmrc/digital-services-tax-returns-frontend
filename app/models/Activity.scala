/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package models

import enumeratum._

sealed trait Activity extends EnumEntry
object Activity extends Enum[Activity] with PlayJsonEnum[Activity] {
  def values: IndexedSeq[Activity] = findValues
  case object SocialMedia extends Activity
  case object SearchEngine extends Activity
  case object OnlineMarketplace extends Activity

  def convert(activities: Set[Activity]): Set[SelectActivities] =
    activities.map {
      case SocialMedia       => SelectActivities.SocialMedia
      case SearchEngine      => SelectActivities.SearchEngine
      case OnlineMarketplace => SelectActivities.OnlineMarketplace
    }

  def convertToActivity(activities: Set[SelectActivities]): Set[Activity] =
    activities.map {
      case SelectActivities.SocialMedia       => SocialMedia
      case SelectActivities.SearchEngine      => SearchEngine
      case SelectActivities.OnlineMarketplace => OnlineMarketplace
    }
}
