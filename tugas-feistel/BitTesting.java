public class BitTesting {
  private static final int IRREDUCIBLE_POLYNOMIAL = 0b100010; // x^16 + x^12 + x^5 + x^3 + 1

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

  public static void main(String[] args) {
    System.out.println(Integer.toBinaryString(polyModulo(0b11111100, IRREDUCIBLE_POLYNOMIAL)));
  }
}