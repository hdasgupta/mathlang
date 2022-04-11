package math.lang.controller

import math.lang.common.DecimalLiteral
import math.lang.common.ExpressionConstants
import math.lang.common.ExpressionConstants.Companion.replace
import math.lang.common.ExpressionConstants.Companion.x
import math.lang.common.Results
import math.lang.common.simp
import math.lang.tokenizer.getOperand
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.util.MimeTypeUtils
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import javax.servlet.http.HttpServletRequest

@Controller
class Simplify {
    @RequestMapping(value = ["/simplify"])
    fun getTemplate(
        @RequestParam(required = false) formula: String? = null,
        map: ModelMap,
        req: HttpServletRequest
    ): String {
        map["formula"] = formula

        return "SimplificationPage"
    }

    @PostMapping(value = ["/eval"], produces = [MimeTypeUtils.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun getEval(
        @RequestParam formula: String,
        @RequestParam(name = "x") X: Double,
        map: ModelMap
    ): String {
        val operand = getOperand(formula)
        val value = DecimalLiteral(X)
        val replace = replace(operand, x, value)
        val num = replace.calc()
        return if(num.toDouble().isInfinite()) {
            "Infinity"
        } else if(num.toDouble().isNaN()) {
            "Not a Number"
        } else {
            num.toString()
        }
    }

    @RequestMapping(value = ["/simple"])
    fun getDiffHtml(
        @RequestParam formula: String,
        @RequestParam(required = false) additionalButtons: Boolean = true,
        map: ModelMap
    ): String {
        try {
            val results: Results = simp(getOperand(formula))
            map["results"] = ArrayList(results)
            map["additionalButtons"] = additionalButtons
            map["formula"] = formula
        } catch (t: Throwable) {
            map["results"] = Results()
        }

        return "SimplificationResult"
    }

}


class Value<T>(val value:T)