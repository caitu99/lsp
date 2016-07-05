package com.caitu99.lsp.utils;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class HtmlHelper {

    public static StringBuilder xHex2Html(InputStream stream) throws IOException {
        StringBuilder sb = new StringBuilder();
        int b;
        int bn;
        int c1, c2;
        String c;
        while ((b = stream.read()) != -1) {
            if (b == (int) '\\') {
                bn = stream.read();
                if (bn == (int) 'x') {
                    String tmp = "";

                    c1 = stream.read();
                    tmp += (char) c1;
                    c2 = stream.read();
                    tmp += (char) c2;
                    c = Character.toString((char) Integer.parseInt(tmp, 16));
                    sb.append(c);
                } else {
                    if (bn == -1) {
                        sb.append((char) b);
                    } else {
                        sb.append((char) b);
                        sb.append((char) bn);
                    }
                }
            } else {
                sb.append((char) b);
            }
        }
        return sb;
    }

    public static StringBuilder html2UHex(StringBuilder stringBuilder) throws IOException {
        StringBuilder sb = new StringBuilder();
        InputStream stream = new ByteArrayInputStream(stringBuilder.toString().getBytes());
        int h1, h2;
        while ((h1 = stream.read()) != -1) {
            if (h1 == (int) '&') {
                h2 = stream.read();
                if (h2 == (int) '#') {
                    String tmp = "";
                    int t;
                    boolean flag = true;
                    while ((t = stream.read()) != (int) ';') {
                        tmp += (char) t;
                        if (t < (int) '0' || t > (int) '9') {
                            flag = false;
                            break;
                        }
                    }
                    if (flag) {
                        int c = Integer.parseInt(tmp, 10);
                        if (c > 9830) {
                            sb.append("\\u").append(int2Hex(c));
                        } else {
                            sb.append(Character.toString((char) c));
                        }
                    } else {
                        sb.append(tmp);
                    }
                } else {
                    sb.append((char) h1);
                    sb.append((char) h2);
                }
            } else {
                sb.append((char) h1);
            }
        }
        return sb;
    }

    public static StringBuilder unicodeDecoded(StringBuilder stringBuilder) throws IOException {
        StringBuilder sb = new StringBuilder();
        InputStream stream = new ByteArrayInputStream(stringBuilder.toString().getBytes());
        int h1, h2;
        while ((h1 = stream.read()) != -1) {
            if (h1 == (int) '\\') {
                h2 = stream.read();
                if (h2 == (int) 'u') {
                    String tmp = "";
                    int t;
                    for (int i = 0; i < 4; ++i) {
                        t = stream.read();
                        tmp += (char) t;
                    }
                    if (tmp.length() > 0) {
                        int c = Integer.parseInt(tmp, 16);
                        sb.append(Character.toString((char) c));
                    }
                } else {
                    sb.append((char) h1);
                    sb.append((char) h2);
                }
            } else {
                sb.append((char) h1);
            }
        }
        return sb;
    }

    private static String int2Hex(int i) {
        String result = Integer.toHexString(i);
        if (result.length() == 1) {
            result = "0" + result;
        }
        return result;
    }

}
