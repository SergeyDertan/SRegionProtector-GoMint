package Sergey_Dertan.SRegionProtector.Utils;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.*;
import java.util.regex.Pattern;

public abstract class Utils {

    private static final Random RANDOM = new SecureRandom();
    private static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final String SALT = "AjzzdaASd341Fdsf";
    private static final int ITERATIONS = 10000; //TODO
    private static final int KEY_LENGTH = 256;

    private Utils() {
    }

    /*---------------- encryption ----------------*/
    public static String createSalt(int length) {
        StringBuilder returnValue = new StringBuilder(length);
        for (int i = 0; i < length; ++i) {
            returnValue.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return new String(returnValue);
    }

    public static byte[] hash(char[] password, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
        Arrays.fill(password, Character.MIN_VALUE);
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            return skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new AssertionError("Error while encrypting a string: " + e.getMessage(), e);
        } finally {
            spec.clearPassword();
        }
    }

    public static String encryptString(String password, String salt) {
        byte[] securePassword = hash(password.toCharArray(), salt.getBytes());
        return Base64.getEncoder().encodeToString(securePassword);
    }

    public static String encryptString(String password) {
        return encryptString(password, SALT);
    }

    public static boolean verifyString(String string, String encryptedString, String salt) {
        String newSecurePassword = encryptString(string, salt);
        return newSecurePassword.equalsIgnoreCase(encryptedString); //TODO ignore case
    }

    public static boolean verifyString(String string, String encryptedString) {
        return verifyString(string, encryptedString, SALT);
    }

    /*---------------- encryption end ----------------*/

    public static boolean isValidEmailAddress(String email) {
        String pattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        return Pattern.compile(pattern).matcher(email).matches();
    }

    /*---------------- serializers ----------------*/

