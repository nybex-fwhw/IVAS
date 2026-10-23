package um.feri.ipt.tids.project.parser.ares;

/** ARES input contract: exactly eight ASCII digits, including leading zeroes. */
public final class Ico {
    private Ico() { }

    public static String validate(String value) {
        if (value == null || !value.matches("[0-9]{8}")) {
            throw new IllegalArgumentException("IČO must contain exactly eight ASCII digits.");
        }
        return value;
    }
}
