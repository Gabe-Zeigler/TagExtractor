import javax.swing.SwingUtilities;

public class TagExtractorRunner
{
    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(() -> new TagExtractorFrame().setVisible(true));
    }
}