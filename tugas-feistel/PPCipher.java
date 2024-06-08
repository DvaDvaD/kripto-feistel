import java.util.Scanner;

class PPCipher {
  private static final int NUMBER_OF_ROUNDS = 16;

  private static String IV;

  private static final int IRREDUCIBLE_POLYNOMIAL = 0x11025; // x^16 + x^12 + x^5 + x^3 + 1

  private static final int opad = 0x36;
  private static final int ipad = 0x5c;

  public static int gfMul(int a, int b) {
    int product = 0;

    String binaryB = Integer.toUnsignedString(b, 2);

    for (int i = binaryB.length() - 1; i >= 0; i--) {
      if (binaryB.charAt(i) == '1') {
        product = product ^ a;
      }
      a <<= 1;
    }

    int productMod = polyModulo(product, IRREDUCIBLE_POLYNOMIAL);

    return productMod;
  }

  public static int polyModulo(int poly, int mod) {
    int polyLength = Integer.toUnsignedString(poly, 2).length();
    int modLength = Integer.toUnsignedString(mod, 2).length();

    while (polyLength >= modLength) {
      int shift = polyLength - modLength;
      int modShift = mod << shift;
      poly = poly ^ modShift;
      polyLength = Integer.toUnsignedString(poly, 2).length();
    }

    return poly;
  }

  public static int textToInt(String text) {
    StringBuilder binaryString = new StringBuilder();
    for (char c : text.toCharArray()) {
      int asciiValue = (int) c;
      String binaryChar = String.format("%8s", Integer.toUnsignedString(asciiValue, 2)).replace(' ', '0');
      binaryString.append(binaryChar);
    }
    return Integer.parseUnsignedInt(binaryString.toString(), 2);
  }

  public static String intToAsciiString(int number) {
    StringBuilder result = new StringBuilder();
    String binaryString = String.format("%32s", Integer.toUnsignedString(number, 2)).replace(' ', '0');

    for (int i = 0; i < binaryString.length(); i += 8) {
      String binaryChar = binaryString.substring(i, i + 8);
      int asciiValue = Integer.parseUnsignedInt(binaryChar, 2);
      result.append((char) asciiValue);
    }

    return result.toString();
  }

  private static int someComplexFunction(int x, int round) {
    int a = x << 9;
    int b = x >> 7;
    int c = x ^ round;
    int d = c ^ (x << 11);
    int e = d ^ (b << 3);
    int result = a ^ e;
    return result;
  }

  public static int[] keyScheduling(int key) {
    int[] subkeys = new int[NUMBER_OF_ROUNDS];

    subkeys[0] = key & 0xffff;

    // Generate the remaining subkeys using a complex function
    for (int i = 1; i < NUMBER_OF_ROUNDS; i++) {
      subkeys[i] = ((subkeys[i - 1] ^ someComplexFunction(subkeys[i - 1], i)) + i) & 0xffff;

    }

    return subkeys;
  }

  public static int feistelCipher(String key, String text, String mode) {
    if (mode.equalsIgnoreCase("decrypt")) {
      int leftBlock = textToInt(text.substring(0, 2));
      int rightBlock = textToInt(text.substring(2, 4));

      int[] subkeys = keyScheduling(textToInt(key));

      // NUMBER_OF_ROUNDS kali iterasi
      for (int i = NUMBER_OF_ROUNDS - 1; i >= 0; i--) {
        int fFunctionResult = fFunction(rightBlock, subkeys[i]);
        int newRightBlock = leftBlock ^ fFunctionResult;
        leftBlock = rightBlock;
        rightBlock = newRightBlock;
      }

      // Penukaran leftBlock dan rightBlock pada akhir
      int temp = leftBlock;
      leftBlock = rightBlock;
      rightBlock = temp;

      // Menggabungkan leftBlock dan rightBlock
      int result = (leftBlock << 16) | rightBlock;

      return result;
    } else {
      int leftBlock = textToInt(text.substring(0, 2));
      int rightBlock = textToInt(text.substring(2, 4));

      int[] subkeys = keyScheduling(textToInt(key));

      // NUMBER_OF_ROUNDS kali iterasi
      for (int i = 0; i < NUMBER_OF_ROUNDS; i++) {
        int fFunctionResult = fFunction(rightBlock, subkeys[i]);
        int newRightBlock = leftBlock ^ fFunctionResult;
        leftBlock = rightBlock;
        rightBlock = newRightBlock;
      }

      // Penukaran leftBlock dan rightBlock pada round terakhir
      int temp = leftBlock;
      leftBlock = rightBlock;
      rightBlock = temp;

      // Menggabungkan leftBlock dan rightBlock
      int result = (leftBlock << 16) | rightBlock;

      return result;
    }
  }

