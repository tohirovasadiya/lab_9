# Laboratory 9: Kotlin Coroutines

**Course:** Mobile Development / Coroutines  
**Organization:** TTPU  
**Author:** Student Solution  

---

## Overview & Core Concepts

### Suspending vs. Blocking in Kotlin Coroutines

```kotlin
/*
 * ============================================================================
 *               SUSPENDING VS BLOCKING IN KOTLIN COROUTINES
 * ============================================================================
 *
 * 1. BLOCKING (e.g., Thread.sleep(millis)):
 *    - Halts and ties up the underlying Operating System (OS) thread.
 *    - While blocked, the thread CANNOT execute any other tasks or coroutines.
 *    - Inefficient for I/O operations and leads to dropped frames or ANR
 *      (Application Not Responding) dialogs when invoked on the Android UI thread.
 *
 * 2. SUSPENDING (e.g., delay(timeMillis)):
 *    - Pauses the execution of the current coroutine WITHOUT blocking the OS thread.
 *    - Yields the thread back to the dispatcher / event loop, enabling other
 *      coroutines to run concurrently on the exact same thread.
 *    - When the suspended operation completes, the coroutine is resumed on an
 *      available thread from its dispatcher context.
 * ============================================================================
 */
```

---

## Practical Questions (Questions 1 – 15)

### Question 1: First Coroutine

#### Context & Task
A cafe app must confirm the order immediately while coffee preparation takes 1 second. The confirmation must not wait for coffee. Build a console app with `runBlocking` where a launched coroutine waits 1s with `delay()` and prints `"Coffee ready"`, while the main body prints `"Order accepted"` right after launch.

#### Code Implementation
```kotlin
fun question1() = runBlocking {
    launch {
        delay(1000)
        println("Coffee ready")
    }
    println("Order accepted")
}
```

#### Program Output
```text
Order accepted
Coffee ready
```

#### Explanation & Answers
* **Order of Lines:** `"Order accepted"` is printed first, followed by `"Coffee ready"` 1 second later.
* **Why `"Order accepted"` comes first:** `launch` is a non-blocking coroutine builder that schedules the child coroutine and returns immediately to the caller scope. Consequently, execution continues to `println("Order accepted")` right after `launch`. The launched coroutine suspends non-blockingly via `delay(1000)` and resumes to print `"Coffee ready"` after 1 second.

---

### Question 2: Three Baristas

#### Context & Task
Three baristas make drinks at the same time on one thread: Espresso (300 ms), Tea (100 ms), Latte (200 ms). Launch three coroutines with `delay()`, write output. Then replace `delay()` with `Thread.sleep()` and write the new output. Explain what the thread does in each case.

#### Code Implementation
```kotlin
// Version 1: With delay()
fun question2WithDelay() = runBlocking {
    launch { println("Espresso start"); delay(300); println("Espresso end") }
    launch { println("Tea start"); delay(100); println("Tea end") }
    launch { println("Latte start"); delay(200); println("Latte end") }
}

// Version 2: With Thread.sleep()
fun question2WithSleep() = runBlocking {
    launch { println("Espresso start"); Thread.sleep(300); println("Espresso end") }
    launch { println("Tea start"); Thread.sleep(100); println("Tea end") }
    launch { println("Latte start"); Thread.sleep(200); println("Latte end") }
}
```

#### Program Output

**Output with `delay()`:**
```text
Espresso start
Tea start
Latte start
Tea end
Latte end
Espresso end
```

**Output with `Thread.sleep()`:**
```text
Espresso start
Espresso end
Tea start
Tea end
Latte start
Latte end
```

#### Explanation & Answers
* **What the thread does with `delay()`:** The thread suspends the active coroutine and frees itself to execute the remaining scheduled coroutines concurrently on that single thread.
* **What the thread does with `Thread.sleep()`:** The thread blocks completely during sleep, holding the thread hostage so no other coroutine can run until the sleeping coroutine completes sequentially.

---

### Question 3: Chain of Suspend Functions

#### Context & Task
A login flow has three steps (load user, check password, open profile) that must run strictly in order. Write three suspend functions calling each other in sequence. Show that suspending does not reorder lines.

#### Code Implementation
```kotlin
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
    step1LoadUser()
}
```

#### Program Output
```text
Step 1: Load User
Step 2: Check Password
Step 3: Open Profile
```

#### Explanation & Answers
* Suspending functions execute top-to-bottom sequentially within a coroutine, maintaining standard procedural control flow. Marking a function `suspend` allows pausing execution without blocking the underlying thread, but it does **not** introduce async concurrency or reorder code execution.

