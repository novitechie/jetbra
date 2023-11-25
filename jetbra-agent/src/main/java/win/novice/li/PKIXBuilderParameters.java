package win.novice.li;

import net.bytebuddy.asm.Advice;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.cert.TrustAnchor;
import java.util.HashSet;
import java.util.Set;

public class PKIXBuilderParameters {


    @Advice.OnMethodEnter
    @SuppressWarnings("unchecked")
    public static void intercept(@Advice.Argument(value = 0, readOnly = false) Set<TrustAnchor> trustAnchors) throws Exception {
        Class<?> clazz = Class.forName("win.novice.li.TrustAnchorHolder", true, ClassLoader.getSystemClassLoader());
        Method method = clazz.getDeclaredMethod("loadTrustAnchors");
        Set<TrustAnchor> loadedTrustAnchors = (Set<TrustAnchor>)method.invoke(null);
        HashSet<TrustAnchor> newTrustAnchors = new HashSet<>(trustAnchors);
        newTrustAnchors.addAll(loadedTrustAnchors);
        trustAnchors = newTrustAnchors;
    }
}
