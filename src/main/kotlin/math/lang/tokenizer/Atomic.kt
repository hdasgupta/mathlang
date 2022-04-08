package math.lang.tokenizer

class Atomic<T>(var obj:T) {
    fun get(): T = obj
    fun set(obj:T): Unit {
        this.obj = obj
    }
}