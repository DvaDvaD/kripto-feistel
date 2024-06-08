import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class HMACCollisionFinder {
    public static void main(String[] args) {
        // Example key for HMAC (must be <= 2 characters as per original code)
        String hmacKey = "aa";
        int key = PPCipher.textToInt(hmacKey);

        findHMACCollision(key);
    }

    public static void findHMACCollision(int hmacKey) {
      Map<String, String> hmacMap = new HashMap<>();
      Random random = new Random();
  
      while (true) {
          // Generate random length message with valid hexadecimal characters
          int length = random.nextInt(20) + 1;  // Message length between 1 and 20 characters
          StringBuilder messageBuilder = new StringBuilder();
          for (int i = 0; i < length; i++) {
              int hexValue = random.nextInt(16);
              messageBuilder.append(Integer.toHexString(hexValue));
          }
          String message = messageBuilder.toString();
  
          // Calculate HMAC
          String hmac = PPCipher.HMAC(hmacKey, message);
  
          if (hmacMap.containsKey(hmac)) {
            String previousMessage = hmacMap.get(hmac);
            
            // Normalize messages by removing leading zeros
            String normalizedPreviousMessage = previousMessage.replaceFirst("^0+(?!$)", "");
            String normalizedMessage = message.replaceFirst("^0+(?!$)", "");

            if (!normalizedPreviousMessage.equals(normalizedMessage)) {
                System.out.println("Collision found:");
                System.out.println("Message 1: " + previousMessage);
                System.out.println("Message 2: " + message);
                System.out.println("HMAC: " + hmac);
                return;
            }
        } else {
            hmacMap.put(hmac, message);
        }
      }
  }
}
