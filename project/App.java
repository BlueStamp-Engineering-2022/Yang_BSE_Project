
import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.lang.Object.*;
import javax.sound.midi.*;

public class App {

    static Scanner in = new Scanner(System.in);// User input from input stream
    static String[] files = { // not used anymore
            " target/off.sh",
            " target/on.sh"
    };
    static String music = "songs/";// File Path for music file
    static String mdir = "songs";// File Path of Songs directory
    static String com = "target/compile.sh";// Not used anymore
    static String sonNam; // Name of song file
    public static final int NOTE_ON = 0x90;
    public static final int NOTE_OFF = 0x80;

    static Instrument instruments[];
    static boolean settings = false;
    public static final String[] NOTE_NAMES = {
            "C",
            "C#",
            "D",
            "D#",
            "E",
            "F",
            "F#",
            "G",
            "G#",
            "A",
            "A#",
            "B"
    };
    static boolean[] desired = {
            true,
            false,
            true,
            false,
            true,
            true,
            false,
            true,
            false,
            true,
            false,
            true
    };

    public static void main(String[] args) throws Exception {
        // Creates/Checks settings file
        File settingsFILE = new File("settings.txt");
        if (settingsFILE.createNewFile()) {
            System.out.println("File created: " + settingsFILE.getName());
        } else {
            System.out.println("Found settings file.");

        }
        // Takes Input Parameters
        if (args.length == 1) {
            music += args[0].trim() + ".mid";
        } else if (args.length == 2) {

            music += args[0].trim() + ".mid";
            settings = true;
            sonNam = args[0].trim();
        } else {
            File dir = new File(mdir);
            HashMap<String, String> bin = new HashMap<String, String>();
            while (true) {
                System.out.println("What File would you like:");

                for (final File fileEntry : dir.listFiles()) {
                    String filename = fileEntry.getName();
                    String[] ext = filename.split("\\.");
                    System.out.println(filename);

                    if (ext[ext.length - 1].trim().toLowerCase().equals("mid")) {
                        String name = "";
                        for (int i = 0; i < ext.length - 1; i++) {
                            name = name + "" + ext[i];
                        }
                        System.out.println("name :" + name.replace(" ", ""));
                        bin.put(name.replace(" ", "").toLowerCase(), name);

                    }

                }

                String choosen = in.next().trim();

                if (bin.containsKey(choosen.toLowerCase())) {
                    music += bin.get(choosen.toLowerCase()) + ".mid";
                    sonNam = bin.get(choosen.toLowerCase());
                    break;
                } else {
                    // System.out.println("STRING DIST");
                    String lowestDistElement = null;
                    int minDist = Integer.MAX_VALUE;
                    for (Map.Entry mapElement : bin.entrySet()) {
                        String key = (String) mapElement.getKey();

                        int dist = editDist(key.substring(0, Math.min(choosen.length(), key.length()) - 1),
                                choosen.toLowerCase()
                                );
                        //System.out.println("checking " + key + " " + dist);

                        if (dist == minDist) { // If the distance is equal
                            //System.out.println("Simular distance " + key + " " + lowestDistElement);

                            int offset = Math.min(lowestDistElement.length(), key.length());

                            int breakCon = Math.max(lowestDistElement.length(), key.length());
                            while (offset <= breakCon && offset< 10) {// Slowly increase search parameters until one is found to be closer

                                int n1dist = editDist(key.substring(0, Math.min(key.length(), offset)),
                                        choosen.toLowerCase()
                                        );

                                int n2dist = editDist(
                                        lowestDistElement.substring(0, Math.min(lowestDistElement.length(), offset)),
                                        choosen.toLowerCase());
                                if (n1dist < n2dist) {

                                    minDist = n1dist;
                                    lowestDistElement = key;
                                    break;
                                }else
                                    if (n1dist > n2dist) {

                                        break;
                                    }
                            }
                            if (dist < minDist) {

                                minDist = dist;
                                lowestDistElement = key;
                            }

                        }
                        if (dist < minDist) {

                            minDist = dist;
                            lowestDistElement = key;
                        }
                    }
                    System.out.println("Did you mean " + bin.get(lowestDistElement) + "? [Y/N]");
                    String response = in.next();
                    if (response.toLowerCase().charAt(0) == 'y') {
                        music += bin.get(lowestDistElement) + ".mid";
                        sonNam = bin.get(lowestDistElement);
                        break;
                    }

                    System.out.println("Your input wasn't interpreted correctly. Please try again.");
                }
            }
            while (true) {
                System.out.print("Do you want to use settings file?[Y/N]:");

                String choosen = in.next().toLowerCase().trim();

                if (choosen.charAt(0) == 'y') {
                    settings = true;
                    break;
                } else if (choosen.charAt(0) == 'n') {
                    break;
                } else {
                    System.out.println("Your input wasn't interpreted correctly. Please try again.");
                }
            }

        }
        // Sets GPIO to 63 (default )
        writeTOGPIO(63);
        Synthesizer synthesizer = MidiSystem.getSynthesizer();
        Soundbank sb = synthesizer.getDefaultSoundbank();
        Sequence sequence = MidiSystem.getSequence(new File(music));
        instruments = sb.getInstruments();
        play(); // MEATHOD
        /*
         * // READING DATA
         * for (Instrument i : instruments)
         * System.out.println(i);
         * 
         * int trackNumber = 0;
         * 
         * for (Track track : sequence.getTracks()) {
         * trackNumber++;
         * System.out.println("Track " + trackNumber + ": size = " + track.size());
         * System.out.println();
         * for (int i = 0; i < track.size(); i++) {
         * MidiEvent event = track.get(i);
         * // System.out.print("@" + event.getTick() + " ");
         * MidiMessage message = event.getMessage();
         * if (message instanceof ShortMessage) {
         * boolean isPrecussion = false;
         * ShortMessage sm = (ShortMessage) message;
         * int channel = sm.getChannel() + 1;
         * // System.out.print("Channel: " + sm.getChannel() + " ");
         * if (channel == 10) {
         * isPrecussion = true;
         * }
         * if (sm.getCommand() == NOTE_ON) {
         * int key = sm.getData1();
         * int octave = (key / 12) - 1;
         * int note = key % 12;
         * String noteName = NOTE_NAMES[note];
         * int velocity = sm.getData2();
         * Thread.sleep(50);
         * System.out.print(key + " ");
         * // System.out.println("Note on, " + noteName + octave + " key=" + key + "
         * // velocity: " + velocity + " " + sm.getData1());
         * } else if (sm.getCommand() == NOTE_OFF) {
         * int key = sm.getData1();
         * int octave = (key / 12) - 1;
         * int note = key % 12;
         * String noteName = NOTE_NAMES[note];
         * int velocity = sm.getData2();
         * // System.out.println("Note off, " + noteName + octave + " key=" + key + "
         * // velocity: " + velocity + " " + instruments[sm.getData1()]);
         * } else {
         * // System.out.println("Command:" + sm.getCommand());
         * }
         * } else {
         * System.out.println("Other message: " + message.getClass());
         * }
         * }
         * 
         * System.out.println();
         * }
         */
        in.close();

    }

