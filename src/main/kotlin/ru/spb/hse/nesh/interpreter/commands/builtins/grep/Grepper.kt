package ru.spb.hse.nesh.interpreter.commands.builtins.grep

import java.io.*

/**
 * Provides functionality similar to `grep`.
 *
 * If [afterContext] is not zero, will print separator between contiguous groups of matches.
 *
 * @param patternText   a text of regular expression to search in files
 * @param wholeWord     if true, searches for a whole word
 * @param ignoreCase    if true, search ignores case
 * @param afterContext  number of lines to print after a matching line.
 */
open class Grepper(
    patternText: String,
    wholeWord: Boolean = false,
    ignoreCase: Boolean = false,
    private val afterContext: Int = 0
) {
    init {
        require(afterContext >= 0) { "afterContext must be positive" }
        if (wholeWord) {
            // Check if it was a valid regex by itself (and throw something otherwise).
            // Needed as protection against injections like "( \" which break appended look-ahead "(?!\w)"
            patternText.toRegex()
        }
    }

    private val pattern: Regex = (if (wholeWord) "(?<!\\w)$patternText(?!\\w)" else patternText).let {
        if (ignoreCase) it.toRegex(RegexOption.IGNORE_CASE) else it.toRegex()
    }

    /**
     * Prints all matches from [input] to [output] according to behavior defined by constructor arguments.
     *
     * Does not close either [input] or [output].
     *
     * @throws IOException  if IO error occurs.
     */
    fun grep(input: BufferedReader, output: Writer) {
        var sinceLastMatch = 0
        var firstMatch = true
        input.lineSequence().forEach {line ->
            if (pattern.containsMatchIn(line)) {
                if (sinceLastMatch > afterContext && afterContext != 0 && !firstMatch) {
                    output.appendln(groupSeparator)
                }
                firstMatch = false
                sinceLastMatch = -1
            }
            if (sinceLastMatch < afterContext) {
                output.appendln(line)
            }
            sinceLastMatch++
        }
    }

    /**
     * Runs [grep] on [file].
     *
     * This grep catches all IOExceptions and reports them using [reportError].
     */
    fun grep(output: Writer, file: File) {
        val reader: BufferedReader
        try {
            reader = file.bufferedReader()
        } catch (ex: FileNotFoundException) {
            reportError("grep can't read from file $file. Does it exist?")
            return
        } catch (ex: SecurityException) {
            reportError("Security manager prevents reading file $file")
            return
        }

        try {
            reader.use { grep(it, output) }
        } catch (ex: IOException) {
            reportError("IO error with file $file")
        }
    }

    /**
     *  Runs [grep] of all [files] printing their lines generated by [fileNameLine].
     *
     * @throws IOException  if IO error occurs while printing a line with a filename.
     */
    fun grep(output: Writer, files: List<File>) = files.forEach { file ->
        output.appendln(fileNameLine(file))
        grep(output, file)
    }

    /** Separator to print instead of skipped lines when [afterContext] is not `0`.  */
    open val groupSeparator = "--"

    /** Produces line to print before matches from a file. */
    open fun fileNameLine(file: File) = "-- $file --"

    /** Used to report any errors encountered by grep. */
    open fun reportError(error: String) {
        System.err.println(error)
    }

    override fun toString(): String = "Grepper(\"${pattern}\", ${pattern.options})"

}