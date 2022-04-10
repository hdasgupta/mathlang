package math.lang.controller

import math.lang.common.Results
import math.lang.common.Differentiate
import math.lang.common.Operand
import math.lang.common.simp
import math.lang.diff
import math.lang.tokenizer.Token
import math.lang.tokenizer.TokenNode
import math.lang.tokenizer.getOperand
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import javax.servlet.http.HttpServletRequest

@Controller
class Simplify {
    @RequestMapping(value = ["/simplify"])
    fun getTemplate(@RequestParam(required = false) formula: String? = null, map: ModelMap, req: HttpServletRequest): String {
        map["formula"] = formula

        return "SimplificationPage"
    }
    @RequestMapping(value = ["/simple"])
    fun getDiffHtml(@RequestParam formula: String, map: ModelMap): String {
        try {
            val results: Results = simp(getOperand(TokenNode.getTree(Token.getTokens(formula))))
            map["results"] = ArrayList(results)
            map["formula"] = formula
        } catch (t:Throwable) {
            map["results"] = Results()
        }

        return "SimplificationResult"
    }

}
