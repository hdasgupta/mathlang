package math.lang.controller

import math.lang.common.Differentiate
import math.lang.common.ExpressionConstants.Companion.div
import math.lang.common.Operand
import math.lang.common.Result
import math.lang.common.Results
import math.lang.diff
import math.lang.tokenizer.getOperand
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import javax.servlet.http.HttpServletRequest

@Controller
class DifferentiationFunc {
    @RequestMapping(value = ["/differentiateFunc"])
    fun getTemplate(
        @RequestParam(required = false) formula1: String? = null,
        @RequestParam(required = false) formula2: String? = null,
        map: ModelMap,
        req: HttpServletRequest
    ): String {
        map["formula1"] = formula1
        map["formula2"] = formula2

        return "DifferentiationOfFunctionsPage"
    }


    @RequestMapping(value = ["/diffFuncHtml"])
    fun getDiffHtml(@RequestParam formula1: String, @RequestParam formula2: String, map: ModelMap): String {
        try {
            val operand1 = getOperand(formula1)
            val operand2 = getOperand(formula2)
            val results1: Results = diff(operand1)
            val results2: Results = diff(operand2)
            val diff1 = Differentiate(operand = operand1)
            val diff2 = Differentiate(operand = operand2)
            val results = Results()
            results1.forEach {
                results.add(
                    Result(
                        div(it.operand, diff2),
                        it.fx,
                        it.dFx,
                        it.formulaName,
                        it.assumptions,
                        it.derive
                    )
                )
            }
            results2.forEach {
                results.add(
                    Result(
                        div(results1.last().operand, it.operand),
                        it.fx,
                        it.dFx,
                        it.formulaName,
                        it.assumptions,
                        it.derive
                    )
                )
            }
            map["results"] = results.toList()
            map["formula"] = div(diff1, diff2)
        } catch (t: Throwable) {
            map["results"] = Results()
        }

        return "DifferentiationResult"
    }

}
