# Kotlin Multiplatform SSE  

Extensible Kotlin Multiplatform implementation of Server Sent Events(Sse)

# Overview


# Documentation
## Setup:

```kotlin
// build.gradle.kts

//... 
dependencies {
    // stream provider, the SSE event source is a transitive dependency
    implementation("cc.scrambledbytes.sse:ktor-stream-provider:1.0.0")
    // client engine
    implementation("io.ktor:ktor-client-apache5:2.3.0")
}

```

## Basic Usage
```kotlin
// in a suspend function

val http = HttpClient()
val provider: SseLineStream.Provider = KtorSseEventStreamProvider(http)

val client = SseEventSource(
    url = "https://0.0.0.0:8080/sse",
    streamProvider = provider,
)

client.open()

client.events.collect {
    println("[Client] Got event: $it")
}
```

## Supported SseLineStream providers
* Ktor  

## Writing your own SseLineStream provider
You can easily write your own stream provider by implementing the `SseLineStream.Provider` interface.

An `SseLineStream.Provider` needs to create a `SseLineStream` which needs two lambdas: 
* `onClose`: which is executed when the connection is closed by the source
* `onConnect`: which is executed when a connection to the SSE source is being attempted. It passes two callback handles, 
 `onConnect`  and `onLine`. `onConnect` needs to be called exactly once after the connection has been initialized, and 
 `onLine` needs to be called everytime a line has arrived in the text stream. 

In addition to that, a `SseLineStream` provider should set the following headers/attributes for each connection attempt:
* [CacheControl](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Cache-Control): no-store
* [Accept](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Accept): text/event-stream
* [intitator type](https://fetch.spec.whatwg.org/#concept-request-initiator-type): other 
* (optional, if true) [withCredentials](https://fetch.spec.whatwg.org/#concept-request-credentials-mode): include 
* (optional, if not null) [Last-Event-ID](https://html.spec.whatwg.org/multipage/server-sent-events.html#the-last-event-id-header) : lastEventId

Additional requirements:
* received content lines should be decoded as UTF-8 strings
* The provider may decide when a request is failed in the  `onConnect` callback

# License
Apache 2.0

# Resources

* [SSE Specification](https://html.spec.whatwg.org/multipage/server-sent-events.html#sse-processing-model)


# Todo
* fix kmp targets
* github actions
* more tests
* static code analysis
* add providers
* documentation
