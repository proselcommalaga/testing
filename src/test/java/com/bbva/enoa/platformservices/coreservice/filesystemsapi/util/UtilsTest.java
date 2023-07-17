package com.bbva.enoa.platformservices.coreservice.filesystemsapi.util;

import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.stream.Stream;

import static com.bbva.enoa.platformservices.coreservice.filesystemsapi.util.Constants.FilesystemsErrorConstants.FILESYSTEM_INVALID_SIZE_ERROR;
import static com.bbva.enoa.platformservices.coreservice.filesystemsapi.util.Constants.FilesystemsErrorConstants.FILESYSTEM_SIZE_UNIT_ERROR;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UtilsTest
{
    @ParameterizedTest
    @ArgumentsSource(ExpectedSizeInTextAndCorrespondingSizeInKbArgumentsProvider.class)
    void givenCorrectSizeInText_whenGetSizeInKb_thenCorrectValueIsObtained(final String sizeInText, final Integer expectedSizeInKb)
    {
        // When
        final Integer obtainedSizeInKb = Utils.getSizeInKB(sizeInText);

        // Then
        assertNotNull(obtainedSizeInKb);
        assertEquals("Obtained size in KB is not the expected one", expectedSizeInKb, obtainedSizeInKb);
    }

    @ParameterizedTest
    @ArgumentsSource(UnexpectedSizeUnitArgumentsProvider.class)
    void givenIncorrectSizeUnitInText_whenGetSizeInKb_thenFilesystemSizeUnitErrorIsThrown(final String sizeInText)
    {
        // When
        final NovaException exception = assertThrows(
                NovaException.class,
                () -> Utils.getSizeInKB(sizeInText)
        );

        // Then
        assertEquals("Unexpected error code thrown", FILESYSTEM_SIZE_UNIT_ERROR, exception.getErrorCode().getErrorCode());
    }

    @ParameterizedTest
    @ArgumentsSource(InvalidSizeArgumentsProvider.class)
    void givenInvalidSize_whenGetSizeInKb_thenFilesystemInvalidSizeErrorIsThrown(final String sizeInText)
    {
        // When
        final NovaException exception = assertThrows(
                NovaException.class,
                () -> Utils.getSizeInKB(sizeInText)
        );

        // Then
        assertEquals("Unexpected error code thrown", FILESYSTEM_INVALID_SIZE_ERROR, exception.getErrorCode().getErrorCode());
    }

    static class ExpectedSizeInTextAndCorrespondingSizeInKbArgumentsProvider implements ArgumentsProvider
    {
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext)
        {
            return Stream.of(
                    Arguments.of("0", 0),
                    Arguments.of("-0K", 0),
                    Arguments.of("0K", 0),
                    Arguments.of("-0k", 0),
                    Arguments.of("0k", 0),
                    Arguments.of("2K", 2),
                    Arguments.of("0M", 0),
                    Arguments.of("-0M", 0),
                    Arguments.of("0m", 0),
                    Arguments.of("-0m", 0),
                    Arguments.of("1M", 1000),
                    Arguments.of("500M", 500 * 1000),
                    Arguments.of("0G", 0),
                    Arguments.of("-0G", 0),
                    Arguments.of("0g", 0),
                    Arguments.of("-0g", 0),
                    Arguments.of("1G", 1000 * 1000),
                    Arguments.of("53G", 53 * 1000 * 1000)
            );
        }
    }

    static class UnexpectedSizeUnitArgumentsProvider implements ArgumentsProvider
    {
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext)
        {
            return Stream.of(
                    Arguments.of("347"),
                    Arguments.of("-156"),
                    Arguments.of("-16k"),
                    Arguments.of("1k"),
                    Arguments.of("-1k"),
                    Arguments.of("750m"),
                    Arguments.of("896g"),
                    Arguments.of("1256g")
            );
        }
    }

    static class InvalidSizeArgumentsProvider implements ArgumentsProvider
    {
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext)
        {
            return Stream.of(
                    Arguments.of((String) null),
                    Arguments.of("asdf")
            );
        }
    }
}
