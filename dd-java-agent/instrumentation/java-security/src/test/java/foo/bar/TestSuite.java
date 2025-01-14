package foo.bar;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestSuite {
  private static final Logger log = LoggerFactory.getLogger(TestSuite.class);

  public Cipher getCipherInstance(String algo)
      throws NoSuchPaddingException, NoSuchAlgorithmException {
    log.debug("Before Cipher.getInstance");
    Cipher c = Cipher.getInstance(algo);
    log.debug("after Cipher.getInstance");
    return c;
  }

  public Cipher getCipherInstance(String algo, String provider)
      throws NoSuchPaddingException, NoSuchAlgorithmException, NoSuchProviderException {
    log.debug("Before Cipher.getInstance");
    Cipher c = Cipher.getInstance(algo, provider);
    log.debug("after Cipher.getInstance");
    return c;
  }

  public Cipher getCipherInstance(String algo, Provider provider)
      throws NoSuchPaddingException, NoSuchAlgorithmException {
    log.debug("Before Cipher.getInstance");
    Cipher c = Cipher.getInstance(algo, provider);
    log.debug("after Cipher.getInstance");
    return c;
  }

  public MessageDigest getMessageDigestInstance(String algo) throws NoSuchAlgorithmException {
    log.debug("before MessageDigest.getInstance");
    MessageDigest md = MessageDigest.getInstance(algo);
    log.debug("after MessageDigest.getInstance");
    return md;
  }

  public MessageDigest getMessageDigestInstance(String algo, String provider)
      throws NoSuchAlgorithmException, NoSuchProviderException {
    log.debug("before MessageDigest.getInstance");
    MessageDigest md = MessageDigest.getInstance(algo, provider);
    log.debug("after MessageDigest.getInstance");
    return md;
  }

  public MessageDigest getMessageDigestInstance(String algo, Provider provider)
      throws NoSuchAlgorithmException {
    log.debug("before MessageDigest.getInstance");
    MessageDigest md = MessageDigest.getInstance(algo, provider);
    log.debug("after MessageDigest.getInstance");
    return md;
  }
}
