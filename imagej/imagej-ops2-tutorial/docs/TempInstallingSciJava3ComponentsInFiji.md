Until we start using the update site, here's how I have set up SciJava Ops in Fiji:

# Ensure Java 11 is being used

`sudo update-java-alternatives` is likely the best 

# Building the necesssary jars into a Fiji installation

From the top-level incubator folder, you can run the following to install the incubator into a local Fiji. 

```bash
mvn clean install -Dscijava.app.directory=<path to your Fiji.app folder>
```

# Launching Fiji

Ideally, until Fiji starts shipping Java 11, we'd follow [these instructions](https://forum.image.sc/t/run-fiji-or-imagej-headless-mode-with-java-11/73989/4). Unfortunately, we don't have access to the full classpath with this method, breaking the Therapi Op discovery mechanism. The following line of code DOES work, however:

```bash
export FIJI_HOME=~/Applications/Fiji.app; /usr/bin/java -cp "$FIJI_HOME/jars/*:$FIJI_HOME/jars/bio-formats/*:$FIJI_HOME/plugins/*" sc.fiji.Main
```


