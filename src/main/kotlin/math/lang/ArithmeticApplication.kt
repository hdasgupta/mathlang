package math.lang

import math.lang.tokenizer.getOperand
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ArithmeticApplication

fun main(args: Array<String>) {
    runApplication<ArithmeticApplication>(*args)
}
