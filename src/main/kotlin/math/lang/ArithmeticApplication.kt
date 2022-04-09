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
	val str = getOperand(TokenNode.getTree(Token.getTokens("-sin(x)"))).toString()
	val tokens = Token.getTokens(str)
	println(str)
	println( TokenNode.getTree(tokens))
	println(tokens.size)
}
