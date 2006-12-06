package enginuity.newmaps.memmodel;

public class Segment {
    
    public static final int TYPE_FLASH = 0;
    public static final int TYPE_RAM = 1;
    
    private String name;
    private int type;
    private int start;
    private int size;
    
    public Segment(String name, int type, int start, int size) {
        setName(name);
        setType(type);
        setStart(start);
        setSize(size);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
    
}
