window = {};
(function () {
    var d = {};
    d.PADCHAR = "=";
    d.ALPHA = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
    d.getbyte = function (D, C) {
        var B = D.charCodeAt(C);
        if (B > 255) {
            throw "INVALID_CHARACTER_ERR: DOM Exception 5"
        }
        return B
    };
    d.encode = function (F) {
        if (arguments.length != 1) {
            throw "SyntaxError: Not enough arguments"
        }
        var C = d.PADCHAR;
        var H = d.ALPHA;
        var G = d.getbyte;
        var E, I;
        var B = [];
        F = "" + F;
        var D = F.length - F.length % 3;
        if (F.length == 0) {
            return F
        }
        for (E = 0; E < D; E += 3) {
            I = (G(F, E) << 16) | (G(F, E + 1) << 8) | G(F, E + 2);
            B.push(H.charAt(I >> 18));
            B.push(H.charAt((I >> 12) & 63));
            B.push(H.charAt((I >> 6) & 63));
            B.push(H.charAt(I & 63))
        }
        switch (F.length - D) {
            case 1:
                I = G(F, E) << 16;
                B.push(H.charAt(I >> 18) + H.charAt((I >> 12) & 63) + C + C);
                break;
            case 2:
                I = (G(F, E) << 16) | (G(F, E + 1) << 8);
                B.push(H.charAt(I >> 18) + H.charAt((I >> 12) & 63)
                    + H.charAt((I >> 6) & 63) + C);
                break
        }
        return B.join("")
    };
    if (!window.btoa) {
        window.btoa = d.encode
    }
})();