package math.lang.controller

import math.lang.common.Differentiate
import math.lang.common.Operand
import math.lang.common.Results
import math.lang.diff
import math.lang.tokenizer.getOperand
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import javax.servlet.http.HttpServletRequest

@Controller
class Differentiation {
    @RequestMapping(value = ["/differentiate"])
    fun getTemplate(
        @RequestParam(required = false) formula: String? = null,
        map: ModelMap,
        req: HttpServletRequest
    ): String {
        map["formula"] = formula

        return "DifferentiationPage"
    }

    @RequestMapping(value = ["/diff"])
    fun getDiff(@RequestParam formula: String, map: ModelMap): String {
        val operation = getOperand(formula)
        val operand: Operand = Differentiate(operand = operation)
        map["operand"] = operand

        return "operands/MainOperand"
    }

    @RequestMapping(value = ["/diffHtml"])
    fun getDiffHtml(@RequestParam formula: String, map: ModelMap): String {
        try {
            val results: Results = diff(getOperand(formula))
            map["results"] = ArrayList(results)
            map["formula"] = formula
        } catch (t: Throwable) {
            map["results"] = Results()
        }

        return "DifferentiationResult"
    }

}