---

### Question 4: Calling Suspend from Normal Code

#### Context & Task
A teammate wrote a normal function that calls a suspend function and the project does not compile. Reproduce the error message, write two fixes, and state which fix you never use on the Android UI thread and why.

#### Erroneous Code & Compiler Error
```kotlin
suspend fun loadUserFromNetwork(): String {
    delay(100)
    return "User data"
}

fun normalFunction() {
    val user = loadUserFromNetwork() // Compiler Error!
}
```
**Compiler Error Message:**
```text
Suspend function 'loadUserFromNetwork' should be called only from a coroutine or another suspend function
```

#### Code Fixes
```kotlin
// Fix 1: Wrap in runBlocking
fun fixWithRunBlocking() {
    val user = runBlocking { loadUserFromNetwork() }
    println("Loaded: $user")
}

// Fix 2: Launch on CoroutineScope
fun fixWithCoroutineScope(scope: CoroutineScope) {
    scope.launch {
        val user = loadUserFromNetwork()
        println("Loaded: $user")
    }
}
```

#### Explanation & Answers
* **Forbidden Fix on Android UI Thread:** You must **never** use `runBlocking` on the Android UI (main) thread.
* **Why:** `runBlocking` blocks the main UI thread while waiting for coroutines to complete, causing the app to freeze, drop frames, and trigger ANR (Application Not Responding) dialogs. On Android, always use `lifecycleScope.launch` or `viewModelScope.launch` instead.

---

### Question 5: Suspend is Not a Thread

#### Context & Task
Prove or disprove the belief that marking a function `suspend` moves work to a background thread. Call a suspend function returning current thread name from `runBlocking`. Name the compiler's hidden parameter.

#### Code Implementation
```kotlin
suspend fun getCurrentThreadName(): String {
    return Thread.currentThread().name
}

fun question5() = runBlocking {
    println("Thread before call: ${Thread.currentThread().name}")
    val returnedThread = getCurrentThreadName()
    println("Thread returned in suspend: $returnedThread")
}
```

#### Program Output
```text
Thread before call: main
Thread returned in suspend: main
```

#### Explanation & Answers
* **Disproof:** Both the caller and the suspend function output `main`. Marking a function `suspend` does **not** shift execution to a background thread; thread dispatching is controlled by `CoroutineDispatcher`.
* **Compiler's Hidden Parameter:** The Kotlin compiler appends a hidden parameter of type `kotlin.coroutines.Continuation<T>` to every `suspend` function for Continuation-Passing Style (CPS) transformation.

---

### Question 6: Ten Downloads, One Thread

#### Context & Task
Launch 10 coroutines under `runBlocking` on the main thread. Show that all 10 start before any finishes. Decide if this is concurrency or parallelism and why.

#### Code Implementation
```kotlin
fun question6() = runBlocking {
    List(10) { i ->
        launch {
            println("download $i started on ${Thread.currentThread().name}")
            delay(500)
            println("download $i finished on ${Thread.currentThread().name}")
        }
    }
}
```

#### Program Output
```text
download 0 started on main
download 1 started on main
download 2 started on main
download 3 started on main
download 4 started on main
download 5 started on main
download 6 started on main
download 7 started on main
download 8 started on main
download 9 started on main
download 0 finished on main
download 1 finished on main
download 2 finished on main
download 3 finished on main
download 4 finished on main
download 5 finished on main
download 6 finished on main
download 7 finished on main
download 8 finished on main
download 9 finished on main
```

#### Explanation & Answers
* **Concurrency vs. Parallelism:** This is **Concurrency**, NOT Parallelism.
* **Why:** All 10 coroutines execute on a single thread (`main`). They make progress concurrently by yielding control when suspended (`delay(500)`), allowing the thread event loop to start all 10 tasks before any finish. Parallelism requires multiple threads running across multiple CPU cores simultaneously.

---

### Question 7: Order of Launch

#### Context & Task
Under `runBlocking`, print `"1"`, launch a coroutine printing `"2"`, `delay(100)`, `"3"`, then print `"4"`, `delay(50)`, `"5"`. Predict output and explain why `"2"` does not come right after `"1"`.

#### Code Implementation
```kotlin
fun question7() = runBlocking {
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
```

#### Predicted & Program Output
```text
1
4
2
5
3
```

