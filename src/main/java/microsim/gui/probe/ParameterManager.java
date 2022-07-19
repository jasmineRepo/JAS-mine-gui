package microsim.gui.probe;

import java.net.URL;
import java.util.ArrayList;

/**
 * Not of interest for users. A data model used to contain the list of elements within a collection. It is used by the
 * Probe frame.
 */
public class ParameterManager {
    URL fileName;
    Object targetObject;
    ArrayList<String> fields;

    public ParameterManager(Object target, URL filePath) {
        targetObject = target;
        fileName = filePath;
        fields = new ArrayList<>();
    }

    public void addParameterField(String fieldName) {
        boolean flag = true;
        for (String field : fields)
            if (field.equals(fieldName)) {
                flag = false;
                break;
            }

        if (flag) fields.add(fieldName);
    }
}
