package win.novice.li;

import net.bytebuddy.asm.Advice;

import java.util.Objects;

public class SystemAdvice {


    //  System.getProperty
    @Advice.OnMethodExit
    public static void intercept(@Advice.Argument(0) Object x, @Advice.Return(readOnly = false) String r) throws Exception {
        if (Objects.equals(x, "jb.vmOptionsFile")) {
            RuntimeException exception = new RuntimeException();
            int nullCnt = 0;
            boolean hasReflect = false;
            for (StackTraceElement element : exception.getStackTrace()) {
                if (element.getFileName() == null) {
                    nullCnt += 1;
                    continue;
                }
                if (element.getFileName().equals("Method.java")) {
                    hasReflect = true;
                }
            }
            if (nullCnt >= 3 && hasReflect) {
                r = null;
            }
        }
    }
}
