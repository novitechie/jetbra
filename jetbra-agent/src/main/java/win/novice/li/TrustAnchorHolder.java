package win.novice.li;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Paths;
import java.security.cert.CertificateFactory;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.Set;

public class TrustAnchorHolder {
    public static Set<TrustAnchor> TRUST_ANCHORS;


    public static Set<TrustAnchor> loadTrustAnchors() throws Exception {
        if (TRUST_ANCHORS != null) {
            return TRUST_ANCHORS;
        }
        TRUST_ANCHORS = new HashSet<>();

        String certDir;
        if (System.getenv("JB_HOME") != null) {
            certDir = System.getenv("JB_HOME");
        } else {
            URI jarURI = getJarURI();
            if (jarURI == null) {
                return TRUST_ANCHORS;
            }
            certDir = Paths.get(jarURI).getParent().resolve("trust-crt").toString();
        }
        System.out.println("load crt from " + certDir);
        File dir = new File(certDir);
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
                for (File item : files) {
                    if (item.getName().endsWith(".crt")) {
                        X509Certificate cert = (X509Certificate) certificateFactory.generateCertificate(new FileInputStream(item));
                        TRUST_ANCHORS.add(new TrustAnchor(cert, null));
                    }
                }
            }
        }
        System.out.println("loaded "  + TRUST_ANCHORS.size() +  " crts");
        return TRUST_ANCHORS;
    }

    public static URI getJarURI() throws Exception {
        URL url = TrustAnchorHolder.class.getProtectionDomain().getCodeSource().getLocation();
        if (null != url) {
            return url.toURI();
        }
        String resourcePath = "/jarLocation.txt";
        url = TrustAnchorHolder.class.getResource(resourcePath);
        if (null == url) {
            return null;
        }
        String path = url.getPath();
        path = path.substring(0, path.length() - resourcePath.length() - 1);
        return new URI(path);
    }
}
