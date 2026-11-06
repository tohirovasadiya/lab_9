package uz.ttpu.laboratory9_coroutines

import kotlinx.coroutines.*

// Question 6: Ten downloads, one thread
fun question6() = runBlocking {
    println("--- Question 6 ---")
    List(10) { i ->
        launch {
            println("download $i started on ${Thread.currentThread().name}")
            delay(500)
            println("download $i finished on ${Thread.currentThread().name}")
        }
    }
}

// Question 7: Order of launch
fun question7() = runBlocking {
    println("--- Question 7 ---")
    println("1")
    launch {
        println("2")
        delay(100)
        println("3")
    }
    println("4")
    delay(50)
    println("5")
}

// Question 8: Parent waits for child
fun question8() = runBlocking {
    println("--- Question 8 ---")
    val parent = launch {
        launch {
            delay(200)
            println("child done")
        }
        println("parent body done")
    }
    parent.join()
    println("report completed")
}

// Question 9: Screen closed, requests cancelled
fun question9() = runBlocking {
    println("--- Question 9 ---")
    val parent = launch {
        launch {
            try {
                delay(1000)
            } finally {
                println("Child 1 cleanup message")
            }
        }
        launch {
            try {
                delay(1000)
            } finally {
                println("Child 2 cleanup message")
            }
        }
    }
    delay(100)
    parent.cancelAndJoin()
    println("end")
}

// Question 10: Profile ready
fun question10() = runBlocking {
    println("--- Question 10 ---")
    coroutineScope {
        launch {
            delay(200)
            println("Avatar loaded")
        }
        launch {
            delay(100)
            println("Settings loaded")
        }
    }
    println("Profile ready")

    launch {
        delay(100)
        println("Background task after scope")
    }
    println("end of main")
}
