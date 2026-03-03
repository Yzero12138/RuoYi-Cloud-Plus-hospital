package org.dromara.datacenter.hospitalqc.util;

import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.datacenter.hospitalqc.config.HospitalQcProperties;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/**
 * Password encrypt/decrypt utility for datasource secret storage.
 */
@Component
public class HospitalQcPasswordCrypto {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH = 128;

    private final HospitalQcProperties properties;

    public HospitalQcPasswordCrypto(HospitalQcProperties properties) {
        this.properties = properties;
    }

    public String encrypt(String plainText) {
        if (StringUtils.isBlank(plainText)) {
            throw new ServiceException("密码不能为空");
        }
        try {
            byte[] iv = new byte[IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, buildKeySpec(), new GCMParameterSpec(TAG_LENGTH, iv));
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            byte[] payload = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, payload, 0, iv.length);
            System.arraycopy(encrypted, 0, payload, iv.length, encrypted.length);
            return Base64.getEncoder().encodeToString(payload);
        } catch (Exception ex) {
            throw new ServiceException("密码加密失败");
        }
    }

    public String decrypt(String cipherText) {
        if (StringUtils.isBlank(cipherText)) {
            return "";
        }
        try {
            byte[] payload = Base64.getDecoder().decode(cipherText);
            if (payload.length <= IV_LENGTH) {
                throw new ServiceException("密码密文无效");
            }
            byte[] iv = Arrays.copyOfRange(payload, 0, IV_LENGTH);
            byte[] encrypted = Arrays.copyOfRange(payload, IV_LENGTH, payload.length);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, buildKeySpec(), new GCMParameterSpec(TAG_LENGTH, iv));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new ServiceException("密码解密失败");
        }
    }

    public String mask(String cipherText) {
        if (StringUtils.isBlank(cipherText)) {
            return "";
        }
        return "********";
    }

    private SecretKeySpec buildKeySpec() {
        String secret = StringUtils.defaultIfBlank(properties.getAesSecret(), "hospital-qc-default-secret");
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(secret.getBytes(StandardCharsets.UTF_8));
            byte[] key = Arrays.copyOf(hash, 16);
            return new SecretKeySpec(key, ALGORITHM);
        } catch (Exception ex) {
            throw new ServiceException("密码密钥初始化失败");
        }
    }
}

