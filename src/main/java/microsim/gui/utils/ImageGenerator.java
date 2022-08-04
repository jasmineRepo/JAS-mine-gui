package microsim.gui.utils;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class ImageGenerator {

    public static SVGGraphics2D getSVGDocument() {
        // Get a DOMImplementation
        DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();

        // Create an instance of org.w3c.dom.Document
        Document document = domImpl.createDocument(null, "svg", null);

        // Create an instance of the SVG Generator
        return new SVGGraphics2D(document);
    }

    public static String getOutput(SVGGraphics2D generator) {
        ByteArrayOutputStream stream = null;
        try {
            stream = new ByteArrayOutputStream();
            OutputStreamWriter out = new OutputStreamWriter(stream, StandardCharsets.UTF_8);
            generator.stream(out, true);
        } catch (IOException e) {
            System.err.println("Error in SVG generation: " + e.getMessage());
        }

        return stream.toString();
    }

    public static void save(SVGGraphics2D generator, String fileName) {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
            generator.stream(out, true);
            out.close();
        } catch (IOException e) {
            System.err.println("Error in SVG generation: " + e.getMessage());
        }
    }

    public static String generate(JPanel panel) {
        SVGGraphics2D svgGenerator = getSVGDocument();
        svgGenerator.setSVGCanvasSize(panel.getSize());

        panel.paint(svgGenerator);

        return getOutput(svgGenerator);
    }

    public static String generate(JFrame frame) {
        SVGGraphics2D svgGenerator = getSVGDocument();
        svgGenerator.setSVGCanvasSize(frame.getSize());

        frame.paint(svgGenerator);

        return getOutput(svgGenerator);
    }

    public static void save(JPanel panel, String fileName) {
        SVGGraphics2D svgGenerator = getSVGDocument();

        panel.paint(svgGenerator);

        save(svgGenerator, fileName);
    }

    public static void save(JFrame frame, String fileName) {
        SVGGraphics2D svgGenerator = getSVGDocument();

        frame.paint(svgGenerator);

        save(svgGenerator, fileName);
    }

    public static BufferedImage toImage(JPanel panel) {
        // Create a Buffered Image
        Dimension d = panel.getSize();
        BufferedImage img = new BufferedImage(d.width + 10, d.height + 10, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = img.createGraphics();
        graphics.translate(5, 5);
        panel.paint(graphics);

        return img;
    }

    public static String[] supportedFormats() {
        return javax.imageio.ImageIO.getWriterFormatNames();
    }

    public static void saveImage(JPanel panel, String fileName, String format) {
        try {
            BufferedImage img = toImage(panel);
            File f = new File(fileName);
            ImageIO.write(img, format, f);
        } catch (IOException e) {
            System.err.println("Error saving image: " + e.getMessage());
        }
    }

    public static JMenu getExportMenu(JPanel panel) {
        Action action;
        JMenu exportMenu = new JMenu("Export");
        final JPanel expPanel = panel;

        String[] formats = ImageIO.getWriterFormatNames();

        //SVG
        action = new AbstractAction("SVG") {
            @Serial
            private static final long serialVersionUID = 1L;

            public void actionPerformed(ActionEvent e) {
                File file = saveDialog("SVG");
                if (file != null)
                    save(expPanel, file.toString());
            }
        };
        exportMenu.add(new JMenuItem(action));


        for (final String format : formats) {
            if (format.toUpperCase().equals(format)) {
                action = new AbstractAction(format) {
                    @Serial
                    private static final long serialVersionUID = 1L;

                    public void actionPerformed(ActionEvent e) {
                        File file = saveDialog(format);
                        if (file != null)
                            try {
                                BufferedImage im = toImage(expPanel);
                                ImageIO.write(im, format, file);
                            } catch (IOException ex) {
                                System.err.println("Error saving image: " + ex.getMessage());
                            }
                    }
                };
                exportMenu.add(new JMenuItem(action));
            }
        }

        return exportMenu;
    }

    public static File saveDialog(String format) {
        File f = new File(".");
        JFileChooser jfc = new JFileChooser(f);

        FileFilter ff = new CustomFileFilter(format, format + " file");
        jfc.setFileFilter(ff);

        int result = jfc.showSaveDialog(null);
        if (result == JFileChooser.CANCEL_OPTION) return null;

        File selectedFile = jfc.getSelectedFile();
        if (!(selectedFile.toString().endsWith(format)))
            selectedFile = new File(selectedFile.getAbsolutePath() + "." + format);
        return selectedFile;
    }
}
