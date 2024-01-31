package services

import connectors.DSTConnector
import models.returns.Return
import models.{PeriodKey, UserAnswers}
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.http.HeaderCarrier

import java.time.Instant
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PreviousReturnsService @Inject()(dstConnector: DSTConnector, userAnswers: UserAnswers)(implicit ec: ExecutionContext, hc: HeaderCarrier){

  def retrieveAndStoreReturnsData(periodKey: PeriodKey): Future[Option[UserAnswers]] = {
    dstConnector.lookupSubmittedReturns(periodKey)(hc).flatMap {
      case Some(returnData) =>
        val ua = convertReturnToUserAnswers(returnData)
        Some(ua)
      case None => Future.successful(None)
    }
  }

  private def convertReturnToUserAnswers(returnData: Return, userId: String): UserAnswers = {
//    val returnJson: JsObject = Json.obj(
//      "reportedActivities" -> returnData.reportedActivities,
//      "alternateCharge" -> returnData.alternateCharge,
//      "crossBorderReliefAmount" -> returnData.crossBorderReliefAmount,
//      ""
//
//    )
  }

}
