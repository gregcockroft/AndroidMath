package com.agog.mathdisplay.parse

/**
 * Created by greg on 2/12/18.
 */


enum class MTParseErrors {
    ErrorNone,
    /// The braces { } do not match.
    MismatchBraces,
    /// A command in the string is not recognized.
    InvalidCommand,
    /// An expected character such as ] was not found.
    CharacterNotFound,
    /// The \left or \right command was not followed by a delimiter.
    MissingDelimiter,
    /// The delimiter following \left or \right was not a valid delimiter.
    InvalidDelimiter,
    /// There is no \right corresponding to the \left command.
    MissingRight,
    /// There is no \left corresponding to the \right command.
    MissingLeft,
    /// The environment given to the \begin command is not recognized
    InvalidEnv,
    /// A command is used which is only valid inside a \begin,\end environment
    MissingEnv,
    /// There is no \begin corresponding to the \end command.
    MissingBegin,
    /// There is no \end corresponding to the \begin command.
    MissingEnd,
    /// The number of columns do not match the environment
    InvalidNumColumns,
    /// Internal error, due to a programming mistake.
    InternalError,
    /// Limit control applied incorrectly
    InvalidLimits
}

data class MTParseError(var errorcode: MTParseErrors = MTParseErrors.ErrorNone, var errordesc: String = "") {

    fun copyFrom(src: MTParseError?) {
        if (src != null) {
            this.errorcode = src.errorcode
            this.errordesc = src.errordesc
        }
    }

    fun clear() {
        this.errorcode = MTParseErrors.ErrorNone
        this.errordesc = ""
    }
}