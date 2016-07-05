var vjAcc = "";
var wrUrl = "http://c.wrating.com/";
var wrSv = 0;

function vjGetTimestamp(A) {
    return Math.round(A / 1000)
}
function vjGetKeyword(C) {
    var A = [["baidu", "wd"], ["baidu", "q1"], ["google", "q"], ["google", "as_q"], ["yahoo", "p"], ["msn", "q"], ["live", "q"], ["sogou", "query"], ["youdao", "q"], ["soso", "w"], ["zhongsou", "w"], ["zhongsou", "w1"]];
    var B = vjGetDomainFromUrl(C.toString().toLowerCase());
    var D = -1;
    var E = "";
    if (typeof(B[0]) == "undefined") {
        return ""
    }
    for (i = 0; i < A.length; i++) {
        if (B[0].indexOf("." + A[i][0] + ".") >= 0) {
            D = -1;
            D = C.indexOf("&" + A[i][1] + "=");
            if (D < 0) {
                D = C.indexOf("?" + A[i][1] + "=")
            }
            if (D >= 0) {
                E = C.substr(D + A[i][1].length + 2, C.length - (D + A[i][1].length + 2));
                D = E.indexOf("&");
                if (D >= 0) {
                    E = E.substr(0, D)
                }
                if (E == "") {
                    return ""
                } else {
                    return A[i][0] + "|" + E
                }
            }
        }
    }
    return ""
}
function vjGetDomainFromUrl(E) {
    if (E == "") {
        return false
    }
    E = E.toString().toLowerCase();
    var F = [];
    var C = E.indexOf("//") + 2;
    var B = E.substr(C, E.length - C);
    var A = B.indexOf("/");
    if (A >= 0) {
        F[0] = B.substr(0, A)
    } else {
        F[0] = B
    }
    var D = F[0].match(/[^.]+\.(com.cn|net.cn|gov.cn|cn|com|net|org|gov|cc|biz|info)+$/);
    if (D) {
        if (typeof(D[0]) != "undefined") {
            F[1] = D[0]
        }
    }
    return F
}
function vjHash(C) {
    if (!C || C == "") {
        return 0
    }
    var B = 0;
    for (var A = C.length - 1; A >= 0; A--) {
        var D = parseInt(C.charCodeAt(A));
        B = (B << 5) + B + D
    }
    return B
}

function vjSurveyCheck() {
    if (wrSv <= 0) {
        return
    }
    var C = new Date();
    var A = C.getTime();
    var D = Math.random(A);
    if (D <= parseFloat(1 / wrSv)) {
        var B = document.createElement("script");
        B.type = "text/javascript";
        B.id = "wratingSuevey";
        B.src = "http://tongji.wrating.com/survey/check.php?c=" + vjAcc;
        document.getElementsByTagName("head")[0].appendChild(B)
    }
}
function uuid() {
    var d = new Date().getTime();
    var uuid = 'xxxxxxxxxxxx4xxxyxxxxxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
        var r = (d + Math.random() * 16) % 16 | 0;
        d = Math.floor(d / 16);
        return (c == 'x' ? r : (r & 0x7 | 0x8)).toString(16);
    });
    return uuid;
}
function getCof(){
	var co_f="2";
	var dCur=new Date();
	var cur=dCur.getTime().toString();
	for (var i=2;i<=(32-cur.length);i++){
	        co_f+=Math.floor(Math.random()*16.0).toString(16);
	}
	co_f+=cur;
	return co_f;
}
function toString16(v){
	return v.toString(16);
};