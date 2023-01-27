# VAT Registration Eligibility Frontend

This microservice functions as part of the VAT registration journey to:

- determine if the user is able to register and route them to an alternative if they can't
- refine and capture the user's registration reason

## How to start the service locally

### From release

This service is started as part of the `VAT_REG_ALL` profile.

It can also be started individually using either `sm --start VAT_REG_EL_FE -r`, or if you're using sm2, `sm2 --start VAT_REG_EL_FE`.

Prior to starting the service locally, make sure the instance running in service manager is stopped. 
This can be done by running either `sm --stop VAT_REG_EL_FE`, or `sm2 --stop VAT_REG_FE` if using sm2

### From source code on your local machine

> The below instructions are for Mac/Linux only. Windows users will have to use SBT to run the service on port `9894`

1. Clone this repository into the development environment on your machine
2. Open a terminal and navigate to the folder you cloned the service into
3. Run `./run.sh`. This will start the service on port `9894` and enable the `/test-only` routes for local testing
4. Ensure all supporting services are started, using the `VAT_REG_ALL` service manager profile

## Starting a VAT registration journey

This service is best accessed as part of a full VAT registration journey because `vat-registration-frontend` needs to
initialize the journey before redirecting to this service.

### Individual journey

Individual journeys are not supported by VAT registration

### Organisation journey

1. In a browser, navigate to the Auth Login Stub on `http://localhost:9949/auth-login-stub/gg-sign-in`
2. Enter the following information:
    - Redirect URL: `http://localhost:9895/register-for-vat`
    - Affinity group: `Organisation`
3. Click `Submit` to start the journey.

### Agent journey

1. In a browser, navigate to the Auth Login Stub on `http://localhost:9949/auth-login-stub/gg-sign-in`
2. Enter the following information:
   - Redirect URL: `http://localhost:9895/register-for-vat`
   - Affinity group: `Agent`
   - Enrolments (at the bottom of the page):
     - Enrolment Key: `HMRC-AS-AGENT`
     - Identifier name: `AgentReferenceNumber`
     - Identifier value: enter anything
     - Status: `Activated`
3. Click `Submit` to start the journey.
4. During the journey, you will be asked if you want to register for yourself or someone else. Select `Somone else's`
   and providing you are logged in as `Agent` affinity group and have the `HMRC-AS-AGENT` enrolment, you will proceed
   with the agent journey. If you select `Your own` instead, the non-agent journey will apply.

## Tests
Prior to committing changes, please run tests and check on coverage report
- `unit tests` can be run with `sbt clean test`
- `integration tests` can be run with `sbt clean it:test`
- `accessiblity tests` can be run with `sbt a11y:test` (This will need node.js and npm installed)

Test coverage can be run using ```sbt clean coverage test it:test scalastyle coverageReport```.

## Test only routes
The only test routes defined by the service are related to feature switches. These can be accessed by navigating to
`http://localhost:9894/check-if-you-can-register-for-vat/test-only/api/feature-switches`.

The 2 endpoints to support retrieval/setting of feature-switches are

`GET        /api/feature-switches`
Used to retrieve feature-switches config data for this service.

`POST       /api/feature-switches`
Used to post any updates to feature-switches config.

### Set feature switches

As this service doesn't have a frontend page to showcase the feature-switches config, it is recommended that you use 
the feature switch page on `vat-registration-frontend` for convenience as it has the ability to update feature switches 
for the 3 core VAT Registration services.

This can be found by navigating to `http://localhost:9895/register-for-vat/test-only/feature-switches`.

> Note: These pages set feature switches using system properties, which are local to the specific JVM the service is running on.
> If you are testing in an environment that provisions multiple instances, you will need to either submit the feature switch page
> several times to ensure all instances are hit, or, alternatively, set the feature switches in config as JVM arguments and 
> re-deploy the service.