package uz.ttpu.laboratory9_coroutines

import kotlinx.coroutines.*

// Question 1: First coroutine
fun question1() = runBlocking {
    println("--- Question 1 ---")
    launch {
        delay(1000)
        println("Coffee ready")
    }
    println("Order accepted")
}

// Question 2: Three baristas
fun question2WithDelay() = runBlocking {
    println("--- Question 2 (with delay) ---")
    launch { println("Espresso start"); delay(300); println("Espresso end") }
    launch { println("Tea start"); delay(100); println("Tea end") }
    launch { println("Latte start"); delay(200); println("Latte end") }
}

fun question2WithSleep() = runBlocking {
    println("--- Question 2 (with Thread.sleep) ---")
    launch { println("Espresso start"); Thread.sleep(300); println("Espresso end") }
    launch { println("Tea start"); Thread.sleep(100); println("Tea end") }
    launch { println("Latte start"); Thread.sleep(200); println("Latte end") }
}

// Question 3: Chain of suspend functions
suspend fun step1LoadUser() {
    delay(100)
    println("Step 1: Load User")
    step2CheckPassword()
}

suspend fun step2CheckPassword() {
    delay(100)
    println("Step 2: Check Password")
    step3OpenProfile()
}

suspend fun step3OpenProfile() {
    delay(100)
    println("Step 3: Open Profile")
}

suspend fun question3Main() {
    println("--- Question 3 ---")
    step1LoadUser()
}

// Question 4: Calling suspend from normal code
suspend fun loadUserFromNetwork(): String {
    delay(100)
    return "User data"
}

// Erroneous example for documentation:
// fun normalFunctionErroneous() {
//     val user = loadUserFromNetwork() // Compile Error: Suspend function should be called only from a coroutine or another suspend function
// }

fun question4Fix1RunBlocking() {
    println("--- Question 4 (Fix 1: runBlocking) ---")
    val user = runBlocking { loadUserFromNetwork() }
    println("Loaded: $user")
}

fun question4Fix2CoroutineScope(scope: CoroutineScope) {
    println("--- Question 4 (Fix 2: launch on CoroutineScope) ---")
    scope.launch {
        val user = loadUserFromNetwork()
        println("Loaded: $user")
    }
}

// Question 5: Suspend is not a thread
suspend fun getCurrentThreadName(): String {
    return Thread.currentThread().name
}

fun question5() = runBlocking {
    println("--- Question 5 ---")
    println("Thread before call: ${Thread.currentThread().name}")
    val returnedThread = getCurrentThreadName()
    println("Thread returned in suspend: $returnedThread")
}
