import groovy.json.JsonBuilder
import ratpack.func.Action as RatpackAction
import ratpack.groovy.test.GroovyRatpackMainApplicationUnderTest
import ratpack.http.Status
import ratpack.http.client.ReceivedResponse
import ratpack.http.client.RequestSpec
import ratpack.test.http.TestHttpClient
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class DimmerSpec extends Specification {
    @AutoCleanup
    @Shared
    GroovyRatpackMainApplicationUnderTest groovyScriptApplicationunderTest = new GroovyRatpackMainApplicationUnderTest()

    @Delegate
    TestHttpClient testClient = groovyScriptApplicationunderTest.httpClient

    @Unroll
    def 'test dimmers list'() {
        when:
        requestSpec({ RequestSpec requestSpec ->
            requestSpec.body.type('application/json')
            requestSpec.headers.set("AUTHORIZATION", "Bearer 74ca99d3-8f2d-4925-90d0-ea16c87ea41c")
        } as RatpackAction)
        ReceivedResponse response = get("/dimmers")
        then:
        response.status == Status.OK
        response.body.text != ""
    }

    def "test home page"() {
        when:
        def responseText = getText()

        then:
        responseText == "Welcome to SmartApps dimmer App"
    }

    def "test setValue"() {
        when:
        requestSpec({ RequestSpec requestSpec ->
            requestSpec.body.type('application/json')
            requestSpec.headers.set("AUTHORIZATION", "Bearer 74ca99d3-8f2d-4925-90d0-ea16c87ea41c")
            requestSpec.body.text(new JsonBuilder([level: 200]).toString())
        } as RatpackAction)
        ReceivedResponse response = post("/setLevel")

        then:
        response.status == Status.OK
        response.body.text == "Invalid Level"
    }

}
