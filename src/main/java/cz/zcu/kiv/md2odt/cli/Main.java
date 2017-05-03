package cz.zcu.kiv.md2odt.cli;

import cz.zcu.kiv.md2odt.Converter;
import cz.zcu.kiv.md2odt.MD2odt;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Pattern;

/**
 * CLI application for MD2odt library
 *
 * @author Vít Mazín
 * @version 2017-05-03
 */
public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class);

    private static final int PATTERN_FLAGS = Pattern.UNICODE_CHARACTER_CLASS;
    private static final Pattern ZIP_PATTERN = Pattern.compile("(?i).+\\.(zip|jar)", PATTERN_FLAGS);
    private static final Pattern MD_PATTERN = Pattern.compile("(?i).+\\.(md|markdown|txt)", PATTERN_FLAGS);
    private static final Pattern TEMPLATE_PATTERN = Pattern.compile("(?i).+\\.(odt|ott)", PATTERN_FLAGS);

    private static String source = null;
    private static String template = null;
    private static String out = null;

    private static Charset charset = null;

    /**
     * Main method of application.
     *
     * @param args array of arguments
     */
    public static void main(String[] args) {
        checkArgs(args);
        convert();
    }

    /**
     * This method checks if arguments passed
     * to application are valid.
     *
     * @param args array of arguments
     */
    private static void checkArgs(String[] args) {
        if(args.length < 2) {
            if(args.length == 1) {
                if(!args[0].equalsIgnoreCase("help")) {
                    LOGGER.debug("Invalid arguments");
                    throw new IllegalArgumentException("Invalid arguments. Type help for help.");
                }
            } else {
                LOGGER.debug("Not enough arguments");
                throw new IllegalArgumentException("Not enough arguments. Type help for help.");
            }
        }

        if(args[0].equalsIgnoreCase("help")) {
            LOGGER.info("Showing help");
            help();
            System.exit(0);
        }

        File input = new File(args[0]);
        String inputPath = input.getAbsolutePath();

        //Check if input is valid
        if(input.isDirectory()) {
            source = inputPath;
        } else {
            if (!input.exists() || !(MD_PATTERN.matcher(inputPath).matches() || ZIP_PATTERN.matcher(inputPath).matches())) {
                LOGGER.debug("Invalid input");
                throw new IllegalArgumentException("Input is not valid");
            } else {
                LOGGER.info("Setting source path");
                source = inputPath;
            }
        }

        //Output can be on second or third position
        File output = new File(args[1]);

        if(output.exists()) {
            LOGGER.debug("Output file already exists");
            throw new IllegalArgumentException("Output file already exists");
        } else {
            LOGGER.info("Setting output path");
            out = output.getAbsolutePath();
        }

        //Check if template is valid if it is set
        if(args.length > 2) {
            try {
                for (int i = 2; i < args.length; i += 2) {
                    switchSwitches(args[i], args[i + 1]);
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                LOGGER.debug("Invalid optional arguments");
                throw new IllegalArgumentException("Invalid optional arguments");
            }
        }
    }

    /**
     * Parsing switcher from arguments.
     *
     * @param sw switch
     * @param value value after switch
     */
    private static void switchSwitches(String sw, String value) {
        switch (sw) {
            case "-t":
                File templateFile = new File(value);

                String templPath = templateFile.getAbsolutePath();

                if (!templateFile.exists() || !TEMPLATE_PATTERN.matcher(templPath).matches()) {
                    LOGGER.debug("Invalid template");
                    throw new IllegalArgumentException("Template is not valid");
                } else {
                    LOGGER.info("Setting template path");
                    template = templPath;
                }
                break;

            case "-c":
                try {
                    value = value.toUpperCase();
                    charset = Charset.forName(value);
                    LOGGER.info("Using " + value + " charset");
                } catch (Exception e) {
                    LOGGER.warn("Unsupported encoding: " + value);
                    charset = StandardCharsets.UTF_8;
                }
                break;
        }
    }

    /**
     * Method sets all parameters to converter and
     * start converting process.
     */
    private static void convert() {
        try {
            Converter converter = MD2odt.converter();

            if(charset == null) {
                charset = StandardCharsets.UTF_8;
            }

            if(MD_PATTERN.matcher(source).matches()) {
                InputStream md = new FileInputStream(source);
                converter.setInput(md, charset);

            } else if(ZIP_PATTERN.matcher(source).matches()) {
                InputStream zip = new FileInputStream(source);
                converter.setInputZip(zip, charset);

            } else {
                File folder = new File(source);
                converter.setInputFolder(folder, charset);
            }

            if(template != null) {
                InputStream tmpl = new FileInputStream(template);
                converter.setTemplate(tmpl);
            }

            OutputStream output = Files.newOutputStream(Paths.get(out));

            converter
                    .setOutput(output)
                    .enableAllExtensions()
                    .convert();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method prints help.
     */
    private static void help() {
        System.out.println("Order of arguments has to be respected.");
        System.out.println("Path to source file, zip or directory. (required)");
        System.out.println("Output path with name of converted document. (required)");
        System.out.println("Optional switches:");
        System.out.println("-t followed by path to template");
        System.out.println("-c followed by charset which can be one of these:");
        System.out.println("utf-8");
        System.out.println("utf-16");
        System.out.println("utf-16be");
        System.out.println("utf-16le");
        System.out.println("iso-8859-1");
        System.out.println("iso-8859-2");
        System.out.println("windows-1250");
        System.out.println("us-ascii");
    }
}
