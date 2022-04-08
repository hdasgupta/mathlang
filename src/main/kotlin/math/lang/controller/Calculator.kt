package math.lang.controller

import math.lang.Results
import math.lang.common.Differentiate
import math.lang.common.Operand
import math.lang.common.Operation
import math.lang.common.calculator.Command
import math.lang.common.calculator.History
import math.lang.common.calculator.Node
import math.lang.diff
import math.lang.tokenizer.Token
import math.lang.tokenizer.TokenNode
import math.lang.tokenizer.getOperand
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.util.MimeTypeUtils
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession

@Controller
class Calculator {

    val buttons: Map<CalculatorType, Array<Array<Button>>> =
        mapOf(
            Pair(
                CalculatorType.sim,
                arrayOf(
                    arrayOf(
                        Button(Command.one),
                        Button(Command.two),
                        Button(Command.three),
                        Button(Command.add),
                        Button(Command.sub),
                    ),
                    arrayOf(
                        Button(Command.four),
                        Button(Command.five),
                        Button(Command.six),
                        Button(Command.mul),
                        Button(Command.div),
                    ),
                    arrayOf(
                        Button(Command.seven),
                        Button(Command.eight),
                        Button(Command.nine),
                        Button(Command.pow),
                        Button(Command.mod),
                    ),
                    arrayOf(
                        Button(Command.delete),
                        Button(Command.zero),
                        Button(Command.clear),
                        Button(Command.result, colSpan = 2),
                    )

                )
            )
        )


    @RequestMapping(value = ["/calculator-{type}"])
    fun calculator(@PathVariable type:CalculatorType, map: ModelMap, req: HttpServletRequest, sess: HttpSession): String {
        map["buttons"] = buttons[type]
        map["type"] = type
        map["types"] = CalculatorType.values()
        map["columnCount"] = buttons[type]?.get(0)?.map { it.colSpan }?.reduce { a, b -> a + b }
        sess.setAttribute("node", Node())
        return "Calculator"
    }

    @RequestMapping(value = ["/calculator-{type}-result"], produces=[MimeTypeUtils.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun update(@PathVariable type:CalculatorType, @RequestParam command: Command, sess: HttpSession): Map<String, String>? {
        val node: Node = sess.getAttribute("node") as Node
        if(node.command(command)) {
            var history: MutableList<History> = if (sess.getAttribute("history") == null) {
                mutableListOf()
            } else {
                sess.getAttribute("history") as MutableList<History>
            }
            node.getHistory()?.let { history.add(it) }
            sess.setAttribute("history", history)
            val lastOperation = if(node.getHistory()?.operand is Operation) if((node.getHistory()?.operand as Operation).operands.size == 1) "${(node.getHistory()?.operand as Operation).operator.symbol}(" else "${(node.getHistory()?.operand as Operation).operator.symbol}${(node.getHistory()?.operand as Operation).operands.last()}" else ""
            sess.setAttribute("node", node.getHistory()?.let { Node(lastAnswer = it.result, lastOperation = lastOperation) })
            return node.getHistory()?.let { mapOf(Pair("result", it.result)) }
        } else {
            return mapOf(Pair("result", node.data.toString()))
        }
    }


}

enum class CalculatorType(val text: String) {
    sim("Simple"),
    tri("Trigonometric"),
    sci("Scientific")
}

open class Button(val command: Command?, val rowSpan: Int = 1, val colSpan: Int = 1)

class SkipButton(): Button(null, 0, 0)
