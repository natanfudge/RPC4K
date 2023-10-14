package io.github.natanfudge.rpc4k.runtime.api


/**
 * Generates a class that may be used to access an RPC server.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Api(val generateClient: Boolean = false)

///**
// * Generates a class that may act as an RPC server.
// */
//@Target(AnnotationTarget.CLASS)
//@Retention(AnnotationRetention.SOURCE)
//annotation class ApiServer

