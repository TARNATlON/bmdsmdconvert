# bmdsmdconvert
Converts between the Source engine's SMD model and animation format and the Pixelmon BMD format.

# How to run
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
Clone this repo with `git clone https://github.com/kernel-pan-ic/bmdsmdconvert.git`, then `cd bmdsmdconvert`, then to build on Linux, run `./gradlew build`.
On Windows, do `gradlew.bat build`.
The compiled output will be at build/libs/bmdsmdconvert-all.jar.
