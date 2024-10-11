
# digital-services-tax-returns-frontend
![](https://img.shields.io/github/v/release/hmrc/digital-services-tax-returns-frontend)

## About
The Digital Services Tax (DST) digital service is split into a number of different microservices all serving specific functions which are listed below:

**Frontend** - The main frontend for the service which includes the pages for returns and an account home page.

**Backend** - The service that the frontend uses to call HOD APIs to retrieve and send information relating to business information and subscribing to regime.

**Stub** - Microservice that is used to mimic the DES APIs when running services locally or in the development and staging environments.

This is the main frontend, currently containing the returns form.

For details about the digital services tax see [the GOV.UK guidance](https://www.gov.uk/government/consultations/digital-services-tax-draft-guidance)

## Running the service
### Service manager
The whole service can be started with:

`sm2 --start DST_ALL`

or specifically for only the frontend

`sm2 --start DST_RETURNS_FRONTEND`

### Locally

`sbt 'run 8745'`

* Visit http://localhost:9949/auth-login-stub/gg-sign-in
* You may need to add some user details to the form
#### DST Returns Journey
    * Affinity Group: Organisation
    * Group identifier: 12345
    * Enrolment Key: HMRC-DST-ORG
    * Identifier Name: DSTRefNumber
    * Identifier Value: AMDST0799721562
* Then enter a redirect url: http://localhost:8743/digital-services-tax-returns
* Press **Submit**.

## Running the tests

    sbt test it/test

## Running scalafmt

To apply scalafmt formatting using the rules configured in the .scalafmt.conf, run:

`sbt scalafmtAll`

To check the files have been formatted correctly, run:

`sbt scalafmtCheckAll`

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")
 