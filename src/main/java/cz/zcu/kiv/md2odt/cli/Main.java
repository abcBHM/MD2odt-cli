package cz.zcu.kiv.md2odt.cli;

import cz.zcu.kiv.md2odt.Converter;
import cz.zcu.kiv.md2odt.MD2odt;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Pattern;

/**
 * Created by Vít Mazín on 28.04.2017.
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

    public static void main(String[] args) {
        checkArgs(args);
        convert();
    }

    private static void checkArgs(String[] args) {
        if(args.length < 2 && !args[0].equalsIgnoreCase("help")) {
            LOGGER.debug("Not enough arguments");
            throw new IllegalArgumentException("Not enough arguments. Type help for help.");
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
        if(args.length == 3) {
            File templateFile = new File(args[2]);

            String templPath = templateFile.getAbsolutePath();

            if (!templateFile.exists() || !TEMPLATE_PATTERN.matcher(templPath).matches()) {
                LOGGER.debug("Invalid template");
                throw new IllegalArgumentException("Template is not valid");
            } else {
                LOGGER.info("Setting template path");
                template = templPath;
            }
        }
    }

    private static void convert() {
        try {
            Converter converter = MD2odt.converter();

            if(MD_PATTERN.matcher(source).matches()) {
                InputStream md = new FileInputStream(source);
                converter.setInputStream(md);

            } else if(ZIP_PATTERN.matcher(source).matches()) {
                InputStream zip = new FileInputStream(source);
                converter.setInputZip(zip);

            } else {
                File folder = new File(source);
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

    private static void help() {
        System.out.println("Order of arguments has to be respected.");
        System.out.println("Path to source file, zip or directory. (required)");
        System.out.println("Output path with name of converted document. (required)");
        System.out.println("Path to odt template (.odt or .ott). (optional)");
    }

}
