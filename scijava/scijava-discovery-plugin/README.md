# SciJava Discovery Plugin: A Discoverer implementation backed by SciJava Common's `PluginService`

This module provides the `PluginBasedDiscoverer`, a `Discoverer` implementation that uses [SciJava Common](https://github.com/scijava/scijava-common)'s annotation processor to create `Discovery` objects. `Plugin` `Class`es are wrapped into `Discovery` objects, and are returned via `PluginBasedDiscoverer.implsOfType()`. Please check out SciJava Common for instructions on developing `Plugin`s.
