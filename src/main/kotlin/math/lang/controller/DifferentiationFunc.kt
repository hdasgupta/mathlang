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
            val results1: Results = diff(getOperand(formula1))
//            map["results"] = ArrayList(results)
//            map["formula"] = formula
        } catch (t: Throwable) {
            map["results"] = Results()
        }

        return "DifferentiationResult"
    }

}
