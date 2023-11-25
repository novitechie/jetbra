package win.novice.li;

import net.bytebuddy.asm.Advice;

import java.net.SocketTimeoutException;

public class HttpClientAdvice {
    @Advice.OnMethodExit
    public static void intercept(@Advice.This Object x) throws Exception {
        if (x.toString().contains("validateKey.action")){
            throw new SocketTimeoutException();
        }
    }
}
