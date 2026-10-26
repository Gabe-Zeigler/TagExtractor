import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Core logic: loads a stop word Set, scans a text file, and builds a
 * Map of word -> frequency, skipping any stop words.
 */
public class TagExtractor
{
    private final Set<String> stopWords = new TreeSet<>();   // TreeSet keeps them sorted
    private final Map<String, Integer> tags = new HashMap<>();

    /** Loads one lowercase word per line into the stop word set. */
    public void loadStopWords(Path file) throws IOException
    {
        stopWords.clear();
        try (BufferedReader reader = Files.newBufferedReader(file))
        {
            String line;
            while ((line = reader.readLine()) != null)
            {
                String word = line.trim().toLowerCase();
                if (!word.isEmpty())
                {
                    stopWords.add(word);
                }
            }
        }
    }

    /** Scans the text file and fills the tag map (replacing any earlier results). */
    public void extractTags(Path file) throws IOException
    {
        tags.clear();
        try (BufferedReader reader = Files.newBufferedReader(file))
        {
            String line;
            while ((line = reader.readLine()) != null)
            {
                // Dashes often join two words with no spaces ("word--word"), so split on them first
                line = line.replace("--", " ").replace('\u2014', ' ').replace('\u2013', ' ');

                for (String token : line.split("\\s+"))
                {
                    // Remove every non-letter character and normalize case
                    String word = token.replaceAll("[^A-Za-z]", "").toLowerCase();

                    if (!word.isEmpty() && !stopWords.contains(word))
                    {
                        tags.merge(word, 1, Integer::sum);
                    }
                }
            }
        }
    }

    public boolean hasStopWords()
    {
        return !stopWords.isEmpty();
    }

    public int getStopWordCount()
    {
        return stopWords.size();
    }

    /** Returns entries sorted by frequency (highest first), ties alphabetical. */
    public List<Map.Entry<String, Integer>> getSortedTags()
    {
        List<Map.Entry<String, Integer>> list = new ArrayList<>(tags.entrySet());
        list.sort((a, b) ->
        {
            int cmp = b.getValue().compareTo(a.getValue());
            return (cmp != 0) ? cmp : a.getKey().compareTo(b.getKey());
        });
        return list;
    }

    /** Formats the tags as "word : count" lines. */
    public String formatTags()
    {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Integer> e : getSortedTags())
        {
            sb.append(String.format("%-20s %d%n", e.getKey(), e.getValue()));
        }
        return sb.toString();
    }

    /** Writes the formatted tags to a file. */
    public void saveTags(Path file) throws IOException
    {
        try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(file)))
        {
            out.print(formatTags());
        }
    }
}