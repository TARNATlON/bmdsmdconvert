# bmdsmdconvert
This program is intended to convert from the Pixelmon format (BMD) for models and animations of most Pokémon in the mod to Source engine SMD files, and the other way around.

This means you can export the models and animations from the mod and/or replace them with your own.

The Pixelmon mod used to use SMD files up to and including version 7.0.8, and afterwards they switched to their own format. I was not able to find a conversion tool for the new format online, so I made my own.

# How to run
You'll need to have your own BMD or SMD file to convert. This tool will not extract them from Pixelmon.

Head over to the releases and download the latest jar. If you don't have Java 11 already, you'll need that.
Command line arguments are:
```
usage: bmdsmdconvert -i input -o output -f format [-b00b]
 -b00b,--b00b        Writes hex B00B in the BMD format in some places
                     Pixelmon doesn't read from. :D
 -f,--format <arg>   The format to write the output file in. Will assume
                     input file is the opposite format. Valid options are
                     "SMD", "smd", "BMD", and "bmd"
 -i,--input <arg>    Input file
 -o,--output <arg>   Output file
```

# How to build
Clone this repo with `git clone https://github.com/kernel-pan-ic/bmdsmdconvert.git`, then `cd bmdsmdconvert`.

To build on Linux, run `./gradlew build`.

On Windows, do `gradlew.bat build`.

The compiled output will be at `build/libs/bmdsmdconvert-all.jar`.
