package microsim.gui.utils;

import lombok.Getter;

import javax.swing.filechooser.*;
import java.io.File;
import java.util.ArrayList;

public class CustomFileFilter extends FileFilter {
    String extension;
    @Getter
    String description;
    ArrayList<String> additionalExt = null;

    public CustomFileFilter(String extension, String description) {
        this.extension = extension;
        this.description = description;
    }

    public void addExtension(String extension) {
        if (additionalExt == null) additionalExt = new ArrayList<>();

        additionalExt.add(extension);
    }

    public boolean accept(File f) {
        if (additionalExt == null) return f.isDirectory() || f.getName().endsWith(extension);

        boolean acceptable = f.isDirectory() || f.getName().endsWith(extension);

        for (String s : additionalExt) if (f.getName().endsWith(s)) return true;

        return acceptable;
    }
}