#### Explanation & Answers
* **Why `"2"` does not come right after `"1"`:** Calling `launch` queues the new child coroutine onto the dispatcher's event queue without immediately interrupting the main coroutine. Execution continues sequentially to print `"4"`. When the main coroutine suspends at `delay(50)`, the dispatcher processes queued items, executing `"2"`. After 50 ms, main resumes and prints `"5"`. Finally, after 100 ms total, the child coroutine resumes to print `"3"`.

---

### Question 8: Parent Waits for Child

#### Context & Task
Launch a parent coroutine that launches a child coroutine and prints `"parent body done"`. The child waits and prints `"child done"`. Main waits for parent with `join()` and prints `"report completed"`. Name the structured concurrency rule.

#### Code Implementation
```kotlin
fun question8() = runBlocking {
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
```

#### Program Output
```text
parent body done
child done
report completed
```

#### Explanation & Answers
* **Structured Concurrency Rule:** **A parent coroutine cannot complete until all of its child coroutines have completed.** Calling `parent.join()` suspends main until the parent job finishes both its own body and all active child coroutines launched in its scope.

---

### Question 9: Screen Closed, Requests Cancelled

#### Context & Task
Launch a parent coroutine with two child coroutines waiting inside `try-finally`. Cancel the parent after 100 ms with `cancelAndJoin()` and print `"end"`. Explain why children stop although only the parent was cancelled.

#### Code Implementation
```kotlin
fun question9() = runBlocking {
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
```

#### Program Output
```text
Child 1 cleanup message
Child 2 cleanup message
end
```

#### Explanation & Answers
* **Why children stop:** Under structured concurrency, parent and child jobs form a linked hierarchy. Cancelling a parent coroutine automatically propagates `CancellationException` recursively down to all its child coroutines, terminating their execution and executing their `finally` blocks for cleanup.

---

### Question 10: Profile Ready

#### Context & Task
Use `coroutineScope` with two launched coroutines (avatar 200 ms, settings 100 ms). Print `"Profile ready"` after scope. Launch another coroutine after scope and show `"end of main"` prints before it. Explain the difference.

#### Code Implementation
```kotlin
fun question10() = runBlocking {
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
```

#### Program Output
```text
Settings loaded
Avatar loaded
Profile ready
end of main
Background task after scope
```

#### Explanation & Answers
* **Difference between the two cases:** `coroutineScope` is a suspending scoping function that suspends the caller and waits for all internal child coroutines to complete before returning; hence `"Profile ready"` only prints after both avatar and settings load. Conversely, `launch` outside the scope spawns a background coroutine non-blockingly, allowing main to immediately execute `println("end of main")`.

---

### Question 11: Reusable Counter

#### Context & Task
Write an extension function on `CoroutineScope` that starts a counter coroutine printing every 100 ms and returns its `Job`. Start two counters inside `coroutineScope`. Explain why the receiver is needed.

#### Code Implementation
```kotlin
fun CoroutineScope.startCounter(name: String, max: Int): Job {
    return launch {
        for (i in 1..max) {
            delay(100)
            println("$name: $i")
        }
    }
}

fun question11() = runBlocking {
    coroutineScope {
        startCounter("Counter A", 3)
        startCounter("Counter B", 2)
        println("launched")
    }
}
```

#### Program Output
```text
launched
Counter A: 1
Counter B: 1
Counter A: 2
Counter B: 2
Counter A: 3
```

#### Explanation & Answers
* **Why the `CoroutineScope` receiver is needed:** Making the function an extension on `CoroutineScope` provides access to the receiver's context (`this`). This ensures that the internal `launch` call attaches the child coroutine to the caller's scope, obeying structured concurrency so the parent scope manages its lifecycle.

---

### Question 12: Returns At Once or Waits

#### Context & Task
Create two helpers: Helper 1 as extension on `CoroutineScope` (bg greeter, returns immediately), Helper 2 as suspend function with `coroutineScope` (returns when done). Call both and verify output.

#### Code Implementation
```kotlin
// Helper 1: Extension on CoroutineScope
fun CoroutineScope.startGreeterBackground(names: List<String>): Job = launch {
    for (name in names) {
        delay(100)
        println("Hello $name (bg)")
    }
}

// Helper 2: Suspend function with coroutineScope
suspend fun greetAllSequential(names: List<String>) = coroutineScope {
    for (name in names) {
        delay(100)
        println("Hello $name (sync)")
    }
}

fun question12() = runBlocking {
    println("Calling Helper 1 (Extension on CoroutineScope)...")
    startGreeterBackground(listOf("Alice", "Bob"))
    println("Returned immediately from Helper 1!")

    delay(300)

    println("\nCalling Helper 2 (Suspend function with coroutineScope)...")
    greetAllSequential(listOf("Charlie", "Dave"))
    println("Returned after completion from Helper 2!")
}
```

