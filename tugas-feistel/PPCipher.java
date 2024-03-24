import java.util.Scanner;

class PPCipher {
  private static final int NUMBER_OF_ROUNDS = 16;

  private static final String IV = "abcd";

  private static final int IRREDUCIBLE_POLYNOMIAL = 0x1000b; // x^16 + x^12 + x^5 + x^3 + 1

  public static int gfMul(int a, int b) {
    int product = 0;
    while (b != 0) {
      if ((b & 1) != 0) {
        product ^= a;
      }
      a = gfShiftLeft(a);
      b >>>= 1;
    }
    return product;
  }

  private static int gfShiftLeft(int a) {
    if ((a & 0x8000) != 0) {
      return (a << 1) ^ IRREDUCIBLE_POLYNOMIAL;
    } else {
      return a << 1;
    }
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

  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
    System.out.print("Enter text: ");
    String text = scanner.nextLine();

    System.out.print("Enter key (4 chars): ");
    String key = scanner.nextLine();

    System.out.print("Enter mode, default is encrypt (encrypt/decrypt): ");
    String mode = scanner.nextLine();

    scanner.close();

    if (key.length() != 4) {
      System.out.println("Key must be 4 characters long");
      return;
    }

    if (text.length() == 0) {
      System.out.println("Text must not be empty");
      return;
    }

    String result = cbcMode(key, text, mode);
    System.out.println("Result: " + result);
  }
}