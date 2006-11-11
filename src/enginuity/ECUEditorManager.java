package enginuity;

public class ECUEditorManager {
    private static ECUEditor editor = null;

    public static ECUEditor getECUEditor() {
        if (editor == null) {
            editor = new ECUEditor();
        }

        return editor;
    }
}