    /**
     * @throws Exception
     *                   Plays music from input
     */
    public static void play() throws Exception {

        /*
         * String[] cmd = { "hello" };
         * toggle(cmd);
         */
        Sequencer sequencer = MidiSystem.getSequencer(); // Get the default Sequencer
        if (sequencer == null) {
            System.err.println("Sequencer device not supported");
            return;
        }

        sequencer.open(); // Open device
        // Create sequence, the File must contain MIDI file data.
        Sequence sequence = MidiSystem.getSequence(new File(music));

        sequencer.setSequence(sequence); // load it into sequencer
        // Prints tracks
        for (Track track : sequence.getTracks()) {
            int num = 0;
            while (!(track.get(num).getMessage() instanceof ShortMessage)) {
                num++;
                if (num == track.size()) {
                    break;
                }
            }
            if (num == track.size()) {// If there's no sound in Track
                continue;
            }
            MidiEvent event = track.get(num);
            System.out.print("@" + event.getTick() + " ");
            MidiMessage message = event.getMessage();
            if (message instanceof ShortMessage) {
                ShortMessage sm = (ShortMessage) message;
                System.out.println("Channel: " + sm.getChannel() + " ");

            }
        }
        ArrayList<MidiEvent> events = new ArrayList<MidiEvent>(); // List of Notes
        int max = Integer.MIN_VALUE; // Highest Note Value
        int min = Integer.MAX_VALUE; // Lowest Note Value
        // Get Settings
        Scanner set = new Scanner(new File("settings.txt"));
        String[] alternateInput = null;
        while (set.hasNextLine()) {
            String line = set.nextLine();
            String[] content = line.split(Pattern.quote(" "));
            System.out.println(line + " " + content[0].trim());
            if (content[0].trim().equals(sonNam.trim())) {
                System.out.println("FOUND INPUT in settings.txt");
                alternateInput = content.clone();
                break;
            }
        }
        // if no settings are found. Turn off settings
        if (alternateInput == null) {
            System.out.println("No alternate input found in settings.txt");
            settings = false;
        }
        set.close();

        // sets max and min values and adds notes to list of notes
        // Also asks users for which tracks to keep if settings is off
        Track[] trac = sequence.getTracks();
        for (int i = 0; i < sequence.getTracks().length; i++) {
            int num = 0;
            while (!(trac[i].get(num).getMessage() instanceof ShortMessage)) {
                num++;
                if (num == trac[i].size()) {
                    break;
                }
            }
            if (num == trac[i].size()) {// If there's no sound in Track+
                continue;
            }
            MidiEvent event = trac[i].get(20);
            MidiMessage message = event.getMessage();

            if (settings) {
                int fileInput = Integer.parseInt(alternateInput[i + 1].trim().charAt(0) + "");
                if (fileInput == 1) {
                    sequence.deleteTrack(trac[i]);
                } else {
                    if (message instanceof ShortMessage) {
                        ShortMessage sm = (ShortMessage) message;
                        System.out.println("Channel: " + sm.getChannel() + " " + instruments[sm.getData1()]);
                    }
                    for (int e = 0; e < trac[i].size(); e++) {
                        event = trac[i].get(e);
                        message = event.getMessage();
                        if (message instanceof ShortMessage) {
                            ShortMessage sm = (ShortMessage) message;
                            if (sm.getCommand() == NOTE_ON && sm.getData2() > 100) {

                                events.add(event);
                                max = Math.max(max, sm.getData1());
                                min = Math.min(min, sm.getData1());
                                System.out.println("TICK " + event.getTick() + " ");
                            }

                        }
                    }
                }

            } else {
                if (message instanceof ShortMessage) {
                    ShortMessage sm = (ShortMessage) message;
                    System.out.println("Channel: " + sm.getChannel() + " " + instruments[sm.getData1()]);
                }

                System.out.print(" KEEP? [Y/N]");
                if (in.next().toLowerCase().charAt(0) == 'n') {
                    System.out.println(" Keeping track");
                    sequence.deleteTrack(trac[i]);
                } else {
                    for (int e = 0; e < trac[i].size(); e++) {
                        event = trac[i].get(e);
                        message = event.getMessage();
                        if (message instanceof ShortMessage) {
                            ShortMessage sm = (ShortMessage) message;
                            if (sm.getCommand() == NOTE_ON && sm.getData2() > 100) {

                                events.add(event);
                                max = Math.max(max, sm.getData1());
                                min = Math.min(min, sm.getData1());
                                System.out.println("TICK " + event.getTick() + " ");
                            }

                        }
                    }
                }

            }
        }
        // if there is no notes in the song
        if (max == Integer.MAX_VALUE) {
            System.exit(-1);
        }

        // ask arduino to initialize max and min values
        writeTOGPIO(max);
        setReset("1");
        Thread.sleep(50);
        writeTOGPIO(min);
        setReset("0");

        // sorts notes by their timestamp
        Collections.sort(events, new compareA());

        sequencer.start(); // start the playback
        int pointer = 0;
        // Goes through all the notes in the song in order
        while (pointer < events.size()) {

            if (sequencer.getTickPosition() > events.get(pointer).getTick()) {

                float keynum = ((ShortMessage) events.get(pointer).getMessage()).getData1();
                int kkey = (int) keynum;
                long curTICK = events.get(pointer).getTick();

                int totalOctive = (kkey / 12) - 1;
                int totalNote = kkey % 12;
                int keyCounted = 1;

                // If the next note is on the same timestamp as the current note then Average
                // all the notes together based on the octive and key
                while (pointer + 1 < events.size() && events.get(pointer + 1).getTick() == curTICK) {
                    int ke = ((ShortMessage) events.get(pointer).getMessage()).getData1();
                    pointer++;
                    keyCounted++;
                    totalOctive += (ke / 12) - 1;
                    totalNote += ke % 12;
                }
                int avOct = totalOctive / keyCounted;
                int avNot = totalNote / keyCounted;
                keynum = (avOct + 1) * 12 + avNot;
                kkey = (int) keynum;
                // Writes the key to the GPIO output
                writeTOGPIO(kkey);
                // Next Note
                pointer++;

            }
        }

    }

