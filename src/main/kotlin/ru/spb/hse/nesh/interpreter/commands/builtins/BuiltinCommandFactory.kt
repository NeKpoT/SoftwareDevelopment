package ru.spb.hse.nesh.interpreter.commands.builtins

import ru.spb.hse.nesh.interpreter.commands.Command
import ru.spb.hse.nesh.interpreter.commands.io.Sink
import ru.spb.hse.nesh.interpreter.commands.io.Source
import ru.spb.hse.nesh.interpreter.interfaces.Environment

/**
 * A [ru.spb.hse.nesh.interpreter.commands.CommandFactory] for builtin commands.
 *
 * Accepts following commands:
 * * `"echo"` - [Echo]
 * * `"cat"` - [Cat]
 * * `"wc"` - [WordCount]
 * * `"pwd"` - [Pwd]
 * * `"exit"` - [Exit]
 * * `"cd"` - [Cd]
 * * `"ls"` - [Ls]
 */
object BuiltinCommandFactory : AbstractCommandFactory() {
    override fun createCommandByName(
        programName: String,
        arguments: List<String>,
        input: Source,
        output: Sink,
        env: Environment
    ): Command? = when (programName) {
        "echo" -> Echo(output.getSinkStream(), arguments)
        "cat" -> Cat(input, output, arguments, env, PathExpand(env))
        "wc" -> WordCount(input, output, arguments, env, PathExpand(env))
        "pwd" -> Pwd(output, env)
        "exit" -> Exit(env, arguments, output)
        "ls" -> Ls(output, arguments, env, PathExpand(env))
        "cd" -> Cd(arguments, env, PathExpand(env))
        else -> null
    }
}