var currScript = $('script[src*=common]'); // or better regexp to get the file name..

var path = currScript.attr('data-url');

var autoload = currScript.attr('data-autoload');

const currPath = $(location).attr("href")

const url = currPath.substring(0, currPath.lastIndexOf('/')+1)+path



var lastResponse = {}
var responses = {}
var successes = []
function load(elem, json, url, lateInit = false, succCallBack, errCallBack) {
    if(!lastResponse[url+JSON.stringify(json)]) {
        $(document).ready(function() {
            $.post(url,
                        json,
                        function (response, status) {


                        }
                )
                .done(function(response) {
                    lastResponse[url+JSON.stringify(json)] = response
                    responses[elem] = response
                    if(!lateInit) {
                        $(elem).html(response)
                    } else {
                        successes.push(json.formula)
                    }
                    if(!!succCallBack) {
                        succCallBack()
                    }
                })
                .fail(function() {
                    if(!!errCallBack) {
                        errCallBack()
                    }
                })
        })
    } else {
        if(!lateInit) {
            $(elem).html(lastResponse[url+JSON.stringify(json)])
        } else {
            successes.push(json.formula)
        }
        responses[elem] = lastResponse[url+JSON.stringify(json)]
        if(!!succCallBack) {
            succCallBack()
        }
    }
}

function loadHtml(elem, html) {
    $(document).ready(function() {
        $(elem).html(html);
    })
}

function onChange(e) {
    onReset()
    $(document).ready(function() {
        var obj = {}
        props = ['formula', 'formula1', 'formula2'].filter(prop=>$('#'+prop).val())
        props.forEach(prop=>obj[prop]=$('#'+prop).val())
        load("#table", obj, url, !autoload, onSuccess, onError)
    })
}

function generate(elem) {
    onReset()
    $(elem).html(responses[elem])
    $("#generate").attr("disabled", "disabled")
}

function onError() {
    $("#success").hide()
    $("#error").show()
    $("#generate").attr("disabled", "disabled")
}

function onReset() {
    $("#success").hide()
    $("#error").hide()
}

function onSuccess() {
    if(responses["#table"].indexOf("ERROR")>=0) {
        onError()
    } else {
        $("#success").show()
        $("#error").hide()
        $("#generate").removeAttr("disabled")

    }
}

$(document).ready(function() {

    onReset()
    $('#auto').hide()
    $('*[tabindex=1]').focus()
})

function isNumber(str) {
    if (typeof str != "string") return false // we only process strings!
    return !isNaN(str) && // use type coercion to parse the _entirety_ of the string (`parseFloat` alone does not do this)...
         !isNaN(parseFloat(str)) // ...and ensure strings of whitespace fail

}

function getNumber(str) {
    return parseFloat(str)
}

function loadVal(elem, json, url) {
    if(!lastResponse[url+JSON.stringify(json)]) {
        $(document).ready(function() {
            $.post(url,
                        json,
                        function (response, status) {


                        },
                        'text'
                )
                .done(function(response) {
                    lastResponse[url+JSON.stringify(json)] = response
                    responses[elem] = response
                    $(elem).html(response)
                })
        })
    } else {
        $(elem).html(lastResponse[url+JSON.stringify(json)])

        responses[elem] = lastResponse[url+JSON.stringify(json)]

    }
}
