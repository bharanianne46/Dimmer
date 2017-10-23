import groovy.json.JsonBuilder
import io.netty.handler.codec.http.HttpHeaderNames
import ratpack.exec.Promise
import ratpack.func.Action
import ratpack.http.client.HttpClient
import ratpack.http.client.ReceivedResponse
import ratpack.http.client.RequestSpec
import ratpack.jackson.Jackson

import java.nio.charset.Charset
import java.time.Duration

import static ratpack.groovy.Groovy.ratpack

String url = "https://graph-na04-useast2.api.smartthings.com/api/smartapps/installations/497e9a33-93d6-4279-abe8-c0fe0ccf9d02"

ratpack {
    handlers {
        get {
            render "Welcome to SmartApps dimmer App"
        }

        get("dimmers") {
            def authToken = request.headers.get('Authorization').replace("Bearer ", "").trim()
            HttpClient httpClient = get(HttpClient.class);            //get httpClient
            URI uri = URI.create("${url}/dimmers")
            executeGet(httpClient, uri, authToken).then { response ->
                render(response.getBody().getText())  //Render the response from the httpClient GET request
            }
        }
// Set All Dimmers value
        post("setLevel") {
            def authToken = request.headers.get('Authorization').replace("Bearer ", "").trim()
            parse(Jackson.fromJson(Map)).then { data ->
                def level = data.level?.toInteger()
                if (level > 0 && level <= 100) {
                    HttpClient httpClient = context.get(HttpClient.class);            //get httpClient
                    URI uri = URI.create("${url}/setLevel")
                    executePost(httpClient, uri, new JsonBuilder([level: level]).toString(), authToken).then { response ->
                        if (response.status.is2xx()) {
                            context.render "Successfully Updated"
                        } else
                            context.render(response.getBody().getText())  //Render the response from the httpClient GET request
                    }
                } else {
                    context.render "Invalid Level"
                }
            }

        }

        path('dimmers/:dimmerId') { ->
            byMethod {
                get {
                    HttpClient httpClient = get(HttpClient.class);            //get httpClient
                    URI uri = URI.create("${url}/dimmers/${pathTokens.dimmerId}")
                    def authToken = request.headers.get('Authorization').replace("Bearer ", "").trim()
                    executeGet(httpClient, uri, authToken).then { response ->
                        render(response.getBody().getText())  //Render the response from the httpClient GET request
                    }
                }
                // Set Individual Dimmer value
                post {
                    def level = request.queryParams.level?.toInteger()
                    if (level > 0 && level <= 100) {
                        HttpClient httpClient = get(HttpClient.class);            //get httpClient
                        def authToken = request.headers.get('Authorization').replace("Bearer ", "").trim()
                        URI uri = URI.create("${url}/dimmers/${pathTokens.dimmerId}/level")
                        executePost(httpClient, uri, new JsonBuilder([level: level]).toString(), authToken).then { response ->
                            if (response.status.is2xx()) {
                                render "Success"
                            } else
                                render(response.getBody().getText())  //Render the response from the httpClient GET request
                        }
                    } else {
                        render "Invalid Level"
                    }
                }
            }
        }


    }
}

private Promise<ReceivedResponse> executeGet(HttpClient httpClient, URI uri, String authToken) {
    httpClient.get(uri, { RequestSpec request ->
        request.headers.set(HttpHeaderNames.CONTENT_TYPE, 'application/json')
        request.headers.set("Authorization", "Bearer " + authToken)
        request.headers.set("X-Platform-Api-Token", authToken)
        request.connectTimeout(Duration.ofSeconds(30))
    } as Action)
}

private Promise<ReceivedResponse> executePost(HttpClient httpClient, URI uri, String body, String authToken) {
    httpClient.post(uri, { RequestSpec request ->
        request.headers.set(HttpHeaderNames.CONTENT_TYPE, 'application/json')
        request.headers.set("Authorization", "Bearer " + authToken)
        request.headers.set("X-Platform-Api-Token", authToken)
        request.body.text(body, Charset.forName('UTF-8'))
        request.connectTimeout(Duration.ofSeconds(30))
    } as Action)
}
