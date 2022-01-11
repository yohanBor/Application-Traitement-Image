package pdl.backend;

public class Image {
  private static Long count = Long.valueOf(0);
  private Long id;
  private String name;
  private byte[] data;
  private int[] dimensions = new int[3]; //dimensions[0] = width , dimensions[1] = height, dimensions[2] = 3 si image RGB ou 1  si image GRISE

  public Image(final String name, final byte[] data, final int[] dimensions) {
    id = count++;
    this.name = name;
    this.data = data;
    this.dimensions = dimensions;
  }

  public long getId() {
    return id;
  }

  public long getWidth() {
    return dimensions[0];
  }

  public long getHeight() {
    return dimensions[1];
  }

  public int isRGB_Or_Grey(){
    return dimensions[2];
  }

  public boolean isRGB() {
    return (this.dimensions[2] == 3);
  }

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public byte[] getData() {
    return data;
  }
}
