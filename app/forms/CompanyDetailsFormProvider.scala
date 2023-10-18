/*
 * Copyright 2023 HM Revenue & Customs
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

package forms

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form
import play.api.data.Forms._
import models.CompanyDetails

class CompanyDetailsFormProvider @Inject() extends Mappings {

  val companyNameMaxLength       = 105
  val companyNameRegex           = """^[a-zA-Z0-9 '&.-]{1,105}$"""
  val uniqueTaxReferenceMaxRegex = "^[0-9]{10}$"

  def apply(): Form[CompanyDetails] = Form(
    mapping(
      "companyName"             -> text("companyDetails.error.companyName.required")
        .verifying(
          firstError(
            maxLength(companyNameMaxLength, "companyDetails.error.companyName.length"),
            regexp(companyNameRegex, "companyDetails.error.companyName.invalid")
          )
        ),
      "uniqueTaxpayerReference" -> optional(
        text("companyDetails.error.uniqueTaxpayerReference.required")
          .verifying(regexp(uniqueTaxReferenceMaxRegex, "companyDetails.error.uniqueTaxpayerReference.invalid"))
      )
    )(CompanyDetails.apply)(CompanyDetails.unapply)
  )
}
