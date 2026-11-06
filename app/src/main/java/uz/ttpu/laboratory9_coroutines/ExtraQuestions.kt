package uz.ttpu.laboratory9_coroutines

import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

// Extra 1: Delay or sleep: measure it
fun extraQuestion1() = runBlocking {
    println("--- Extra 1 ---")
    val delayTime = measureTimeMillis {
        coroutineScope {
            launch { delay(300) }
            launch { delay(300) }
            launch { delay(300) }
        }
    }
    val roundedDelay = (delayTime + 50) / 100 * 100

    val sleepTime = measureTimeMillis {
        coroutineScope {
            launch { Thread.sleep(300) }
            launch { Thread.sleep(300) }
            launch { Thread.sleep(300) }
        }
    }
    val roundedSleep = (sleepTime + 50) / 100 * 100

    println("Delay time ~ $roundedDelay ms")
    println("Sleep time ~ $roundedSleep ms")
}

// Extra 2: Three levels of coroutines
fun extraQuestion2() = runBlocking {
    println("--- Extra 2 ---")
    coroutineScope {
        launch { // Child 1
            launch { // Grandchild
                delay(200)
                println("grandchild done")
            }
            println("child 1 done")
        }
        launch { // Child 2
            delay(100)
            println("child 2 done")
        }
    }
    println("scope done")
}

// Extra 3: No scope, no launch
// Erroneous code:
// fun plainMain() {
//     launch { // Compile Error: Unresolved reference: launch
//         println("Hello")
//     }
// }

fun extraQuestion3() = runBlocking {
    println("--- Extra 3 ---")
    launch {
        println("Hello from launch wrapped inside runBlocking")
    }
}

// Extra 4: What the compiler sees
data class User(val id: String, val name: String)

suspend fun fetchUser(id: String): User {
    delay(100)
    return User(id, "Alice")
}

fun extraQuestion4() = runBlocking {
    println("--- Extra 4 ---")
    val user = fetchUser("101")
    println("Fetched user: $user")
}

// Extra 5: Greet everybody
suspend fun greetEverybody(names: List<String>) = coroutineScope {
    names.forEachIndexed { index, name ->
        val i = index + 1
        launch {
            delay(i * 100L)
            println("Hello, $name")
        }
    }
}

fun extraQuestion5() = runBlocking {
    println("--- Extra 5 ---")
    println("before")
    greetEverybody(listOf("Ada", "Linus", "Grace"))
    println("after")
}
