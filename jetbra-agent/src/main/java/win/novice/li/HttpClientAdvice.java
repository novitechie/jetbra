package win.novice.li;

import net.bytebuddy.asm.Advice;
import sun.net.www.http.HttpClient;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.SocketTimeoutException;
import java.util.Set;


public class HttpClientAdvice {
    @Advice.OnMethodExit
    @SuppressWarnings("unchecked")
    public static void intercept(@Advice.This Object httpClient) throws Exception {
        Class<?> clazz = Class.forName("win.novice.li.ConfigHelper", true, ClassLoader.getSystemClassLoader());
        Method method = clazz.getDeclaredMethod("loadBlockUrlKeywords");
        Set<String> BLOCK_URL_KEYWORDS = (Set<String>) method.invoke(null);
        String clientString = httpClient.toString();
        for (String keyword : BLOCK_URL_KEYWORDS) {
            if (clientString.contains(keyword)) {
                throw new SocketTimeoutException();
            }
        }
    }
}