#### Program Output
```text
Calling Helper 1 (Extension on CoroutineScope)...
Returned immediately from Helper 1!
Hello Alice (bg)
Hello Bob (bg)

Calling Helper 2 (Suspend function with coroutineScope)...
Hello Charlie (sync)
Hello Dave (sync)
Returned after completion from Helper 2!
```

#### Explanation & Answers
* **Helper 1 (`CoroutineScope.startGreeterBackground`):** Non-blocking builder returning a `Job` handle **immediately**.
* **Helper 2 (`suspend fun greetAllSequential`):** Suspending function using `coroutineScope` that **waits** until all child coroutines finish before returning.

---

### Question 13: Five Builders

#### Context & Task
Write a program using `launch`, `async`, `runBlocking`, and `coroutineScope`. Provide the lecture cheat sheet for all five builders including `withContext`.

#### Code Implementation
```kotlin
fun question13() = runBlocking {
    val job: Job = launch { delay(50) }
    val deferred: Deferred<Int> = async { 42 }
    val scopeResult: String = coroutineScope { "Scope complete" }
    println("launch returned: $job")
    println("async returned: $deferred")
    println("coroutineScope returned: $scopeResult")
    println("async await result: ${deferred.await()}")
}
```

#### Program Output
```text
launch returned: StandaloneCoroutine{Active}@...
async returned: DeferredCoroutine{Active}@...
coroutineScope returned: Scope complete
async await result: 42
```

#### Five Coroutine Builders Cheat Sheet (Lecture 19, Slide 17)

| Builder | Return Type | Blocks Thread? | Waits for Children? | Purpose & Behavior |
| :--- | :--- | :--- | :--- | :--- |
| `launch` | `Job` | **No** | **No** (returns immediately; parent scope waits) | Starts a fire-and-forget coroutine without returning a result value. |
| `async` | `Deferred<T>` | **No** | **No** (returns `Deferred` immediately) | Starts concurrent computation returning a `Deferred<T>` result read via `.await()`. |
| `runBlocking` | `T` (block result) | **Yes** | **Yes** | Bridges standard code to coroutines by blocking current thread until block finishes. |
| `withContext` | `T` (block result) | **No** (suspends) | **Yes** | Switches coroutine context (e.g., dispatcher) and suspends until block completes. |
| `coroutineScope` | `T` (block result) | **No** (suspends) | **Yes** | Scoping function that creates nested scope and suspends until all children complete. |

---

### Question 14: Job or Deferred

#### Context & Task
Show why `launch` cannot return a calculation result directly, then use `async` with `Deferred` to yield 42 via `await()`. Explain difference between `Job` and `Deferred`.

#### Code Implementation
```kotlin
fun question14() = runBlocking {
    val job: Job = launch {
        val calculated = 6 * 7
        println("Calculated inside launch: $calculated")
        // launch does not carry or return a result value to caller
    }
    println("Launch version returns Job: $job")

    val deferred: Deferred<Int> = async {
        6 * 7
    }
    val value: Int = deferred.await()
    println("Async version returns Deferred and yields value via await(): $value")
}
```

#### Program Output
```text
Calculated inside launch: 42
Launch version returns Job: StandaloneCoroutine{Completed}@...
Async version returns Deferred and yields value via await(): 42
```

#### Explanation & Answers
* **`Job`:** Represents a lifecycle handle (`Active`, `Completed`, `Cancelled`) for fire-and-forget coroutines started by `launch`. It carries no result value.
* **`Deferred<T>`:** A subtype interface extending `Job` (`interface Deferred<out T> : Job`) returned by `async`. It represents a lightweight promise/future carrying a computation result of type `T` fetched via `.await()`.

---

### Question 15: Three Exports in a Scope

#### Context & Task
Export three files concurrently (X 300 ms, Y 100 ms, Z 200 ms). Ensure output order is `Start, Y, Z, X, All done` using `coroutineScope` with no manual `join` or `delay` in main.

#### Code Implementation
```kotlin
fun question15() = runBlocking {
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
```

#### Program Output
```text
Start
Y
Z
X
All done
```

