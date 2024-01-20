package win.novice.li;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.cert.CertificateFactory;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.Set;

public class ConfigHelper {
    public static Set<TrustAnchor> TRUST_ANCHORS;
    public static Set<String> BLOCK_URL_KEYWORDS;

    public static Set<TrustAnchor> loadTrustAnchors() throws Exception {
        if (TRUST_ANCHORS != null) {
            return TRUST_ANCHORS;
        }
        TRUST_ANCHORS = new HashSet<>();

        String certDir;
        if (System.getenv("TRUST_CRT_DIR") != null) {
            certDir = System.getenv("TRUST_CRT_DIR");
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
                        X509Certificate cert = (X509Certificate) certificateFactory.generateCertificate(Files.newInputStream(item.toPath()));
                        TRUST_ANCHORS.add(new TrustAnchor(cert, null));
                    }
                }
            }
        }
        System.out.println("loaded " + TRUST_ANCHORS.size() + " crts");
        return TRUST_ANCHORS;
    }

    public static Set<String> loadBlockUrlKeywords() throws Exception {
        if (BLOCK_URL_KEYWORDS != null) {
            return BLOCK_URL_KEYWORDS;
        }
        BLOCK_URL_KEYWORDS = new HashSet<>();
        String blockUrlKeywordFilePath;
        if (System.getenv("BLOCK_URL_KEYWORD_FILE_PATH") != null) {
            blockUrlKeywordFilePath = System.getenv("BLOCK_URL_KEYWORD_FILE_PATH");
        } else {
            URI jarURI = getJarURI();
            if (jarURI == null) {
                return BLOCK_URL_KEYWORDS;
            }
            blockUrlKeywordFilePath = Paths.get(jarURI).getParent().resolve("block_url_keywords").toString();
        }
        System.out.println("load block url keywords from " + blockUrlKeywordFilePath);
        File file = new File(blockUrlKeywordFilePath);
        if (file.exists()) {
            for (String line : Files.readAllLines(file.toPath())) {
                if (!line.trim().isEmpty()) {
                    BLOCK_URL_KEYWORDS.add(line);
                }
            }
        }
        System.out.println("loaded " + BLOCK_URL_KEYWORDS.size() + " keywords");
        return BLOCK_URL_KEYWORDS;
    }


    public static URI getJarURI() throws Exception {
        URL url = ConfigHelper.class.getProtectionDomain().getCodeSource().getLocation();
        if (null != url) {
            return url.toURI();
        }
        String resourcePath = "/jarLocation.txt";
        url = ConfigHelper.class.getResource(resourcePath);
        if (null == url) {
            return null;
        }
        String path = url.getPath();
        path = path.substring(0, path.length() - resourcePath.length() - 1);
        return new URI(path);
    }
}
