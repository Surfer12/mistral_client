/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Swift project to get you started.
 * For more details on building Swift applications and libraries, please refer to https://docs.gradle.org/8.11.1/userguide/building_swift_projects.html in the Gradle documentation.
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    // Apply the swift-application plugin to add support for building Swift executables
    `swift-application`

    // Apply the xctest plugin to add support for building and running Swift test executables (Linux) or bundles (macOS)
    xctest
}

// Set the target operating system and architecture for this application
application {
    // Set the target operating system and architecture for this library
    targetMachines.add(machines.macOS.architecture("aarch64"))
}
