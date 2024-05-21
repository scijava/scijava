# Installation

## Fiji

One easy way to use SciJava Ops is within [Fiji](https://fiji.sc), using SciJava's Script Editor. **Note that to use Fiji, you must use Java 11+. Instructions on using Java 11+ for Fiji can be found [here](https://imagej.net/learn/faq#how-do-i-launch-imagej-with-a-different-version-of-java)**.

### Installing SciJava Ops

You can install SciJava Ops into Fiji by enabling the SciJava Ops update site, as described in the [Update site tutorial](https://imagej.net/update-sites/following#add-update-sites). 

1. With Fiji open, navigate to `Help → Update...`
2. Click `Manage Update Sites` 
3. In the `Search` bar at the top of the popup, search `SciJava Ops`
4. Find the `SciJava Ops` row, and check the checkbox in the `Active` column.
5. Click `Apply and Close`. You should see many items appear in the `ImageJ Updater` table.
6. Click `Apply Changes`
7. **Note: launcher change** Close and restart Fiji using the new launcher: `fiji-[platform]`. For example, if you were on 64-bit Windows your old (deprecated) launcher was `ImageJ-win64.exe`, and you would instead now use `fiji-windows-x64.exe`.
8. Run `Help → Update...` a second time. Historically Fiji has shipped with Java 8, but a newer Java is required for SciJava Ops. You should get a dialog saying a new Java version is recommended, and asking if you'd like to update. Click `OK` to start the update. This download and extraction may take longer than a usual update.
9. After receiving a notification that the Java update is complete you can close Fiji and restart one more time to take advantage of the new Java. **Note:** always use the new `fiji-` launcher going forward.
10. After Fiji restarts, you can [double-check the Java version](https://imagej.net/learn/troubleshooting#checking-the-java-version) - it should be `21` or newer.

At this point you can proceed with using SciJava Ops!

### Troubleshooting

SciJava Ops uses a branch new launcher and updating logic to get access to new Java programming language features. These have not reached the main Fiji release yet and thus have not gone through community hardening yet, so errors may pop up.

If the following steps do not help resolve your situation, please [report the issue](https://imagej.net/discuss/bugs) on the [ImageJ forum](https://forum.image.sc/) or [Zulip](https://imagej.net/discuss/chat#zulip).

#### The new fiji- launcher doesn't start

Double-check the `Fiji.app/config/jaunch` directory [Windows/Linux] or `Fiji.app/Contents/MacOS`. There should be a number of files including `jvm.toml`, `fiji.toml`, `common.toml`. If they aren't there, they may be stuck in the same path under the `Fiji.app/update` directory: you can manually copy them out to the base `Fiji.app` diretory, following the same subdirectory structure they had under `/update/`.

#### I did the new Java update but after restarting Fiji is not using Java 21

The new java should have been downloaded to `Fiji.app/java/[platform/` and will likely have a directory name along the lines of `zulu21[...]jdk21[...]`.

In your `Fiji.app/config/jaunch` directory you should have a file named `fiji.cfg` containing the line:

```
jvm.app-configured=/path/to/downloaded/java
```

Ensure that the right hand side of this assignment matches the Java download location.

#### Other failures

We have tested these steps using [freshly downloaded Fiji installations](https://imagej.net/downloads) for each platform. Given the number of update sites and plugins available for Fiji, it is possible there are unforseen interactions. Thus, falling back to one of these fresh installs is a recommended starting point.
