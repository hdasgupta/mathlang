package math.lang.controller

import math.lang.Results
import math.lang.common.Differentiate
import math.lang.common.Operand
import math.lang.common.calculator.Command
import math.lang.common.calculator.History
import math.lang.common.calculator.Node
import math.lang.diff
import math.lang.tokenizer.Token
import math.lang.tokenizer.TokenNode
import math.lang.tokenizer.getOperand
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
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
                        Button(Command.zero),
                        Button(Command.result, colSpan = 4),
                    )

                )
            )
        )


    @RequestMapping(value = ["/calculator-{type}"])
    fun calculator(@PathVariable type:CalculatorType, @RequestParam(required = false) command: Command? =null, map: ModelMap, req: HttpServletRequest, sess: HttpSession): String {
        if(command == null) {
            map["buttons"] = buttons[type]
            sess.setAttribute("node", Node())
            return "Calculator"
        } else {
            val node: Node = sess.getAttribute("node") as Node
            if(node.command(command)) {
                sess.setAttribute("node", Node())
                var history: MutableList<History> = if(sess.getAttribute("history") == null) {
                    mutableListOf()
                } else {
                    sess.getAttribute("history") as MutableList<History>
                }
                node.getHistory()?.let { history.add(it) }
                sess.setAttribute("history", history)
            }
            return "CalculatorDisplay"
        }

    }

}

enum class CalculatorType {
    sim,
    tri,
    sci
}

open class Button(val command: Command?, val rowSpan: Int = 1, val colSpan: Int = 1)

class SkipButton(): Button(null, 0, 0)
