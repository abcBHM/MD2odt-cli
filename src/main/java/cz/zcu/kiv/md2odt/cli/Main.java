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
            LOGGER.info("Not enough arguments");
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
        if(!input.exists() || !(MD_PATTERN.matcher(inputPath).matches() || ZIP_PATTERN.matcher(inputPath).matches())) {
            LOGGER.info("Invalid input");
            throw new IllegalArgumentException("Input is not valid");
        } else {
            LOGGER.debug("Setting source path");
            source = inputPath;
        }

        //Output can be on second or third position
        File output;

        //Check if template is valid if it is set
        if(args.length == 3) {
            File templateFile = new File(args[1]);

            String templPath = templateFile.getAbsolutePath();

            if(!templateFile.exists() || !TEMPLATE_PATTERN.matcher(templPath).matches()) {
                LOGGER.info("Invalid template");
                throw new IllegalArgumentException("Template is not valid");
            } else {
                LOGGER.debug("Setting template path");
                template = templPath;
            }

            output = new File(args[2]);
        } else {
            output = new File(args[1]);
        }

        if(output.exists()) {
            LOGGER.info("Output file already exists");
            throw new IllegalArgumentException("Output file already exists");
        } else {
            LOGGER.debug("Setting output path");
            out = output.getAbsolutePath();
        }
    }

    private static void convert() {
        try {
            Converter converter = MD2odt.converter();

            if(MD_PATTERN.matcher(source).matches()) {
                InputStream md = new FileInputStream(source);
                converter.setInputStream(md);
                LOGGER.debug("Source InputStream is set");

            } else if(ZIP_PATTERN.matcher(source).matches()) {
                InputStream zip = new FileInputStream(source);
                converter.setInputZip(zip);
                LOGGER.debug("Source InputStream is set");
            }

            if(template != null) {
                InputStream tmpl = new FileInputStream(template);
                converter.setTemplate(tmpl);
                LOGGER.debug("Template InputStream is set");
            }

            OutputStream output = Files.newOutputStream(Paths.get(out));
            LOGGER.debug("OutputStream is set");

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
        System.out.println("Path to odt template (.odt or .ott). (optional)");
        System.out.println("Path with name of converted document. (required)");
    }

}
