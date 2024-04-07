package com.novitechie.jetbra;

import net.bytebuddy.asm.Advice;

import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

public class LicensingFacadeAdvice {

    @Advice.OnMethodExit
    public static void intercept(@Advice.Return(readOnly = false) Date expirationDate) {
        RuntimeException e = new RuntimeException();
        Pattern pattern = Pattern.compile("\\A\\p{ASCII}*\\z");
        for (StackTraceElement stackTraceElement : e.getStackTrace()) {
            if (!pattern.matcher(stackTraceElement.getMethodName()).matches()){
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DAY_OF_MONTH, 180);
                expirationDate = calendar.getTime();
                break;
            }
        }
    }
}
