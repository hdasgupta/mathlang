package math.lang.controller

import math.lang.common.Results
import math.lang.common.simp
import math.lang.tokenizer.getOperand
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
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
