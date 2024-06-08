import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class HashCollisionFinder {
    public static void main(String[] args) {
        findHashCollision();
    }

    public static void findHashCollision() {
      Map<Integer, String> hashMap = new HashMap<>();
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
  
          // Calculate hash
          int hash = hashFunction(message);
  
          if (hashMap.containsKey(hash)) {
              String previousMessage = hashMap.get(hash);
              
              // Normalize messages by removing leading zeros
              String normalizedPreviousMessage = previousMessage.replaceFirst("^0+(?!$)", "");
              String normalizedMessage = message.replaceFirst("^0+(?!$)", "");
  
              if (!normalizedPreviousMessage.equals(normalizedMessage)) {
                  System.out.println("Collision found:");
                  System.out.println("Message 1: " + previousMessage);
                  System.out.println("Message 2: " + message);
                  System.out.println("Hash: " + String.format("%4s", Integer.toHexString(hash)).replace(' ', '0'));
                  return;
              }
          } else {
              hashMap.put(hash, message);
          }
      }
  }
    public static int hashFunction(String input) {
        // Hash function using block size of 16 bits
        int hash = 0;

        // Split input into blocks of 16 bits
        String[] blocks = input.split("(?<=\\G.{4})");

        // XOR each block with the hash
        for (String block : blocks) {
            hash = hash ^ Integer.parseUnsignedInt(block, 16);
        }

        return hash;
    }
}