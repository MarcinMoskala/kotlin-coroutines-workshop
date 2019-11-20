import kotlin.test.assertEquals

inline fun <reified T> assertThrows(body: () -> Unit) {
    val error = try {
        body()
        Any()
    } catch (t: Throwable) {
        t
    }
    assertEquals(T::class, error::class)
}