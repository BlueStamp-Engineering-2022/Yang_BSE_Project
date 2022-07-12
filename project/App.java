
import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.lang.Object.*;
import javax.sound.midi.*;

public class App {

    static Scanner in = new Scanner(System.in);
    static String[] files = {
            "/home/pi/Desktop/sing/target/off.sh",
            "/home/pi/Desktop/sing/target/on.sh"
    };
    static String music = "/home/pi/Desktop/sing/songs/";
    static String mdir = "/home/pi/Desktop/sing/songs/";
    static String com = "/home/pi/Desktop/sing/target/compile.sh";
    static String sonNam;
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
        // setReset("0");
        // File ndas=writeCommand(6,0);
        File settingsFILE = new File("settings.txt");
        if (settingsFILE.createNewFile()) {
            System.out.println("File created: " + settingsFILE.getName());
        } else {
            System.out.println("Found settings file.");

        }
        if (args.length == 1) {
            music += args[0].trim() + ".mid";
        } else if (args.length == 2) {

            music += args[0].trim() + ".mid";
            settings = true;
            sonNam = args[0].trim();
        } else {
            File dir = new File(mdir);
            HashMap<String, File> bin = new HashMap<String, File>();
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
                        System.out.println("name :" + name);
                        bin.put(name, fileEntry);

                    }

                }

                String choosen = in.next().trim();

                if (bin.containsKey(choosen)) {
                    music += choosen + ".mid";
                    sonNam = choosen;
                    break;
                } else {
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
        System.out.println(music);
        writeTOGPIO(10);
        Synthesizer synthesizer = MidiSystem.getSynthesizer();
        Soundbank sb = synthesizer.getDefaultSoundbank();

        Sequence sequence = MidiSystem.getSequence(new File(music));
        instruments = sb.getInstruments();
        for (Instrument i : instruments)
            System.out.println(i);

        play();
        int trackNumber = 0;

        for (Track track : sequence.getTracks()) {
            trackNumber++;
            System.out.println("Track " + trackNumber + ": size = " + track.size());
            System.out.println();
            for (int i = 0; i < track.size(); i++) {
                MidiEvent event = track.get(i);
                // System.out.print("@" + event.getTick() + " ");
                MidiMessage message = event.getMessage();
                if (message instanceof ShortMessage) {
                    boolean isPrecussion = false;
                    ShortMessage sm = (ShortMessage) message;
                    int channel = sm.getChannel() + 1;
                    // System.out.print("Channel: " + sm.getChannel() + " ");
                    if (channel == 10) {
                        isPrecussion = true;
                    }
                    if (sm.getCommand() == NOTE_ON) {
                        int key = sm.getData1();
                        int octave = (key / 12) - 1;
                        int note = key % 12;
                        String noteName = NOTE_NAMES[note];
                        int velocity = sm.getData2();
                        Thread.sleep(50);
                        System.out.print(key + " ");
                        // System.out.println("Note on, " + noteName + octave + " key=" + key + "
                        // velocity: " + velocity + " " + sm.getData1());
                    } else if (sm.getCommand() == NOTE_OFF) {
                        int key = sm.getData1();
                        int octave = (key / 12) - 1;
                        int note = key % 12;
                        String noteName = NOTE_NAMES[note];
                        int velocity = sm.getData2();
                        // System.out.println("Note off, " + noteName + octave + " key=" + key + "
                        // velocity: " + velocity + " " + instruments[sm.getData1()]);
                    } else {
                        // System.out.println("Command:" + sm.getCommand());
                    }
                } else {
                    System.out.println("Other message: " + message.getClass());
                }
            }

            System.out.println();
        }
        in.close();

    }

    public static void play() throws Exception {
        String[] cmd = { "hello" };
        toggle(cmd);
        Sequencer sequencer = MidiSystem.getSequencer(); // Get the default Sequencer
        if (sequencer == null) {
            System.err.println("Sequencer device not supported");
            return;
        }

        sequencer.open(); // Open device
        // Create sequence, the File must contain MIDI file data.
        Sequence sequence = MidiSystem.getSequence(new File(music));

        sequencer.setSequence(sequence); // load it into sequencer

        for (Track track : sequence.getTracks()) {
            int num = 0;
            while(! (track.get(num).getMessage() instanceof ShortMessage)){
                num++;
                if( num == track.size()){
                    break;
                }
            }
            if( num == track.size()){// If there's no sound in Track
                continue;
            }
            MidiEvent event = track.get(num);
            System.out.print("@" + event.getTick() + " ");
            MidiMessage message = event.getMessage();
            if (message instanceof ShortMessage) {
                boolean isPrecussion = false;
                ShortMessage sm = (ShortMessage) message;
                int channel = sm.getChannel() + 1;
                System.out.println("Channel: " + sm.getChannel() + " ");
                
            }
        }
        ArrayList<Long> timestamps = new ArrayList<Long>();
        ArrayList<MidiEvent> events = new ArrayList<MidiEvent>();
        int max = Integer.MIN_VALUE;
        int min = Integer.MAX_VALUE;
        Scanner set = new Scanner(new File("settings.txt"));
        String[] alternateInput = null;
        while (set.hasNextLine()) {
            String line = set.nextLine();
            String[] content = line.split(Pattern.quote(" "));
            System.out.println(line + " " + content[0].trim());
            if (content[0].trim().equals(sonNam.trim())) {
                System.out.println("FOUND INPUT");
                alternateInput = content.clone();
                break;
            }
        }
        if (alternateInput == null) {
            System.out.println("No alternate input found in settings.txt");
            settings = false;
        }
        set.close();
        Track[] trac = sequence.getTracks();
        for (int i = 0; i < sequence.getTracks().length; i++) {
            int num = 0;
            while(! (trac[i].get(num).getMessage() instanceof ShortMessage)){
                num++;
                if( num == trac[i].size()){
                    break;
                }
            }
            if( num == trac[i].size()){// If there's no sound in Track+
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
    
                                timestamps.add(event.getTick());
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

                System.out.print(" KEEP?");
                if (in.nextInt() == 1) {
                    sequence.deleteTrack(trac[i]);
                } else {
                    for (int e = 0; e < trac[i].size(); e++) {
                        event = trac[i].get(e);
                        message = event.getMessage();
                        if (message instanceof ShortMessage) {
                            ShortMessage sm = (ShortMessage) message;
                            if (sm.getCommand() == NOTE_ON && sm.getData2() > 100) {

                                timestamps.add(event.getTick());
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
        if (max == Integer.MAX_VALUE){
            System.exit(-1);
        }
        writeTOGPIO(max);
        setReset("1");
        Thread.sleep(50);
        writeTOGPIO(min);
        setReset("0");

        Collections.sort(events, new compareA());

        Collections.sort(timestamps);
        sequencer.start(); // start the playback
        int pointer = 0;

        while (pointer < events.size()) {
            if (sequencer.getTickPosition() > events.get(pointer).getTick()) {

                float keynum = ((ShortMessage) events.get(pointer).getMessage()).getData1();
                int kkey = (int) keynum;
                long curTICK = events.get(pointer).getTick();

                int totalOctive = (kkey / 12) - 1;
                int totalNote = kkey % 12;
                int keyCounted = 1;
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
                writeTOGPIO(kkey);
                pointer++;

            }
        }

    }

    public static void setReset(String a) throws Exception {
        runCmd("/home/pi/Desktop/sing/target/classes/cmd.sh", "26", a);
    }

    public static void runCmd(String input, String one, String two) throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(input, one, two);

        Process process = processBuilder.start();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {

            String line;

            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

        }
    }

    public static void toggle(String args[]) throws Exception {
        PrintWriter out = new PrintWriter(com);
        out.println("#!/bin/sh");
        for (int i = 0; i < args.length; i++) {
            out.println(args[i]);
        }
        out.close();

    }

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

    static int[] gpioPOS = { 24, 23, 22, 17, 27, 5, 6 };

    public static int writeTOGPIO(int num) throws Exception {
        if (num > 127) {
            return 0;
        }
        String bin = getBin(num);
        for (int i = 0; i < bin.length(); i++) {
            //System.out.println(bin);
            runCmd("/home/pi/Desktop/sing/target/classes/cmd.sh", gpioPOS[i] + "", bin.charAt(i) + "");

        }
        return 1;
    }

    public static String getBin(int input) {
        String result = Integer.toBinaryString(input);
        String resultWithPadding = String.format("%7s", result).replaceAll(" ", "0"); // 32-bit Integer
        return resultWithPadding;
    }
}

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