#### Explanation & Answers
* `coroutineScope` automatically waits for all three child coroutines to complete. Since Y finishes in 100 ms, Z in 200 ms, and X in 300 ms, they output in the order Y, Z, X. Once all complete, `coroutineScope` unblocks and `"All done"` is printed without calling `join()` manually.

---

## Extra Questions (Extras 1 – 5)

### Extra 1: Delay or Sleep: Measure It

#### Context & Task
Measure three launched coroutines with `delay()` versus `Thread.sleep()` using `measureTimeMillis`. Print times rounded to hundreds and explain difference.

#### Code Implementation
```kotlin
fun extraQuestion1() = runBlocking {
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
```

#### Program Output
```text
Delay time ~ 300 ms
Sleep time ~ 900 ms
```

#### Explanation & Answers
* **Why times differ:** `delay()` suspends coroutines non-blockingly, allowing all three coroutines on the single thread to wait concurrently (~300 ms total). `Thread.sleep()` blocks the single thread completely, forcing the three coroutines to execute sequentially (300 + 300 + 300 = ~900 ms total).

---

### Extra 2: Three Levels of Coroutines

#### Context & Task
Launch child 1 (which launches grandchild 200 ms) and child 2 (100 ms). Draw the coroutine tree and explain why `"scope done"` is printed last.

#### Code Implementation
```kotlin
fun extraQuestion2() = runBlocking {
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
```

#### Program Output
```text
child 1 done
child 2 done
grandchild done
scope done
```

#### Coroutine Tree Diagram
```text
             coroutineScope (Root)
             /                   \
        Child 1                 Child 2 (100 ms)
           |
      Grandchild (200 ms)
```

#### Explanation & Answers
* Under structured concurrency, `coroutineScope` waits for all direct children (Child 1, Child 2) and indirect descendants (Grandchild) to complete before finishing. Grandchild finishes last after 200 ms, so `"scope done"` is printed last.

---

### Extra 3: No Scope, No Launch

#### Context & Task
Reproduce the error when calling `launch` inside plain `main()`. Fix it with `runBlocking` and explain what `runBlocking` does with the current thread.

#### Erroneous Code & Error
```kotlin
fun plainMain() {
    launch { // Compiler Error!
        println("Hello")
    }
}
```
**Compiler Error Message:**
```text
Unresolved reference: launch
```

#### Code Fix
```kotlin
fun extraQuestion3() = runBlocking {
    launch {
        println("Hello from launch wrapped inside runBlocking")
    }
}
```

#### Explanation & Answers
* `launch` is an extension function on `CoroutineScope` and requires an active scope to run. `runBlocking` creates a root `CoroutineScope` and blocks the current thread until the enclosed coroutines complete execution.

---

### Extra 4: What the Compiler Sees (Bytecode Decompilation)

#### Context & Task
Write a suspend function `fetchUser(id: String): User`. Decompile its Kotlin Bytecode to Java. Copy the decompiled signature, name the extra parameter and return type, and explain their purpose.

#### Code Implementation
```kotlin
data class User(val id: String, val name: String)

suspend fun fetchUser(id: String): User {
    delay(100)
    return User(id, "Alice")
}
```

#### Decompiled Java Signature
```java
@Nullable
public static final Object fetchUser(@NotNull String id, @NotNull Continuation<? super User> $completion)
```

#### Explanation & Answers
* **Extra Parameter:** `$completion` of type `kotlin.coroutines.Continuation<? super User>`. It serves as the callback interface passing execution state and resumption point for Continuation-Passing Style (CPS).
* **New Return Type:** `java.lang.Object`. It returns either the final result (`User`) if executed synchronously, or the marker object `COROUTINE_SUSPENDED` if execution suspended.

---

### Extra 5: Greet Everybody

#### Context & Task
Write a suspend function with `coroutineScope` taking names and launching coroutines waiting `i * 100 ms`. Print `"before"`, call with Ada, Linus, Grace, then print `"after"`. Explain why `"after"` is last.

#### Code Implementation
```kotlin
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
    println("before")
    greetEverybody(listOf("Ada", "Linus", "Grace"))
    println("after")
}
```

#### Program Output
```text
before
Hello, Ada
Hello, Linus
Hello, Grace
after
```

#### Explanation & Answers
* `"before"` prints first. `greetEverybody` creates a `coroutineScope` launching three child coroutines with 100 ms, 200 ms, and 300 ms delays. `coroutineScope` suspends execution of `main` until all child coroutines finish. Thus, `"after"` is printed only after Grace completes at 300 ms.
