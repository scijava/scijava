Here's how I have set up SciJava Ops in Fiji:

# Ensure Java 11 is being used

`sudo update-java-alternatives` is likely the best 

# Installing the SciJava Ops update site

You can install the SciJava Ops update site (containing ImageJ Ops2, SciJava Ops, and all other required components) by following the [Update site tutorial](https://imagej.net/update-sites/following#add-update-sites). 

1. With Fiji open, navigate to `Help --> Update...`
2. Click `Manage Update Sites` 
3. Click `Add Unlisted Site`
4. In the URL field, enter `https://sites.imagej.net/scijava-ops`
5. (Optional) in the Name field, enter `SciJava Ops`
6. Click `Apply and Close`. You should see many items appear in the `ImageJ Updater` table.
7. Click `Apply Changes`
8. Click the close button of Fiji, or navigate to `File --> Quit`, to close Fiji.

# Launching Fiji

Ideally, until Fiji starts shipping Java 11, we'd follow [these instructions](https://forum.image.sc/t/run-fiji-or-imagej-headless-mode-with-java-11/73989/4). Unfortunately, we don't have access to the full classpath with this method, breaking the Therapi Op discovery mechanism. The following line of code DOES work, however:

```bash
export FIJI_HOME=~/Applications/Fiji.app; /usr/bin/java -cp "$FIJI_HOME/jars/*:$FIJI_HOME/jars/bio-formats/*:$FIJI_HOME/plugins/*" sc.fiji.Main
```


