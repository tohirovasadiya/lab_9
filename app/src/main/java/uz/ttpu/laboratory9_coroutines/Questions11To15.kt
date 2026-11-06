package uz.ttpu.laboratory9_coroutines

import kotlinx.coroutines.*

// Question 11: Reusable counter
fun CoroutineScope.startCounter(name: String, max: Int): Job {
    return launch {
        for (i in 1..max) {
            delay(100)
            println("$name: $i")
        }
    }
}

fun question11() = runBlocking {
    println("--- Question 11 ---")
    coroutineScope {
        startCounter("Counter A", 3)
        startCounter("Counter B", 2)
        println("launched")
    }
}

// Question 12: Returns at once or waits
fun CoroutineScope.startGreeterBackground(names: List<String>): Job = launch {
    for (name in names) {
        delay(100)
        println("Hello $name (bg)")
    }
}

suspend fun greetAllSequential(names: List<String>) = coroutineScope {
    for (name in names) {
        delay(100)
        println("Hello $name (sync)")
    }
}

fun question12() = runBlocking {
    println("--- Question 12 ---")
    println("Calling Helper 1 (Extension on CoroutineScope)...")
    startGreeterBackground(listOf("Alice", "Bob"))
    println("Returned immediately from Helper 1!")

    delay(300)

    println("\nCalling Helper 2 (Suspend function with coroutineScope)...")
    greetAllSequential(listOf("Charlie", "Dave"))
    println("Returned after completion from Helper 2!")
}

// Question 13: Five builders
fun question13() = runBlocking {
    println("--- Question 13 ---")
    val job: Job = launch { delay(50) }
    val deferred: Deferred<Int> = async { 42 }
    val scopeResult: String = coroutineScope { "Scope complete" }
    println("launch returned: $job")
    println("async returned: $deferred")
    println("coroutineScope returned: $scopeResult")
    println("async await result: ${deferred.await()}")
}

// Question 14: Job or Deferred
fun question14() = runBlocking {
    println("--- Question 14 ---")
    val job: Job = launch {
        val calculated = 6 * 7
        println("Calculated inside launch: $calculated")
        // Result cannot be directly returned or extracted from launch
    }
    println("Launch version returns Job: $job")

    val deferred: Deferred<Int> = async {
        6 * 7
    }
    val value: Int = deferred.await()
    println("Async version returns Deferred and yields value via await(): $value")
}

// Question 15: Three exports in a scope
fun question15() = runBlocking {
    println("--- Question 15 ---")
    println("Start")
    coroutineScope {
        launch {
            delay(300)
            println("X")
        }
        launch {
            delay(100)
            println("Y")
        }
        launch {
            delay(200)
            println("Z")
        }
    }
    println("All done")
}
