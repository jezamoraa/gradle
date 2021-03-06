/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.internal.buildoption

import org.gradle.cli.CommandLineOption
import org.gradle.cli.CommandLineParser
import org.gradle.cli.ParsedCommandLine
import spock.lang.Specification

import static org.gradle.internal.buildoption.BuildOptionFixture.*

class ListBuildOptionTest extends Specification {

    private static final List<String> SAMPLE_VALUES = ['val1', 'val2', 'val3']

    def testSettings = new TestSettings()
    def commandLineParser = new CommandLineParser()

    def "can apply from property"() {
        given:
        def testOption = new TestOption(GRADLE_PROPERTY, CommandLineOptionConfiguration.create(LONG_OPTION, SHORT_OPTION, DESCRIPTION))

        when:
        testOption.applyFromProperty([:], testSettings)

        then:
        testSettings.values.empty
        !testSettings.origin

        when:
        testOption.applyFromProperty([(GRADLE_PROPERTY): 'val1, val2, val3'], testSettings)

        then:
        testSettings.values == SAMPLE_VALUES
        testSettings.origin == BuildOption.Origin.GRADLE_PROPERTY
    }

    def "can configure command line parser"() {
        when:
        def testOption = new TestOption(GRADLE_PROPERTY)
        testOption.configure(commandLineParser)

        then:
        !commandLineParser.optionsByString.containsKey(LONG_OPTION)
        !commandLineParser.optionsByString.containsKey(SHORT_OPTION)

        when:
        testOption = new TestOption(GRADLE_PROPERTY, CommandLineOptionConfiguration.create(LONG_OPTION, SHORT_OPTION, DESCRIPTION))
        testOption.configure(commandLineParser)

        then:
        CommandLineOption longOption = commandLineParser.optionsByString[LONG_OPTION]
        CommandLineOption shortOption = commandLineParser.optionsByString[SHORT_OPTION]
        assertMultipleArgument(longOption)
        assertMultipleArgument(shortOption)
        assertNoDeprecationWarning(longOption)
        assertNoDeprecationWarning(shortOption)
    }

    def "can configure incubating command line option"() {
        when:
        def commandLineOptionConfiguration = CommandLineOptionConfiguration.create(LONG_OPTION, SHORT_OPTION, DESCRIPTION)

        if (incubating) {
            commandLineOptionConfiguration.incubating()
        }

        def testOption = new TestOption(GRADLE_PROPERTY, commandLineOptionConfiguration)
        testOption.configure(commandLineParser)

        then:
        assertIncubating(commandLineParser.optionsByString[LONG_OPTION], incubating)
        assertIncubating(commandLineParser.optionsByString[SHORT_OPTION], incubating)

        where:
        incubating << [false, true]
    }

    def "can configure deprecated command line option"() {
        given:
        String deprecationWarning = 'replaced by other'

        when:
        def commandLineOptionConfiguration = CommandLineOptionConfiguration.create(LONG_OPTION, SHORT_OPTION, DESCRIPTION)
            .deprecated(deprecationWarning)

        def testOption = new TestOption(GRADLE_PROPERTY, commandLineOptionConfiguration)
        testOption.configure(commandLineParser)

        then:
        assertDeprecationWarning(commandLineParser.optionsByString[LONG_OPTION], deprecationWarning)
        assertDeprecationWarning(commandLineParser.optionsByString[SHORT_OPTION], deprecationWarning)
    }

    def "can apply from command line"() {
        when:
        def testOption = new TestOption(GRADLE_PROPERTY)
        def options = [] as List<CommandLineOption>
        def parsedCommandLine = new ParsedCommandLine(options)
        testOption.applyFromCommandLine(parsedCommandLine, testSettings)

        then:
        testSettings.values.empty
        !testSettings.origin

        when:
        testOption = new TestOption(GRADLE_PROPERTY, CommandLineOptionConfiguration.create(LONG_OPTION, SHORT_OPTION, DESCRIPTION))
        def option = new CommandLineOption([LONG_OPTION])
        options << option
        parsedCommandLine = new ParsedCommandLine(options)
        def parsedCommandLineOption = parsedCommandLine.addOption(LONG_OPTION, option)
        parsedCommandLineOption.addArgument('val1')
        parsedCommandLineOption.addArgument('val2')
        parsedCommandLineOption.addArgument('val3')
        testOption.applyFromCommandLine(parsedCommandLine, testSettings)

        then:
        testSettings.values == SAMPLE_VALUES
        testSettings.origin == BuildOption.Origin.COMMAND_LINE
    }

    static class TestOption extends ListBuildOption<TestSettings> {

        TestOption(String gradleProperty) {
            super(gradleProperty)
        }

        TestOption(String gradleProperty, CommandLineOptionConfiguration commandLineOptionConfiguration) {
            super(gradleProperty, commandLineOptionConfiguration)
        }

        @Override
        void applyTo(List<String> values, TestSettings settings, BuildOption.Origin origin) {
            settings.values.addAll(values)
            settings.origin = origin
        }
    }

    static class TestSettings {
        List<String> values = []
        BuildOption.Origin origin
    }
}

