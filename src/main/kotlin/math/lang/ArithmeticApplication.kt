package math.lang

import math.lang.common.ExpressionConstants
import math.lang.tokenizer.Token
import math.lang.tokenizer.TokenNode
import math.lang.tokenizer.getOperand
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ArithmeticApplication

fun main(args: Array<String>) {
	runApplication<ArithmeticApplication>(*args)
	println(ExpressionConstants.y.new())
	println( getOperand(TokenNode.getTree(Token.getTokens(ExpressionConstants.y.new().toString()))))
}
