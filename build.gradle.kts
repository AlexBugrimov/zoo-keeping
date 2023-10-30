plugins {
    checkstyle
}

group = "dev.bug"
version = "1.0-SNAPSHOT"

checkstyle {
    toolVersion = "10.12.2"
    configFile = file("${project.rootDir}/quality/checkstyle/checkstyle.xml")
    isIgnoreFailures = true
}