    public static String serializeStringArray(final String[] arr) throws RuntimeException {
        try (final ByteArrayOutputStream boas = new ByteArrayOutputStream(); final ObjectOutputStream oos = new ObjectOutputStream(boas)) {
            oos.writeObject(arr);
            return Base64.getEncoder().encodeToString(boas.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String[] deserializeStringArray(final String data) throws RuntimeException {
        try (final ByteArrayInputStream bias = new ByteArrayInputStream(Base64.getDecoder().decode(data)); final ObjectInputStream ois = new ObjectInputStream(bias)) {
            return (String[]) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static String serializeBooleanArray(final boolean[] arr) {
        String[] strings = new String[arr.length];
        for (int i = 0; i < arr.length; ++i) {
            strings[i] = arr[i] ? "true" : "false";
        }
        return serializeStringArray(strings);
    }

    public static boolean[] deserializeBooleanArray(final String str) {
        String[] strings = deserializeStringArray(str);
        boolean[] arr = new boolean[strings.length];
        for (int i = 0; i < arr.length; ++i) {
            arr[i] = strings[i].equalsIgnoreCase("true");
        }
        return arr;
    }

    /*---------------- serializers end ----------------*/

    /*---------------- resources ----------------*/
    @SuppressWarnings("unchecked")
    public static void copyResource(String fileName, String sourceFolder, String targetFolder, Class clazz, boolean fixMissingContents) throws Exception {
        if (sourceFolder.charAt(sourceFolder.length() - 1) != '/') sourceFolder += '/';
        if (targetFolder.charAt(targetFolder.length() - 1) != '/') targetFolder += '/';
        File file = new File(targetFolder + fileName);
        if (!file.exists()) {
            writeFile(file, clazz.getClassLoader().getResourceAsStream(sourceFolder + fileName));
            return;
        }
        if (!fixMissingContents) return;
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(dumperOptions);
        Map<String, Object> var1 = yaml.loadAs(clazz.getClassLoader().getResourceAsStream(sourceFolder + fileName), HashMap.class);
        Map<String, Object> var4 = yaml.loadAs(clazz.getClassLoader().getResourceAsStream(sourceFolder + fileName), HashMap.class);

        boolean changed = copyMapOfMaps(var1, var4); //for messages updating;
        if (changed) {
            //TODO map of maps sort?
            LinkedHashMap<String, Object> var5 = new LinkedHashMap<>();
            var4.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEachOrdered(x -> var5.put(x.getKey(), x.getValue()));

            Yaml save = new Yaml(dumperOptions);
            writeFile(file, new ByteArrayInputStream(save.dump(var5).getBytes(StandardCharsets.UTF_8)));
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void writeFile(File file, InputStream content) throws IOException {
        if (content == null) {
            throw new IllegalArgumentException("Content must not be null");
        } else {
            if (!file.exists()) file.createNewFile();
            FileOutputStream stream = new FileOutputStream(file);
            Throwable var3 = null;

            try {
                byte[] buffer = new byte[1024];

                int length;
                while ((length = content.read(buffer)) != -1) {
                    stream.write(buffer, 0, length);
                }
            } catch (Throwable var13) {
                var3 = var13;
                throw var13;
            } finally {
                if (var3 != null) {
                    try {
                        stream.close();
                    } catch (Throwable var12) {
                        var3.addSuppressed(var12);
                    }
                } else {
                    stream.close();
                }
            }

            content.close();
        }
    }

    public static String readFile(Reader reader) throws IOException {
        try (BufferedReader br = new BufferedReader(reader)) {
            String temp;
            StringBuilder stringBuilder = new StringBuilder();
            temp = br.readLine();
            while (temp != null) {
                if (stringBuilder.length() != 0) {
                    stringBuilder.append("\n");
                }
                stringBuilder.append(temp);
                temp = br.readLine();
            }
            return stringBuilder.toString();
        }
    }

    public static String readFile(File file) throws IOException {
        return readFile(new FileReader(file));
    }

    /**
     * for the messages copying
     */
    @SuppressWarnings("unchecked")
    public static boolean copyMapOfMaps(Map<String, Object> from, Map<String, Object> to) {
        boolean changed = false;
        if (from.size() > to.size()) changed = true;
        from.forEach(to::putIfAbsent);
        Iterator<Map.Entry<String, Object>> var1 = from.entrySet().iterator();
        while (var1.hasNext()) {
            Map.Entry<String, Object> next = var1.next();
            if (next.getValue() instanceof Map) {
                boolean c = copyMapOfMaps((Map<String, Object>) next.getValue(), (Map<String, Object>) to.get(next.getKey()));
                if (!changed) changed = c;
            }
        }
        return changed;
    }

    public static void copyResource(String fileName, String sourceFolder, String targetFolder, Class clazz) throws Exception {
        copyResource(fileName, sourceFolder, targetFolder, clazz, true);
    }

    public static boolean resourceExists(String fileName, String folder, Class clazz) {
        if (folder.charAt(folder.length() - 1) != '/') folder += '/';
        return clazz.getClassLoader().getResource(folder + fileName) != null;
    }

    /*---------------- resources end ----------------*/

    @SuppressWarnings("unchecked")
    public static <T extends Cloneable> Collection<T> deepClone(Collection<T> arr) {
        Collection<T> copy = new ArrayList<>();
        for (T elem : arr) {
            copy.add((T) elem.clone());
        }
        return copy;
    }

    public static double round(double value, int precision) {
        for (int i = 0; i < precision; ++i) {
            value *= 10D;
        }
        value = Math.round(value);
        for (int i = 0; i < precision; ++i) {
            value /= 10D;
        }
        return value;
    }

    public static <T> List<List<T>> sliceArray(T[] array, int pieces, boolean keepEmpty) { //TODO complete
        List<List<T>> result = new ObjectArrayList<>();
        for (int i = 0; i < pieces; ++i) {
            result.add(new ObjectArrayList<>());
        }

        int i = 0;

        for (T obj : array) {
            if (i == pieces) i = 0;
            result.get(i).add(obj);
            ++i;
        }
        if (!keepEmpty) {
            result.removeIf(s -> s.size() == 0);
        }
        return result;
    }
}