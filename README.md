# kmp-sse
SSE implementation for Kotlin Multiplatform

# Protocol: https://html.spec.whatwg.org/multipage/server-sent-events.html#sse-processing-model

# TODO

## functionality
* handle 301 / 307 -> redirect
* check last connected id
* client factory
* external connection flow that checks whether we are online before reconnecting
* targets for ktor provider
* check API + module names
 
## testing
* tests: client-core
* tests: for SseLineStream provider

## deployment
* remove debug trace
* documentation - write own provider
* documentation - examples 
* publish

