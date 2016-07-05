var JD = (function () {
    function BarrettMu(a) {
        this.modulus = biCopy(a),
            this.k = biHighIndex(this.modulus) + 1;
        var b = new BigInt;
        b.digits[2 * this.k] = 1,
            this.mu = biDivide(b, this.modulus),
            this.bkplus1 = new BigInt,
            this.bkplus1.digits[this.k + 1] = 1,
            this.modulo = BarrettMu_modulo,
            this.multiplyMod = BarrettMu_multiplyMod,
            this.powMod = BarrettMu_powMod
    }

    function BarrettMu_modulo(a) {
        var b = biDivideByRadixPower(a, this.k - 1)
            , c = biMultiply(b, this.mu)
            , d = biDivideByRadixPower(c, this.k + 1)
            , e = biModuloByRadixPower(a, this.k + 1)
            , f = biMultiply(d, this.modulus)
            , g = biModuloByRadixPower(f, this.k + 1)
            , h = biSubtract(e, g);
        h.isNeg && (h = biAdd(h, this.bkplus1));
        for (var i = biCompare(h, this.modulus) >= 0; i;)
            h = biSubtract(h, this.modulus),
                i = biCompare(h, this.modulus) >= 0;
        return h
    }

    function BarrettMu_multiplyMod(a, b) {
        var c = biMultiply(a, b);
        return this.modulo(c)
    }

    function BarrettMu_powMod(a, b) {
        var c = new BigInt;
        c.digits[0] = 1;
        for (var d = a, e = b; ;) {
            if (0 != (1 & e.digits[0]) && (c = this.multiplyMod(c, d)),
                    e = biShiftRight(e, 1),
                0 == e.digits[0] && 0 == biHighIndex(e))
                break;
            d = this.multiplyMod(d, d)
        }
        return c
    }

    function setMaxDigits(a) {
        maxDigits = a,
            ZERO_ARRAY = new Array(maxDigits);
        for (var b = 0; b < ZERO_ARRAY.length; b++)
            ZERO_ARRAY[b] = 0;
        bigZero = new BigInt,
            bigOne = new BigInt,
            bigOne.digits[0] = 1
    }

    function BigInt(a) {
        this.digits = "boolean" == typeof a && 1 == a ? null : ZERO_ARRAY.slice(0),
            this.isNeg = !1
    }

    function biFromDecimal(a) {
        for (var b, c = "-" == a.charAt(0), d = c ? 1 : 0; d < a.length && "0" == a.charAt(d);)
            ++d;
        if (d == a.length)
            b = new BigInt;
        else {
            var e = a.length - d
                , f = e % dpl10;
            for (0 == f && (f = dpl10),
                     b = biFromNumber(Number(a.substr(d, f))),
                     d += f; d < a.length;)
                b = biAdd(biMultiply(b, lr10), biFromNumber(Number(a.substr(d, dpl10)))),
                    d += dpl10;
            b.isNeg = c
        }
        return b
    }

    function biCopy(a) {
        var b = new BigInt(!0);
        return b.digits = a.digits.slice(0),
            b.isNeg = a.isNeg,
            b
    }

    function biFromNumber(a) {
        var b = new BigInt;
        b.isNeg = 0 > a,
            a = Math.abs(a);
        for (var c = 0; a > 0;)
            b.digits[c++] = a & maxDigitVal,
                a >>= biRadixBits;
        return b
    }

    function reverseStr(a) {
        for (var b = "", c = a.length - 1; c > -1; --c)
            b += a.charAt(c);
        return b
    }

    function biToString(a, b) {
        var c = new BigInt;
        c.digits[0] = b;
        for (var d = biDivideModulo(a, c), e = hexatrigesimalToChar[d[1].digits[0]]; 1 == biCompare(d[0], bigZero);)
            d = biDivideModulo(d[0], c),
                digit = d[1].digits[0],
                e += hexatrigesimalToChar[d[1].digits[0]];
        return (a.isNeg ? "-" : "") + reverseStr(e)
    }

    function biToDecimal(a) {
        var b = new BigInt;
        b.digits[0] = 10;
        for (var c = biDivideModulo(a, b), d = String(c[1].digits[0]); 1 == biCompare(c[0], bigZero);)
            c = biDivideModulo(c[0], b),
                d += String(c[1].digits[0]);
        return (a.isNeg ? "-" : "") + reverseStr(d)
    }

    function digitToHex(a) {
        var b = 15
            , c = "";
        for (i = 0; i < 4; ++i)
            c += hexToChar[a & b],
                a >>>= 4;
        return reverseStr(c)
    }

    function biToHex(a) {
        for (var b = "", c = (biHighIndex(a),
            biHighIndex(a)); c > -1; --c)
            b += digitToHex(a.digits[c]);
        return b
    }

    function charToHex(a) {
        var b, c = 48, d = c + 9, e = 97, f = e + 25, g = 65, h = 90;
        return b = a >= c && d >= a ? a - c : a >= g && h >= a ? 10 + a - g : a >= e && f >= a ? 10 + a - e : 0
    }

    function hexToDigit(a) {
        for (var b = 0, c = Math.min(a.length, 4), d = 0; c > d; ++d)
            b <<= 4,
                b |= charToHex(a.charCodeAt(d));
        return b
    }

    function biFromHex(a) {
        for (var b = new BigInt, c = a.length, d = c, e = 0; d > 0; d -= 4,
            ++e)
            b.digits[e] = hexToDigit(a.substr(Math.max(d - 4, 0), Math.min(d, 4)));
        return b
    }

    function biFromString(a, b) {
        var c = "-" == a.charAt(0)
            , d = c ? 1 : 0
            , e = new BigInt
            , f = new BigInt;
        f.digits[0] = 1;
        for (var g = a.length - 1; g >= d; g--) {
            var h = a.charCodeAt(g)
                , i = charToHex(h)
                , j = biMultiplyDigit(f, i);
            e = biAdd(e, j),
                f = biMultiplyDigit(f, b)
        }
        return e.isNeg = c,
            e
    }

    function biToBytes(a) {
        for (var b = "", c = biHighIndex(a); c > -1; --c)
            b += digitToBytes(a.digits[c]);
        return b
    }

    function digitToBytes(a) {
        var b = String.fromCharCode(255 & a);
        a >>>= 8;
        var c = String.fromCharCode(255 & a);
        return c + b
    }

    function biDump(a) {
        return (a.isNeg ? "-" : "") + a.digits.join(" ")
    }

    function biAdd(a, b) {
        var c;
        if (a.isNeg != b.isNeg)
            b.isNeg = !b.isNeg,
                c = biSubtract(a, b),
                b.isNeg = !b.isNeg;
        else {
            c = new BigInt;
            for (var d, e = 0, f = 0; f < a.digits.length; ++f)
                d = a.digits[f] + b.digits[f] + e,
                    c.digits[f] = 65535 & d,
                    e = Number(d >= biRadix);
            c.isNeg = a.isNeg
        }
        return c
    }

    function biSubtract(a, b) {
        var c;
        if (a.isNeg != b.isNeg)
            b.isNeg = !b.isNeg,
                c = biAdd(a, b),
                b.isNeg = !b.isNeg;
        else {
            c = new BigInt;
            var d, e;
            e = 0;
            for (var f = 0; f < a.digits.length; ++f)
                d = a.digits[f] - b.digits[f] + e,
                    c.digits[f] = 65535 & d,
                c.digits[f] < 0 && (c.digits[f] += biRadix),
                    e = 0 - Number(0 > d);
            if (-1 == e) {
                e = 0;
                for (var f = 0; f < a.digits.length; ++f)
                    d = 0 - c.digits[f] + e,
                        c.digits[f] = 65535 & d,
                    c.digits[f] < 0 && (c.digits[f] += biRadix),
                        e = 0 - Number(0 > d);
                c.isNeg = !a.isNeg
            } else
                c.isNeg = a.isNeg
        }
        return c
    }

    function biHighIndex(a) {
        for (var b = a.digits.length - 1; b > 0 && 0 == a.digits[b];)
            --b;
        return b
    }

    function biNumBits(a) {
        var b, c = biHighIndex(a), d = a.digits[c], e = (c + 1) * bitsPerDigit;
        for (b = e; b > e - bitsPerDigit && 0 == (32768 & d); --b)
            d <<= 1;
        return b
    }

    function biMultiply(a, b) {
        for (var c, d, e, f = new BigInt, g = biHighIndex(a), h = biHighIndex(b), i = 0; h >= i; ++i) {
            for (c = 0,
                     e = i,
                     j = 0; j <= g; ++j,
                     ++e)
                d = f.digits[e] + a.digits[j] * b.digits[i] + c,
                    f.digits[e] = d & maxDigitVal,
                    c = d >>> biRadixBits;
            f.digits[i + g + 1] = c
        }
        return f.isNeg = a.isNeg != b.isNeg,
            f
    }

    function biMultiplyDigit(a, b) {
        var c, d, e;
        result = new BigInt,
            c = biHighIndex(a),
            d = 0;
        for (var f = 0; c >= f; ++f)
            e = result.digits[f] + a.digits[f] * b + d,
                result.digits[f] = e & maxDigitVal,
                d = e >>> biRadixBits;
        return result.digits[1 + c] = d,
            result
    }

    function arrayCopy(a, b, c, d, e) {
        for (var f = Math.min(b + e, a.length), g = b, h = d; f > g; ++g,
            ++h)
            c[h] = a[g]
    }

    function biShiftLeft(a, b) {
        var c = Math.floor(b / bitsPerDigit)
            , d = new BigInt;
        arrayCopy(a.digits, 0, d.digits, c, d.digits.length - c);
        for (var e = b % bitsPerDigit, f = bitsPerDigit - e, g = d.digits.length - 1, h = g - 1; g > 0; --g,
            --h)
            d.digits[g] = d.digits[g] << e & maxDigitVal | (d.digits[h] & highBitMasks[e]) >>> f;
        return d.digits[0] = d.digits[g] << e & maxDigitVal,
            d.isNeg = a.isNeg,
            d
    }

    function biShiftRight(a, b) {
        var c = Math.floor(b / bitsPerDigit)
            , d = new BigInt;
        arrayCopy(a.digits, c, d.digits, 0, a.digits.length - c);
        for (var e = b % bitsPerDigit, f = bitsPerDigit - e, g = 0, h = g + 1; g < d.digits.length - 1; ++g,
            ++h)
            d.digits[g] = d.digits[g] >>> e | (d.digits[h] & lowBitMasks[e]) << f;
        return d.digits[d.digits.length - 1] >>>= e,
            d.isNeg = a.isNeg,
            d
    }

    function biMultiplyByRadixPower(a, b) {
        var c = new BigInt;
        return arrayCopy(a.digits, 0, c.digits, b, c.digits.length - b),
            c
    }

    function biDivideByRadixPower(a, b) {
        var c = new BigInt;
        return arrayCopy(a.digits, b, c.digits, 0, c.digits.length - b),
            c
    }

    function biModuloByRadixPower(a, b) {
        var c = new BigInt;
        return arrayCopy(a.digits, 0, c.digits, 0, b),
            c
    }

    function biCompare(a, b) {
        if (a.isNeg != b.isNeg)
            return 1 - 2 * Number(a.isNeg);
        for (var c = a.digits.length - 1; c >= 0; --c)
            if (a.digits[c] != b.digits[c])
                return a.isNeg ? 1 - 2 * Number(a.digits[c] > b.digits[c]) : 1 - 2 * Number(a.digits[c] < b.digits[c]);
        return 0
    }

    function biDivideModulo(a, b) {
        var c, d, e = biNumBits(a), f = biNumBits(b), g = b.isNeg;
        if (f > e)
            return a.isNeg ? (c = biCopy(bigOne),
                c.isNeg = !b.isNeg,
                a.isNeg = !1,
                b.isNeg = !1,
                d = biSubtract(b, a),
                a.isNeg = !0,
                b.isNeg = g) : (c = new BigInt,
                d = biCopy(a)),
                new Array(c, d);
        c = new BigInt,
            d = a;
        for (var h = Math.ceil(f / bitsPerDigit) - 1, i = 0; b.digits[h] < biHalfRadix;)
            b = biShiftLeft(b, 1),
                ++i,
                ++f,
                h = Math.ceil(f / bitsPerDigit) - 1;
        d = biShiftLeft(d, i),
            e += i;
        for (var j = Math.ceil(e / bitsPerDigit) - 1, k = biMultiplyByRadixPower(b, j - h); -1 != biCompare(d, k);)
            ++c.digits[j - h],
                d = biSubtract(d, k);
        for (var l = j; l > h; --l) {
            var m = l >= d.digits.length ? 0 : d.digits[l]
                , n = l - 1 >= d.digits.length ? 0 : d.digits[l - 1]
                , o = l - 2 >= d.digits.length ? 0 : d.digits[l - 2]
                , p = h >= b.digits.length ? 0 : b.digits[h]
                , q = h - 1 >= b.digits.length ? 0 : b.digits[h - 1];
            c.digits[l - h - 1] = m == p ? maxDigitVal : Math.floor((m * biRadix + n) / p);
            for (var r = c.digits[l - h - 1] * (p * biRadix + q), s = m * biRadixSquared + (n * biRadix + o); r > s;)
                --c.digits[l - h - 1],
                    r = c.digits[l - h - 1] * (p * biRadix | q),
                    s = m * biRadix * biRadix + (n * biRadix + o);
            k = biMultiplyByRadixPower(b, l - h - 1),
                d = biSubtract(d, biMultiplyDigit(k, c.digits[l - h - 1])),
            d.isNeg && (d = biAdd(d, k),
                --c.digits[l - h - 1])
        }
        return d = biShiftRight(d, i),
            c.isNeg = a.isNeg != g,
        a.isNeg && (c = g ? biAdd(c, bigOne) : biSubtract(c, bigOne),
            b = biShiftRight(b, i),
            d = biSubtract(b, d)),
        0 == d.digits[0] && 0 == biHighIndex(d) && (d.isNeg = !1),
            new Array(c, d)
    }

    function biDivide(a, b) {
        return biDivideModulo(a, b)[0]
    }

    function biModulo(a, b) {
        return biDivideModulo(a, b)[1]
    }

    function biMultiplyMod(a, b, c) {
        return biModulo(biMultiply(a, b), c)
    }

    function biPow(a, b) {
        for (var c = bigOne, d = a; ;) {
            if (0 != (1 & b) && (c = biMultiply(c, d)),
                    b >>= 1,
                0 == b)
                break;
            d = biMultiply(d, d)
        }
        return c
    }

    function biPowMod(a, b, c) {
        for (var d = bigOne, e = a, f = b; ;) {
            if (0 != (1 & f.digits[0]) && (d = biMultiplyMod(d, e, c)),
                    f = biShiftRight(f, 1),
                0 == f.digits[0] && 0 == biHighIndex(f))
                break;
            e = biMultiplyMod(e, e, c)
        }
        return d
    }

    function RSAKeyPair(a, b, c, d) {
        this.e = biFromHex(a),
            this.d = biFromHex(b),
            this.m = biFromHex(c),
            this.chunkSize = "number" != typeof d ? 2 * biHighIndex(this.m) : d / 8,
            this.radix = 16,
            this.barrett = new BarrettMu(this.m)
    }

    function encryptedString(a, b, c, d) {
        var e, f, g, h, i, j, k, l, m, n, o = new Array, p = b.length, q = "";
        for (h = "string" == typeof c ? c == RSAAPP.NoPadding ? 1 : c == RSAAPP.PKCS1Padding ? 2 : 0 : 0,
                 i = "string" == typeof d && d == RSAAPP.RawEncoding ? 1 : 0,
                 1 == h ? p > a.chunkSize && (p = a.chunkSize) : 2 == h && p > a.chunkSize - 11 && (p = a.chunkSize - 11),
                 e = 0,
                 f = 2 == h ? p - 1 : a.chunkSize - 1; p > e;)
            h ? o[f] = b.charCodeAt(e) : o[e] = b.charCodeAt(e),
                e++,
                f--;
        for (1 == h && (e = 0),
                 f = a.chunkSize - p % a.chunkSize; f > 0;) {
            if (2 == h) {
                for (j = Math.floor(256 * Math.random()); !j;)
                    j = Math.floor(256 * Math.random());
                o[e] = j
            } else
                o[e] = 0;
            e++,
                f--
        }
        for (2 == h && (o[p] = 0,
            o[a.chunkSize - 2] = 2,
            o[a.chunkSize - 1] = 0),
                 k = o.length,
                 e = 0; k > e; e += a.chunkSize) {
            for (l = new BigInt,
                     f = 0,
                     g = e; g < e + a.chunkSize; ++f)
                l.digits[f] = o[g++],
                    l.digits[f] += o[g++] << 8;
            m = a.barrett.powMod(l, a.e),
                n = 1 == i ? biToBytes(m) : 16 == a.radix ? biToHex(m) : biToString(m, a.radix),
                q += n
        }
        return q
    }

    function decryptedString(a, b) {
        var c, d, e, f, g = b.split(" "), h = "";
        for (d = 0; d < g.length; ++d)
            for (f = 16 == a.radix ? biFromHex(g[d]) : biFromString(g[d], a.radix),
                     c = a.barrett.powMod(f, a.d),
                     e = 0; e <= biHighIndex(c); ++e)
                h += String.fromCharCode(255 & c.digits[e], c.digits[e] >> 8);
        return 0 == h.charCodeAt(h.length - 1) && (h = h.substring(0, h.length - 1)),
            h
    }

    var biRadixBase = 2, biRadixBits = 16, bitsPerDigit = biRadixBits, biRadix = 65536, biHalfRadix = biRadix >>> 1, biRadixSquared = biRadix * biRadix, maxDigitVal = biRadix - 1, maxInteger = 9999999999999998, maxDigits, ZERO_ARRAY, bigZero, bigOne;
    setMaxDigits(20);
    var dpl10 = 15
        , lr10 = biFromNumber(1e15)
        , hexatrigesimalToChar = new Array("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z")
        , hexToChar = new Array("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f")
        , highBitMasks = new Array(0, 32768, 49152, 57344, 61440, 63488, 64512, 65024, 65280, 65408, 65472, 65504, 65520, 65528, 65532, 65534, 65535)
        , lowBitMasks = new Array(0, 1, 3, 7, 15, 31, 63, 127, 255, 511, 1023, 2047, 4095, 8191, 16383, 32767, 65535)
        , RSAAPP = {};
    RSAAPP.NoPadding = "NoPadding",
        RSAAPP.PKCS1Padding = "PKCS1Padding",
        RSAAPP.RawEncoding = "RawEncoding",
        RSAAPP.NumericEncoding = "NumericEncoding",
        function () {
            function a(a) {
                var b = new Image
                    , c = "";
                for (var d in a)
                    c += "&" + d + "=" + encodeURIComponent(a[d]);
                c = "https://wlmonitor.m.jd.com/web_login_report?" + c.substring(1),
                    b.src = c
            }

            function b(a, c) {
                if ("object" == typeof c && null != c)
                    for (var d in c)
                        "object" == typeof c[d] ? (a[d] = c[d].length ? [] : {},
                            b(a[d], c[d])) : a[d] = c[d]
            }

            function c(a) {
                for (var b = location.search.substring(1), c = b.split("&"), d = {}, e = 0; e < c.length; e++) {
                    var f = c[e].split("=");
                    d[f[0]] = f[1]
                }
                return d[a] ? d[a] : ""
            }

            function d(a) {
                var b = document.cookie.match(new RegExp("(^| )" + a + "=([^;]*)($|;)"));
                return b ? decodeURIComponent(b[2]) : ""
            }

            var e = function (e) {
                var f = c("appid")
                    , g = d("guid")
                    , h = d("pin")
                    , i = {
                        appID: f ? parseInt(f, 10) : 100,
                        interfaceID: 0,
                        loginName: "",
                        uuid: g,
                        pin: h,
                        guid: g,
                        os: "5",
                        netType: "",
                        appVersion: "1.3.0",
                        status: "",
                        callTime: 0
                    };
                b(i, e),
                    a(i)
            }
            window.pl_report = e

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
                        B.push(H.charAt(I >> 18) + H.charAt((I >> 12) & 63) + H.charAt((I >> 6) & 63) + C);
                        break
                }
                return B.join("")
            };
            if (!window.btoa) {
                window.btoa = d.encode
            }
        }(window);

    return {
        RSAKeyPair: RSAKeyPair,
        setMaxDigits: setMaxDigits,
        encryptedString: encryptedString,
        RSAAPP: RSAAPP
    }
})();

function getJDEncryption(pwd, rsaValue) {
    JD.setMaxDigits(131);
    var key = new JD.RSAKeyPair("3", "10001", rsaValue, 1024);
    return window.btoa(JD.encryptedString(key, pwd, JD.RSAAPP.PKCS1Padding, JD.RSAAPP.RawEncoding));
}
