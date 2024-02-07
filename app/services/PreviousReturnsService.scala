package services

import connectors.DSTConnector
import jdk.jfr.Percentage
import models.Activity.{OnlineMarketplace, SearchEngine, SocialMedia}
import models.returns.Return
import models.{Activity, PeriodKey, UserAnswers}
import pages.{CrossBorderTransactionReliefPage, GroupLiabilityPage, ReportOnlineMarketplaceLossPage, ReportOnlineMarketplaceOperatingMarginPage, ReportSearchEngineOperatingMarginPage, ReportSocialMediaOperatingMarginPage, SearchEngineLossPage, SelectActivitiesPage, SocialMediaLossPage}
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.http.HeaderCarrier

import java.time.Instant
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

@Singleton
class PreviousReturnsService @Inject()(dstConnector: DSTConnector, userAnswers: UserAnswers)(implicit ec: ExecutionContext, hc: HeaderCarrier){

  def convertReturnToUserAnswers(periodKey: PeriodKey, userAnswers: UserAnswers): Future[Option[Try[UserAnswers]]] = {
    dstConnector.lookupSubmittedReturns(periodKey)(hc).map {
      case Some(returnData) =>
        val updatedUserAnswers = userAnswers.set(SelectActivitiesPage(periodKey), Activity.convert(returnData.reportedActivities))
          .flatMap(_.set(GroupLiabilityPage(periodKey), BigDecimal(returnData.totalLiability.toDouble)))
          .flatMap(_.set(CrossBorderTransactionReliefPage(periodKey), BigDecimal(returnData.crossBorderReliefAmount.toDouble)))
//          .flatMap(alternateChargeMap(periodKey, _, returnData))
        Some(updatedUserAnswers)
      case None => None
    }
  }

  def alternateChargeMap(periodKey: PeriodKey, userAnswers: UserAnswers, returnData: Return) = {
    returnData.alternateCharge map{
      case (activity, percentage) =>
        activity match {
          case SocialMedia => if (percentage == 0) {
            userAnswers.set(SocialMediaLossPage(periodKey),true)
          } else {
            userAnswers.set(ReportSocialMediaOperatingMarginPage(periodKey), percentage)
          }
          case SearchEngine => if (percentage == 0) {
            userAnswers.set(SearchEngineLossPage(periodKey), true)
          } else {
            userAnswers.set(ReportSearchEngineOperatingMarginPage(periodKey), percentage)
          }
          case OnlineMarketplace => if (percentage == 0) {
            userAnswers.set(ReportOnlineMarketplaceLossPage(periodKey), true)
          } else {
            userAnswers.set(ReportOnlineMarketplaceOperatingMarginPage(periodKey), percentage)
          }
        }
    }
  }

}
