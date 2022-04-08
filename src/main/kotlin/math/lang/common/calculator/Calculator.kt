package math.lang.common.calculator

import math.lang.common.Operand
import math.lang.tokenizer.Token
import math.lang.tokenizer.TokenNode
import math.lang.tokenizer.getOperand
import java.math.BigDecimal
import java.util.*

enum class Command(val text: String? = null) {
    zero("0"),
    one("1"),
    two("2"),
    three("3"),
    four("4"),
    five("5"),
    six("6"),
    seven("7"),
    eight("8"),
    nine("9"),
    add("+"),
    sub("-"),
    mul("*"),
    div("/"),
    pow("^"),
    mod("%"),
    log("log("),
    ln("ln("),
    opening("("),
    closing(")"),
    sin("sin("),
    cos("cos("),
    tan("tan("),
    cot("cot("),
    sec("sec("),
    cosec("cosec("),
    result("="),
    left("LEFT"),
    right("RIGHT"),
    delete("DEL"),
    clear("CLEAR")


}
class Node(var cursor: Int = 0, val data: StringBuilder = StringBuilder()) {
    private var history: History? = null

    fun command(command: Command): Boolean {
        when(command) {
            Command.result -> prepareResult()
            Command.left -> if(cursor>0) cursor-- else cursor = 0
            Command.right -> if(cursor<data.length) cursor++ else cursor = data.length
            Command.delete -> if(cursor>0) data.removeRange(cursor-1, cursor-1)
            Command.clear -> data.clear()
            else -> Optional.ofNullable(command.text).ifPresent {
                data.insert(cursor, it)
                cursor+=it.length
            }
        }
        return when(command) {
            Command.result -> true
            else -> false
        }
    }

    private fun prepareResult() {
        val tokens : List<Token> = Token.getTokens("$data")
        val tokenNode : TokenNode = TokenNode.getTree(tokens)
        val parsed: Operand = getOperand(tokenNode)
        val result = parsed.calc()
        if(result is BigDecimal) {
            history = History(parsed, result.toPlainString())
        } else {
            history = History(parsed, result.toString())
        }
    }

    fun getHistory(): History? = history
}

class History(val operand: Operand, val result: String)