  public static int fFunction(int rightBlock, int subkey) {
    int result = gfMul(gfMul((rightBlock ^ subkey), gfMul(rightBlock, subkey)), subkey);

    return result;
  }

  public static String cbcMode(String key, String text, String mode) {
    String prevBlock = IV;

    StringBuilder result = new StringBuilder();

    if (mode.equalsIgnoreCase("decrypt")) {
      String[] blocks = text.split("(?<=\\G.{8})");

      for (String block : blocks) {
        String inputBlock = block;
        int plainText = feistelCipher(key, intToAsciiString(Integer.parseUnsignedInt(block, 16)), mode);
        String plainTextAscii = intToAsciiString(plainText);

        String decryptedBlock;

        if (prevBlock.equals(IV)) {
          decryptedBlock = intToAsciiString(textToInt(plainTextAscii) ^ textToInt(IV));
        } else {
          decryptedBlock = intToAsciiString(
              textToInt(plainTextAscii) ^ textToInt(intToAsciiString(Integer.parseUnsignedInt(prevBlock, 16))));
        }

        prevBlock = inputBlock;

        result.append(decryptedBlock);
      }
    } else {
      String[] blocks = text.split("(?<=\\G.{4})");

      for (String block : blocks) {
        if (block.length() < 4) {
          block = block + "\0".repeat(4 - block.length());
        }

        String inputBlock = intToAsciiString(textToInt(block) ^ textToInt(prevBlock));
        int cipherText = feistelCipher(key, inputBlock, mode);
        String cipherTextHex = String.format("%8s", Integer.toHexString(cipherText)).replace(" ", "0");
        String cipherTextAscii = intToAsciiString(cipherText);

        prevBlock = cipherTextAscii;

        result.append(cipherTextHex);
      }
    }
    return result.toString();
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

  public static String HMAC(int key, String text) {
    String result = "";

    // Pad key if it's less than HMAC_BLOCK_SIZE with 0's
    String paddedKey = String.format("%16s", Integer.toUnsignedString(key, 2)).replace(' ', '0');

    // XOR paddedKey with ipad
    int ipadKey = Integer.parseUnsignedInt(paddedKey, 2) ^ ipad;

    // XOR paddedKey with opad
    int opadKey = Integer.parseUnsignedInt(paddedKey, 2) ^ opad;

    // Compute inner hash
    int innerHash = hashFunction(Integer.toHexString(ipadKey) + text);

    // Compute outer hash
    int outerHash = hashFunction(Integer.toHexString(opadKey) + Integer.toHexString(innerHash));

    // Convert outer hash to hexadecimal including leading zeros
    result = String.format("%4s",Integer.toHexString(outerHash)).replace(' ', '0');

    return result;
  }

  public static boolean verifyHMAC(int key, String text, String hmac) {
    String computedHMAC = HMAC(key, text);

    System.out.println("Computed HMAC: " + computedHMAC);
    System.out.println("Attached HMAC: " + hmac);

    return computedHMAC.equals(hmac);
  }

  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
    System.out.print("Enter text: ");
    String text = scanner.nextLine();

    if (text.length() == 0) {
      System.out.println("Text must not be empty");
      scanner.close();
      return;
    }

    System.out.print("Enter key (4 chars): ");
    String key = scanner.nextLine();

    if (key.length() != 4) {
      System.out.println("Key must be 4 characters long");
      scanner.close();
      return;
    }

    System.out.print("Enter initialization vector (4 chars): ");
    IV = scanner.nextLine();

    if (IV.length() != 4) {
      System.out.println("IV must be 4 characters long");
      scanner.close();
      return;
    }

    System.out.print("Enter mode, default is encrypt (encrypt/decrypt): ");
    String mode = scanner.nextLine();

    String result = cbcMode(key, text, mode);
    System.out.println("Result: " + result);
    
    System.out.print("Enter key for HMAC (<= 2 chars): ");
    String hmacKey = scanner.nextLine();

    scanner.close();

    if (!(hmacKey.length() <= 2)) {
      System.out.println("HMAC key must be less than or equal to 2 characters long");
      scanner.close();
      return;
    }

    String hmacResult = HMAC(textToInt(hmacKey), result);
    System.out.println("HMAC: " + hmacResult);

    System.out.println("Final result: " + result + "." + hmacResult);

    // Verify the HMAC
    boolean hmacVerification = verifyHMAC(textToInt(hmacKey), result, hmacResult);
    System.out.println("HMAC verification: " + hmacVerification);
  }
}