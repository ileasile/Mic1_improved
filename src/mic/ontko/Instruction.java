package mic.ontko;

public class Instruction {

  public static final int NOPARAM = 0;
  public static final int BYTE = 1;
  public static final int CONST = 2;
  public static final int VARNUM = 3;
  public static final int LABEL = 4;
  public static final int OFFSET = 5;
  public static final int INDEX = 6;
  public static final int VARNUM_CONST = 7;
  public static final int WIDE = 8;    // rms - wide fix;
  private int opcode;
  private String mnemonic = null;
  private int type;

  public Instruction() {}

  public Instruction(int opcode, String mnemonic, int type) {
    this.opcode = opcode;
    this.mnemonic = mnemonic;
    this.type = type;
  }

  public void setOpcode(int opcode) {
    this.opcode = opcode;
  }

  public void setMnemonic(String mnemonic) {
    this.mnemonic = mnemonic;
  }

  public void setType(int type) {
    this.type = type;
  }

  public int getOpcode() {
    return opcode;
  }

  public String getMnemonic() {
    return mnemonic;
  }

  public int getType() {
    return type;
  }
}
