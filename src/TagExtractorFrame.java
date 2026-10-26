import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/** Swing GUI for the Tag/Keyword Extractor. */
public class TagExtractorFrame extends JFrame
{
    private final TagExtractor extractor = new TagExtractor();

    private File textFile;
    private File stopFile;

    private final JLabel textFileLabel = new JLabel("Text file: (none selected)");
    private final JLabel stopFileLabel = new JLabel("Stop word file: (none selected)");
    private final JLabel statusLabel = new JLabel("Choose a text file and a stop word file.");
    private final JTextArea outputArea = new JTextArea(25, 40);

    private final JButton chooseTextBtn = new JButton("Choose Text File...");
    private final JButton chooseStopBtn = new JButton("Choose Stop Word File...");
    private final JButton extractBtn = new JButton("Extract Tags");
    private final JButton saveBtn = new JButton("Save Tags...");
    private final JButton quitBtn = new JButton("Quit");

    public TagExtractorFrame()
    {
        super("Tag/Keyword Extractor");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));

        // Top: file names
        JPanel top = new JPanel(new GridLayout(3, 1));
        top.add(textFileLabel);
        top.add(stopFileLabel);
        top.add(statusLabel);
        top.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 8));
        add(top, BorderLayout.NORTH);

        // Center: results
        outputArea.setEditable(false);
        outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        add(new JScrollPane(outputArea), BorderLayout.CENTER);

        // Bottom: buttons
        JPanel bottom = new JPanel(new FlowLayout());
        bottom.add(chooseTextBtn);
        bottom.add(chooseStopBtn);
        bottom.add(extractBtn);
        bottom.add(saveBtn);
        bottom.add(quitBtn);
        add(bottom, BorderLayout.SOUTH);

        extractBtn.setEnabled(false);
        saveBtn.setEnabled(false);

        chooseTextBtn.addActionListener(e -> chooseTextFile());
        chooseStopBtn.addActionListener(e -> chooseStopFile());
        extractBtn.addActionListener(e -> runExtraction());
        saveBtn.addActionListener(e -> saveResults());
        quitBtn.addActionListener(e -> System.exit(0));

        pack();
        setLocationRelativeTo(null);
    }

    private JFileChooser newChooser()
    {
        // Start in the project folder so the data files are easy to find
        return new JFileChooser(new File(System.getProperty("user.dir")));
    }

    private void chooseTextFile()
    {
        JFileChooser chooser = newChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
        {
            textFile = chooser.getSelectedFile();
            textFileLabel.setText("Text file: " + textFile.getName());
            updateButtons();
        }
    }

    private void chooseStopFile()
    {
        JFileChooser chooser = newChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
        {
            stopFile = chooser.getSelectedFile();
            stopFileLabel.setText("Stop word file: " + stopFile.getName());
            updateButtons();
        }
    }

    private void updateButtons()
    {
        extractBtn.setEnabled(textFile != null && stopFile != null);
    }

    private void runExtraction()
    {
        try
        {
            extractor.loadStopWords(stopFile.toPath());
            extractor.extractTags(textFile.toPath());

            outputArea.setText(extractor.formatTags());
            outputArea.setCaretPosition(0);
            textFileLabel.setText("Extracting tags from: " + textFile.getName());
            statusLabel.setText(extractor.getStopWordCount() + " stop words loaded; "
                    + extractor.getSortedTags().size() + " distinct tags found.");
            saveBtn.setEnabled(true);
        }
        catch (IOException ex)
        {
            JOptionPane.showMessageDialog(this, "Could not read file:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveResults()
    {
        JFileChooser chooser = newChooser();
        chooser.setSelectedFile(new File("tags_output.txt"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
        {
            Path out = chooser.getSelectedFile().toPath();
            try
            {
                extractor.saveTags(out);
                statusLabel.setText("Saved tags to " + out.getFileName());
            }
            catch (IOException ex)
            {
                JOptionPane.showMessageDialog(this, "Could not save file:\n" + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}