    /**
     * <p>
     * Sets the reset pin of the arduino
     * <p>
     * How to Use :
     * <p>
     * 1. Set GPIO using writeTOGPIO function to the either of the Max or Min Values
     * <p>
     * 2. Call Function to set to 1
     * <p>
     * 3. Wait for Arduino to take in input and be ready
     * <p>
     * 4. Set GPIO using writeTOGPIO function to the min/max values you didn't use
     * in the first step
     * <p>
     * 5. Call Function to set to 0
     * 
     * @param a Output of Pin 26
     * @throws Exception
     * 
     * @see {@link #writeToGPIO(int)}
     */
    public static void setReset(String a) throws Exception {
        runCmd("cmd.sh", "26", a);
    }

    public static void runCmd(String input, String one, String two) throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder();
        File file = new File(input);
        processBuilder.command(file.getAbsolutePath(), one, two);

        Process process = processBuilder.start();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {

            String line;

            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

        }
    }

    /**
     * @deprecated Use {@link #writeTOGPIO(int)} instead
     *             <p>
     *             Writes a command file
     * @param args
     * @throws Exception
     */
    public static void toggle(String args[]) throws Exception {
        PrintWriter out = new PrintWriter(com);
        out.println("#!/bin/sh");
        for (int i = 0; i < args.length; i++) {
            out.println(args[i]);
        }
        out.close();

    }

    /**
     * @deprecated Use {@link #writeTOGPIO(int)} instead
     *             <p>
     *             Writes a command file with GPIOO
     * @param pin
     * @param on
     * @return
     * @throws Exception
     */
    public static File writeCommand(int pin, int on) throws Exception {
        File myObj = new File("cmdp" + pin + "o" + on + ".sh");
        if (myObj.createNewFile()) {
            System.out.println("File created: " + myObj.getName());
        } else {
            System.out.println("File already exists.");
        }

        PrintWriter out = new PrintWriter(myObj);
        out.println("#!/bin/sh");
        out.println("gpio -g  mode " + pin + " out");
        out.println("gpio -g write " + pin + " " + on);
        out.close();
        return myObj;
    }

    static int[] gpioPOS = { 24, 23, 22, 17, 27, 5, 6 };// Positions of the pins for the numbers

    /**
     * <p>
     * Writes a number to the gpio of the raspberry device by converting it to
     * binary format
     * 
     * @param num
     * @return
     * @throws Exception
     */
    public static int writeTOGPIO(int num) throws Exception {
        if (num > 127) {
            return 0;
        }
        String bin = getBin(num);
        for (int i = 0; i < bin.length(); i++) {
            // System.out.println(bin);
            runCmd("cmd.sh", gpioPOS[i] + "", bin.charAt(i) + "");

        }
        return 1;
    }

    // Converts to a 7 bit binary format
    public static String getBin(int input) {
        String result = Integer.toBinaryString(input);
        String resultWithPadding = String.format("%7s", result).replaceAll(" ", "0"); // 32-bit Integer
        return resultWithPadding;
    }


    static int editDist(String x, String y) {
        int[][] dp = new int[x.length() + 1][y.length() + 1];
    
        for (int i = 0; i <= x.length(); i++) {
            for (int j = 0; j <= y.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                }
                else if (j == 0) {
                    dp[i][j] = i;
                }
                else {
                    dp[i][j] = min(dp[i - 1][j - 1] 
                     + costOfSubstitution(x.charAt(i - 1), y.charAt(j - 1)), 
                      dp[i - 1][j] + 1, 
                      dp[i][j - 1] + 1);
                }
            }
        }
    
        return dp[x.length()][y.length()];

    }
    public static int costOfSubstitution(char a, char b) {
        return a == b ? 0 : 1;
    } public static int min(int... numbers) {
        return Arrays.stream(numbers)
          .min().orElse(Integer.MAX_VALUE);
    }
}

// Sort by Timestamp ascending
class compareA implements Comparator<MidiEvent> {

    @Override
    public int compare(MidiEvent o1, MidiEvent o2) {
        if (o1.getTick() < o2.getTick())
            return -1;
        if (o1.getTick() > o2.getTick())
            return 1;
        return 0;
    }

}
